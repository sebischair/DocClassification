package util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import controllers.MorphiaObject;
import model.Pipeline;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import play.libs.Json;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class StaticFunctions {
    public static String LABEL = "label";
    public static String TEXT = "text";
    public static String FEATURES = "features";
    public static String WORDS = "words";

    public static ObjectNode printResults(Dataset<Row> predictionAndLabels, Map labelMap) {
        ObjectNode result = Json.newObject();
        MulticlassMetrics metrics = new MulticlassMetrics(predictionAndLabels);

        // Confusion matrix
        Matrix confusion = metrics.confusionMatrix();
        System.out.println("Confusion matrix: \n" + confusion);
        double[] matrix_array = confusion.toArray();
        ArrayNode matrix = Json.newArray();
        for(int i=0; i <matrix_array.length; i++) {
            matrix.add(matrix_array[i]);
        }
        result.put("confusion_matrix", matrix);

        // Overall statistics
        result.put("Accuracy", new Double(metrics.accuracy() * 100).intValue());
        System.out.println("Accuracy = " + metrics.accuracy());

        // Stats by labels
        for (int i = 0; i < metrics.labels().length; i++) {
            ArrayNode array = Json.newArray();
            array.add(new Double(metrics.precision(metrics.labels()[i]) * 100).intValue());
            array.add(new Double(metrics.recall(metrics.labels()[i]) * 100).intValue());
            array.add(new Double(metrics.fMeasure(metrics.labels()[i]) * 100).intValue());
            result.put(labelMap.get(i).toString(), array);
//
//            System.out.format("Class %f precision = %f\n", metrics.labels()[i],metrics.precision(metrics.labels()[i]));
//            System.out.format("Class %f recall = %f\n", metrics.labels()[i], metrics.recall(metrics.labels()[i]));
//            System.out.format("Class %f F1 score = %f\n", metrics.labels()[i], metrics.fMeasure(metrics.labels()[i]));
        }

        //Weighted stats
        result.put("weighted_precision", new Double(metrics.weightedPrecision() * 100).intValue());
        result.put("weighted_recall", new Double(metrics.weightedRecall() * 100).intValue());
        result.put("weighted_F1_score", new Double(metrics.weightedFMeasure() * 100).intValue());
        result.put("weighted_false_positive_rate", new Double(metrics.weightedFalsePositiveRate() * 100).intValue());
//
//        System.out.format("Weighted precision = %f\n", metrics.weightedPrecision());
//        System.out.format("Weighted recall = %f\n", metrics.weightedRecall());
//        System.out.format("Weighted F1 score = %f\n", metrics.weightedFMeasure());
//        System.out.format("Weighted false positive rate = %f\n", metrics.weightedFalsePositiveRate());

        return result;
    }

    public static JavaRDD<Row> getRDDsFromHref(JavaSparkContext sparkContext, String path, double label) {
        try {
            if(path.contains("files")) {
                //get the file in the path
                HttpURLConnection connection = RestCaller.connectionForGetRequest(path);
                String output = RestCaller.outputForConnection(connection);
                System.out.println("Status: " + connection.getResponseCode() + " - " + RestCaller.responseCodeDisplayForCode(connection.getResponseCode()));
                JSONObject jo = new JSONObject(output);
                connection = RestCaller.connectionForGetRequest(jo.getString("href")+ "/content");
                RestCaller.saveFile(connection, jo.getString("name"), label);
                System.out.println("File saved");
            } else {
                //get all files in this path
                HttpURLConnection connection = RestCaller.connectionForGetRequest(path + "/files");
                String output = RestCaller.outputForConnection(connection);
                System.out.println("Status: " + connection.getResponseCode() + " - " + RestCaller.responseCodeDisplayForCode(connection.getResponseCode()));
                JSONArray newArray = new JSONArray(output);
                for(int i=0;i<newArray.length(); i++) {
                    JSONObject jo = newArray.getJSONObject(i);
                    //RestCaller.saveFile(jo.getString("href")+ "/content", jo.getString("name"), label);
                    connection = RestCaller.connectionForGetRequest(jo.getString("href")+ "/content");
                    RestCaller.saveFile(connection, jo.getString("name"), label);
                    System.out.println("Files saved");
                }
            }

            JavaPairRDD<String, String> logData = sparkContext.wholeTextFiles(play.Play.application().path().getAbsolutePath()+"/tmp/"+label).cache();
            return logData.values().map(new Function<String, Row>() {
                @Override
                public Row call(String s) throws Exception {
                    if(label != -1) {
                        return RowFactory.create(label, s);
                    } else {
                        return RowFactory.create(s);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JavaRDD<Row> getRDDs(JavaSparkContext sparkContext, String dir, double label) {
        JavaPairRDD<String, String> logData = sparkContext.wholeTextFiles(dir).cache();
        return logData.values().map(new Function<String, Row>() {
            @Override
            public Row call(String s) throws Exception {
                if(label != -1) {
                    return RowFactory.create(label, s);
                } else {
                    return RowFactory.create(s);
                }
            }
        });
    }

    public static String deserializeToJSON(List<?> objList, String... removeAttributes) {
        List<DBObject> dbObjList = new ArrayList<>(objList.size());
        DBObject dbObj;
        for(Object obj :objList){
            dbObj = MorphiaObject.morphia.toDBObject(obj);
            for(int i=0; i < removeAttributes.length; i++){
                dbObj.removeField(removeAttributes[i]);
            }
            dbObjList.add(dbObj);
        }
        String json = JSON.serialize(dbObjList);
        return json;
    }

    public static String deserializeToJSON(Object obj, String... removeAttributes) {
        DBObject dbObj;
        dbObj = MorphiaObject.morphia.toDBObject(obj);
        for(int i=0; i < removeAttributes.length; i++){
            dbObj.removeField(removeAttributes[i]);
        }
        String json = JSON.serialize(dbObj);
        return json;
    }

    public static Pipeline getPipeline(String pipelineName) {
        return (Pipeline) new Pipeline().findByName("name", pipelineName);
    }
}
