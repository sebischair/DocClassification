package util.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import model.Label;
import play.Logger;
import play.Play;
import play.libs.ws.WSClient;
import services.HelperService;
import util.StaticFunctions;
import util.training.TrainingPipeline;
import weka.Classifiers.ClassifierFactory;
import weka.Classifiers.WekaFilteredClassifier;
import weka.Filters.WekaStringToWordVector;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

import static play.mvc.Results.ok;
import static util.StaticFunctions.getStringValueFromSCObject;

/**
 * Created by Manoj on 10/24/2016.
 */
public class ExampleTrainingPipeline extends TrainingPipeline {
    WSClient ws;
    private AbstractClassifier classifer;
    private WekaFilteredClassifier wfc;
    private String modelFileName;
    private int crossValidation;
    private Instances trainingData;

    public ExampleTrainingPipeline(WSClient ws) {
        this.ws = ws;
    }

    @Override
    public void init() {
        crossValidation = 10;
        classifer = new ClassifierFactory().get();
    }

    @Override
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

    @Override
    public void process() {
        try {
            wfc = new WekaFilteredClassifier(new WekaStringToWordVector().get(), classifer);
            wfc.getFC().buildClassifier(trainingData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String evaluate() {
        try {
            Evaluation eval = new Evaluation(trainingData);
            eval.crossValidateModel(wfc.getFC(), trainingData, crossValidation, new Random(1));
            return eval.toSummaryString("\nResults\n======\n", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void save() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Play.application().getFile("/public/" + modelFileName)));
            out.writeObject(wfc.getFC());
            out.close();
            Logger.info("===== Saved model: " + modelFileName + " =====");
        } catch (IOException e) {
            Logger.info("Problem found when writing: " + modelFileName);
        }
    }

    private Map getData(List<Label> labels) {
        Map map = new HashMap();
        List<String> miningAttributes = pipeline.getMiningAttributes();
        labels.forEach(label -> map.put(label.getName(), new ArrayList<String>()));

        HelperService hs = new HelperService(ws);
        System.out.println(labels.get(0).getPath() + "/entities");
        hs.entitiesForPath(labels.get(0).getPath() + "/entities").thenApply(entityObject -> {
            entityObject.forEach(entity -> {
                hs.entityForUid(entity.get("id").asText("")).thenApply(e -> {
                    JsonNode entityAttributes = e.get("attributes");
                    String text = "";
                    for (int j = 0; j < labels.size(); j++) {
                        Label label = labels.get(j);
                        if (StaticFunctions.tagValuesMatch(entityAttributes, pipeline.getTag(), label)) {
                            for (String miningAttribute : miningAttributes) {
                                String textValue = getStringValueFromSCObject(entityAttributes, miningAttribute);
                                if (textValue != null) text += " " + textValue;
                            }
                            if (text != "") ((ArrayList) map.get(label.getName())).add(text.replaceAll("class", ""));
                        }
                    }
                    return ok();
                }).toCompletableFuture().join();
            });
            return ok();
        }).toCompletableFuture().join();

        return map;
    }
}
