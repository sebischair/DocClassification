package util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import controllers.MorphiaObject;
import model.Label;
import model.Pipeline;
import org.apache.lucene.analysis.CharArraySet;
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
import org.tartarus.snowball.ext.PorterStemmer;
import play.libs.Json;
import play.mvc.Result;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class StaticFunctions {
    public static String LABEL = "label";
    public static String TEXT = "text";
    public static String FEATURES = "features";
    public static String WORDS = "words";
    public static String[] STOPWORDS = {"a", "an", "and", "the", "that", "they", "which", "we", "us", "i", "me", "like", "that", "only", "much", "kafka", "it", "is", "if", "by", "basically", "are", "as", "but", "those", "spark", "also"};
    public static final Set<String> STOPWORDS_SET = new HashSet<String>(Arrays.asList(STOPWORDS));
    private static final CharArraySet STOPWORDS_CHARSET = CharArraySet.unmodifiableSet(CharArraySet.copy(STOPWORDS_SET));;

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

    public static JavaRDD<Row> getRDDsFromHref(JavaSparkContext sparkContext, String documentsPath, int label) {
        try{
            if(documentsPath.contains("files")) {
                //get the file in the path
                HttpURLConnection connection = RestCaller.connectionForGetRequest(documentsPath);
                String output = RestCaller.outputForConnection(connection);
                System.out.println("Status: " + connection.getResponseCode() + " - " + RestCaller.responseCodeDisplayForCode(connection.getResponseCode()));
                JSONObject jo = new JSONObject(output);
                connection = RestCaller.connectionForGetRequest(jo.getString("href")+ "/content");
                RestCaller.saveFile(connection, jo.getString("name"), label);
            } else {
                //get all files in this path
                HttpURLConnection connection = RestCaller.connectionForGetRequest(documentsPath + "/files");
                String output = RestCaller.outputForConnection(connection);
                System.out.println("Status: " + connection.getResponseCode() + " - " + RestCaller.responseCodeDisplayForCode(connection.getResponseCode()));
                JSONArray newArray = new JSONArray(output);
                for(int i=0;i<newArray.length(); i++) {
                    JSONObject jo = newArray.getJSONObject(i);
                    //RestCaller.saveFile(jo.getString("href")+ "/content", jo.getString("name"), label);
                    connection = RestCaller.connectionForGetRequest(jo.getString("href")+ "/content");
                    RestCaller.saveFile(connection, jo.getString("name"), label);
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
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<Row> getRDDsFromHref(JavaSparkContext sparkContext, List<Label> labels, Pipeline pipeline) {
        List<Row> data = new ArrayList();
        try {
            HttpURLConnection connection = RestCaller.connectionForGetRequest(labels.get(0).getPath() + "/entities");
            JSONArray newArray = new JSONArray(RestCaller.outputForConnection(connection));
            Map map = new HashMap();
            for(int m=0; m<labels.size(); m++) {
                map.put(labels.get(m).getName(), new ArrayList<>());
            }
            for(int i=0; i<newArray.length(); i++) {
                JSONObject newObject = newArray.getJSONObject(i);
                HttpURLConnection newConnection = RestCaller.connectionForGetRequest(newObject.getString("href"));
                System.out.println(i);
                JSONObject entityObject = new JSONObject(RestCaller.outputForConnection(newConnection));
                JSONArray entityAttributes = entityObject.getJSONArray("attributes");
                List<String> miningAttributes = pipeline.getMiningAttributes();
                String text = "";

                for(int j=0; j<labels.size(); j++) {
                    Label label = labels.get(j);
                    if(tagValuesMatch(entityAttributes, pipeline.getTag(), label)) {
                        for(String miningAttribute: miningAttributes) {
                            String textValue = getStringValueFromSCObject(entityAttributes, miningAttribute);
                            if(textValue != null) text += " " + textValue;
                        }
                        if(text != "") ((ArrayList) map.get(label.getName())).add(text);
                    }
                    //if(text != "") ((ArrayList) map.get(label.getName())).add(stemWords(removeStopWords(text)));
                }
                //Thread.sleep(1000);
            }
            for(int k=0; k<labels.size(); k++) {
                JavaRDD<String> listRDD = sparkContext.parallelize(((ArrayList) map.get(labels.get(k).getName())).subList(0, ((ArrayList) map.get(labels.get(k).getName())).size()-1));
                double finalJ = (double) k;
                data.addAll(listRDD.map(new Function<String, Row>() {
                    @Override
                    public Row call(String s) throws Exception {
                        return RowFactory.create(finalJ, s);
                    }
                }).collect());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static JavaRDD<Row> getRDDsFromHref(JavaSparkContext sparkContext, Label label, double labelValue, Pipeline pipeline) {
        try {
            if(label.getType() == "Page") {
                if(label.getPath().contains("files")) {
                    //get the file in the path
                    HttpURLConnection connection = RestCaller.connectionForGetRequest(label.getPath());
                    String output = RestCaller.outputForConnection(connection);
                    System.out.println("Status: " + connection.getResponseCode() + " - " + RestCaller.responseCodeDisplayForCode(connection.getResponseCode()));
                    JSONObject jo = new JSONObject(output);
                    connection = RestCaller.connectionForGetRequest(jo.getString("href")+ "/content");
                    RestCaller.saveFile(connection, jo.getString("name"), labelValue);
                } else {
                    //get all files in this path
                    HttpURLConnection connection = RestCaller.connectionForGetRequest(label.getPath() + "/files");
                    String output = RestCaller.outputForConnection(connection);
                    System.out.println("Status: " + connection.getResponseCode() + " - " + RestCaller.responseCodeDisplayForCode(connection.getResponseCode()));
                    JSONArray newArray = new JSONArray(output);
                    for(int i=0;i<newArray.length(); i++) {
                        JSONObject jo = newArray.getJSONObject(i);
                        //RestCaller.saveFile(jo.getString("href")+ "/content", jo.getString("name"), label);
                        connection = RestCaller.connectionForGetRequest(jo.getString("href")+ "/content");
                        RestCaller.saveFile(connection, jo.getString("name"), labelValue);
                    }
                }
                JavaPairRDD<String, String> logData = sparkContext.wholeTextFiles(play.Play.application().path().getAbsolutePath()+"/tmp/"+label).cache();
                return logData.values().map(new Function<String, Row>() {
                    @Override
                    public Row call(String s) throws Exception {
                        if(labelValue != -1) {
                            return RowFactory.create(label, s);
                        } else {
                            return RowFactory.create(s);
                        }
                    }
                });
            } else {
                HttpURLConnection connection = RestCaller.connectionForGetRequest(label.getPath() + "/entities");
                JSONArray newArray = new JSONArray(RestCaller.outputForConnection(connection));
                List<String> labeledText = new ArrayList<>();
                for(int i=0; i<newArray.length(); i++) {
                    //if(i>160)break;
                    JSONObject newObject = newArray.getJSONObject(i);
                    HttpURLConnection newConnection = RestCaller.connectionForGetRequest(newObject.getString("href"));
                    System.out.println(i);
                    JSONObject entityObject = new JSONObject(RestCaller.outputForConnection(newConnection));
                    JSONArray entityAttributes = entityObject.getJSONArray("attributes");
                    List<String> miningAttributes = pipeline.getMiningAttributes();
                    String text = "";

                    if(tagValuesMatch(entityAttributes, pipeline.getTag(), label)) {
                        for(String miningAttribute: miningAttributes) {
                            String textValue = getStringValueFromSCObject(entityAttributes, miningAttribute);
                            if(textValue != null) text += " " + textValue;
                        }
                    }
                    //if(text != "") labeledText.add(stemWords(removeStopWords(text)));
                    if(text != "") labeledText.add(text);
                }
                JavaRDD<String> listRDD = sparkContext.parallelize(labeledText);
                return listRDD.map(new Function<String, Row>() {
                    @Override
                    public Row call(String s) throws Exception {
                        if(labelValue != -1) {
                            return RowFactory.create(labelValue, s);
                        } else {
                            return RowFactory.create(s);
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean tagValuesMatch(JSONArray entityAttributes, String tag, Label label) {
        for(int j=0; j<entityAttributes.length(); j++) {
            JSONObject entityAttribute = entityAttributes.getJSONObject(j);
            if(entityAttribute.getString("name").equals(tag)) {
                JSONArray jsonValue = entityAttribute.getJSONArray("values");
                if(jsonValue.length() > 0) {
                    if(label.getType().equals("Boolean")) {
                        Boolean value = jsonValue.getBoolean(0);
                        if((value && label.getName().equals("1")) || (!value && label.getName().equals("0"))) return true;
                        else return false;
                    } else {
                        return jsonValue.getJSONObject(0).getString("name").equalsIgnoreCase(label.getName());
                    }
                }
            }
        }
        return false;
    }

    public static String getStringValueFromSCObject(JSONArray entityAttributes, String attributeName) {
        for(int j=0; j<entityAttributes.length(); j++) {
            JSONObject attr = entityAttributes.getJSONObject(j);
            if(attr.getString("name").equals(attributeName)) {
                JSONArray jsonValue = attr.getJSONArray("values");
                if(jsonValue.length() > 0) {
                    return jsonValue.getString(0);
                }
            }
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

    public static Result jsonResult(Result httpResponse) {
        return httpResponse.as("application/json; charset=utf-8");
    }

    public static String removeStopWords(String text) {
        text = text.toLowerCase();
        for(String s: STOPWORDS) {
            text = text.replaceAll("\\b"+s+"\\b", "");
        }
        return text.replaceAll("[()]", "method");
    }

    public static String stemWords(String input) throws IOException {
        PorterStemmer stem = new PorterStemmer();
        StringBuilder builder = new StringBuilder(input.length());
        String[] words = input.split("\\s+");
        for (String word : words) {
            stem.setCurrent(word);
            stem.stem();
            builder.append(stem.getCurrent() + " ");
        }
        return builder.toString();
    }
}
