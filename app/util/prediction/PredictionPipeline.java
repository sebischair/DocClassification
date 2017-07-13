package util.prediction;

import model.Pipeline;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

/**
 * Created by Manoj on 10/24/2016.
 */
public abstract class PredictionPipeline {
    public Pipeline pipeline = null;
    public String textToClassify = null;
    public AbstractClassifier classifier = null;
    protected Instances data;

    public abstract void loadModel();

    public abstract void createDataFrame();

    public abstract String classify();

    public String execute(Pipeline pipeline, String text) {
        this.pipeline = pipeline;
        this.textToClassify = text;

        loadModel();
        createDataFrame();
        String results = classify();
        return results;
    }
}
