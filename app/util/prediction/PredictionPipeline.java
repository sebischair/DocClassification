package util.prediction;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.SQLContext;
import util.SparkSingleton;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public abstract class PredictionPipeline {

    private JavaSparkContext sparkContext = SparkSingleton.getInstance().getSparkContext();

    private SQLContext sqlContext = SparkSingleton.getInstance().getSqlContext();

    private Pipeline pipeline = new Pipeline();

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    private PipelineModel model = null;

    public abstract void loadModel(String documentsPath);

    public abstract void createDataFrame(String documentsPath);

    public abstract ArrayNode applyModel();

    public void cleanUp() {
        sqlContext.clearCache();
        sparkContext.stop();
        sparkContext.close();
    }

    public ArrayNode execute(model.Pipeline pipeline, String documentsPath) {
        loadModel(pipeline.getModelPath());
        createDataFrame(documentsPath);
        ArrayNode results = applyModel();
        cleanUp();
        return results;
    }

    public void setModel(PipelineModel model) {
        this.model = model;
    }

    public JavaSparkContext getSparkContext() {
        return sparkContext;
    }

    public SQLContext getSqlContext() {
        return sqlContext;
    }

    public PipelineModel getModel() {
        return model;
    }
}
