package util.pipeline;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import play.libs.Json;
import util.StaticFunctions;
import util.prediction.PredictionPipeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class ExamplePredictionPipeline extends PredictionPipeline {
    List<Row> data = new ArrayList();
    Dataset<Row> dataset = null;

    public void loadDocuments(String documentsPath) {
        data.addAll(StaticFunctions.getRDDs(this.getSparkContext(), documentsPath).collect());
    }

    @Override
    public void loadModel(String modelPath) {
        this.setModel(PipelineModel.load(modelPath));
    }

    @Override
    public void createDataFrame(String documentsPath) {
        loadDocuments(documentsPath);
        StructType schema = new StructType(new StructField[]{
                new StructField(StaticFunctions.TEXT, DataTypes.StringType, false, Metadata.empty())
        });
        dataset = this.getSqlContext().createDataFrame(data, schema);
    }

    @Override
    public ArrayNode applyModel() {
        Iterator predictions = this.getModel().transform(dataset).cache().toJSON().collectAsList().iterator();
        ArrayNode result = Json.newArray();
        while(predictions.hasNext()) {
            result.add(Json.parse(predictions.next().toString()));
        }
        return result;
    }
}
