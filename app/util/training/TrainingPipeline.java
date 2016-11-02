package util.training;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import util.SparkSingleton;
import util.StaticFunctions;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public abstract class TrainingPipeline {
    private JavaSparkContext sparkContext = SparkSingleton.getInstance().getSparkContext();

    private SQLContext sqlContext = SparkSingleton.getInstance().getSqlContext();

    private Pipeline pipeline = new Pipeline();

    private Dataset<Row> trainingData = null;

    private Dataset<Row> testingData = null;

    private PipelineModel model = null;

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public abstract void createDataFrame(model.Pipeline pipeline);

    public abstract void setStages();

    public abstract void trainDocuments();

    public abstract ObjectNode testDocuments();

    public abstract void saveModel(model.Pipeline pipeline);

    public void cleanUp() {
        sqlContext.clearCache();
    }

    public ObjectNode execute(model.Pipeline pipeline) {
        createDataFrame(pipeline);
        setStages();
        addCrossValidator();
        trainDocuments();
        ObjectNode results = testDocuments();
        saveModel(pipeline);
        cleanUp();
        return results;
    }

    public JavaSparkContext getSparkContext() {
        return sparkContext;
    }

    public SQLContext getSqlContext() {
        return sqlContext;
    }

    public void addCrossValidator() {
        ParamMap[] paramGrid = new ParamGridBuilder().build();
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator();
        evaluator.setLabelCol(StaticFunctions.LABEL);
        evaluator.setPredictionCol("prediction");
        evaluator.setMetricName("accuracy");

        CrossValidator crossval = new CrossValidator();
        crossval.setEstimator(this.pipeline);
        crossval.setEvaluator(evaluator);
        crossval.setEstimatorParamMaps(paramGrid);
        crossval.setNumFolds(10);
    }

    public Dataset<Row> getTrainingData() {
        return trainingData;
    }

    public void setTrainingData(Dataset<Row> trainingData) {
        this.trainingData = trainingData;
    }

    public Dataset<Row> getTestingData() {
        return testingData;
    }

    public void setTestingData(Dataset<Row> testingData) {
        this.testingData = testingData;
    }

    public PipelineModel getModel() {
        return model;
    }

    public void setModel(PipelineModel model) {
        this.model = model;
    }
}
