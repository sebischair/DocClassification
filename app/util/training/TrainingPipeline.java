package util.training;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Label;
import model.Pipeline;
import play.Logger;
import play.Play;
import play.libs.Json;
import weka.Classifiers.WekaFilteredClassifier;
import weka.Filters.WekaStringToWordVector;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Manoj on 10/24/2016.
 */
public abstract class TrainingPipeline {
    public Pipeline pipeline;

    public int crossValidation;
    public Instances trainingData;
    public WekaFilteredClassifier wfc;
    public String modelFileName;
    public AbstractClassifier classifer;

    public abstract void init();
    public abstract Map getData(List<Label> labels);

    public void load() {
        modelFileName = pipeline.getName();
        List<Label> labels = pipeline.getLabels();

        FastVector<String> fvNominalVal = new FastVector<>(labels.size());
        labels.forEach(label -> fvNominalVal.addElement(label.getName()));

        Attribute attribute1 = new Attribute("class", fvNominalVal);
        Attribute attribute2 = new Attribute("text", (FastVector<String>) null);

        FastVector<Attribute> fvWekaAttributes = new FastVector<>();
        fvWekaAttributes.addElement(attribute1);
        fvWekaAttributes.addElement(attribute2);

        trainingData = new Instances("relation", fvWekaAttributes, 0);
        trainingData.setClassIndex(0);

        Map dataMap = getData(labels);
        dataMap.forEach((key, values) -> {
            ArrayList entities = (ArrayList) values;
            entities.forEach(e -> {
                Instance i = new DenseInstance(2);
                i.setValue(fvWekaAttributes.elementAt(0), (String) key);
                i.setValue(fvWekaAttributes.elementAt(1), (String) e);
                trainingData.add(i);
            });
        });

        Logger.info("===== Instance created with reference dataset =====");
        Logger.info(trainingData.toString());
    }

    public void process() {
        try {
            wfc = new WekaFilteredClassifier(new WekaStringToWordVector().get(), classifer);
            wfc.getFC().buildClassifier(trainingData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObjectNode evaluate() {
        try {
            ObjectNode result = Json.newObject();
            Evaluation eval = new Evaluation(trainingData);
            eval.crossValidateModel(wfc.getFC(), trainingData, crossValidation, new Random(1));
            result.put("fscore", eval.weightedFMeasure());
            result.put("recall", eval.weightedRecall());
            result.put("precision", eval.weightedPrecision());
            result.put("correctInstances", eval.correct());
            result.put("inCorrectInstances", eval.incorrect());
            result.put("pctCorrectInstances", eval.pctCorrect());
            result.put("pctInCorrectInstances", eval.pctIncorrect());
            result.put("totalInstances", eval.numInstances());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Play.application().getFile("myresources/" + modelFileName)));
            out.writeObject(wfc.getFC());
            out.close();
            Logger.info("===== Saved model: " + modelFileName + " =====");
        } catch (IOException e) {
            Logger.info("Problem found when writing: " + modelFileName);
        }
    }

    public ObjectNode run(Pipeline pipeline) {
        this.pipeline = pipeline;
        init();
        load();
        process();
        ObjectNode result = evaluate();
        save();
        return result;
    }
}
