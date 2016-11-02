package util.pipeline;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Label;
import model.Pipeline;
import org.apache.commons.io.FileUtils;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.OneVsRest;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import util.StaticFunctions;
import util.mlLib.DefaultOneVsRest;
import util.textProcessing.DefaultHashingTF;
import util.textProcessing.DefaultTokenizer;
import util.training.TrainingPipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class ExampleTrainingPipeline extends TrainingPipeline {
    List<Row> data = new ArrayList();
    Map labelMap = new HashMap();

    public void loadDocuments(Pipeline pipeline) {
        List<Label> labels = pipeline.getLabels();
        for(int i=0; i<labels.size(); i++) {
            labelMap.put(i, labels.get(i).getName());
            data.addAll(StaticFunctions.getRDDs(this.getSparkContext(), labels.get(i).getPath(), i).collect());
        }
    }

    @Override
    public void createDataFrame(Pipeline pipeline) {
        loadDocuments(pipeline);
        StructType schema = new StructType(new StructField[]{
                new StructField(StaticFunctions.LABEL, DataTypes.DoubleType, false, Metadata.empty()),
                new StructField(StaticFunctions.TEXT, DataTypes.StringType, false, Metadata.empty())
        });

        Dataset<Row> dataset = this.getSqlContext().createDataFrame(data, schema);
        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.9, 0.1});
        this.setTrainingData(splits[0]);
        this.setTestingData(splits[1]);
    }

    @Override
    public void setStages() {
        Tokenizer tokenizer = DefaultTokenizer.get();
        //Word2Vec word2vec = new DefaultWord2Vec().get(tokenizer);
        //NGram ng = new NGram().setInputCol(tokenizer.getOutputCol()).setOutputCol("rawFeatures").setN(3);
        HashingTF hashingTF = DefaultHashingTF.get(tokenizer.getOutputCol(), 10000);
        //LogisticRegression lr = new DefaultLogisticRegression().get();
        //DecisionTreeClassifier dtc = DefaultDecisionTreeClassifier.get();
        //SVMWithSGD svm = new DefaultSVMWithSGD().get();
        //NaiveBayes nb = new DefaultNaiveBayes().get();
        //KMeans kmeans = new KMeans().setK(3);
        //LDA lda = new LDA().setK(10).setMaxIter(10);
        OneVsRest ovr = new DefaultOneVsRest().get();
        //RandomForestClassifier rfc = new DefaultRandomForestClassifier().get();
        this.getPipeline().setStages(new PipelineStage[]{tokenizer, hashingTF, ovr});
    }

    @Override
    public void trainDocuments() {
        this.setModel(this.getPipeline().fit(this.getTrainingData()));
    }

    @Override
    public ObjectNode testDocuments() {
        Dataset<Row> predictions = this.getModel().transform(this.getTestingData());
        predictions.show();
        return StaticFunctions.printResults(predictions.select("prediction", "label"), labelMap);
    }

    @Override
    public void saveModel(Pipeline pipeline) {
        try {
            FileUtils.deleteDirectory(new File("sparkModels/" + pipeline.getName()));
            this.getModel().save("sparkModels/" + pipeline.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
