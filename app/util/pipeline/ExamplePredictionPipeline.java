package util.pipeline;

import model.Label;
import play.Play;
import util.prediction.PredictionPipeline;
import weka.classifiers.AbstractClassifier;
import weka.core.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class ExamplePredictionPipeline extends PredictionPipeline {

    @Override
    public void loadModel() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(Play.application().getFile("/public/" + pipeline.getName())));
            System.out.println("Try to load model... from " + Play.application().getFile("/public/" + pipeline.getName()));
            long startTime = System.currentTimeMillis();
            Object obj = in.readObject();
            System.out.print("Load model done. " + (System.currentTimeMillis() - startTime) / 1000 + "s");
            in.close();

            if (obj instanceof AbstractClassifier) {
                classifier = (AbstractClassifier) obj;
            }
        } catch (FileNotFoundException e1) {
            System.err.println("Warning: File not found, retrain model...");
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found, retrain model...");
        } catch (IOException e) {
            System.err.println("Can't read object. retrain model...");
        }
    }

    @Override
    public void createDataFrame() {
        List<Label> labels = pipeline.getLabels();

        FastVector<String> fvNominalVal = new FastVector<>(labels.size());
        labels.forEach(label -> fvNominalVal.addElement(label.getName()));

        Attribute attribute1 = new Attribute("class", fvNominalVal);
        Attribute attribute2 = new Attribute("text", (FastVector<String>) null);

        FastVector<Attribute> fvWekaAttributes = new FastVector<>();
        fvWekaAttributes.addElement(attribute1);
        fvWekaAttributes.addElement(attribute2);

        data = new Instances("relation", fvWekaAttributes, 0);
        data.setClassIndex(0);

        Instance i = new DenseInstance(2);
        i.setValue(fvWekaAttributes.elementAt(1), textToClassify);
        data.add(i);

        System.out.println("===== Instance created with reference dataset =====");
        System.out.println(data);
    }

    @Override
    public String classify() {
        try {
            double pred = classifier.classifyInstance(data.instance(0));
            System.out.println("===== Classified instance =====");
            System.out.println("Class predicted: " + data.classAttribute().value((int) pred));
            return "Class predicted: " + pipeline.getLabels().get((int) pred).getName();
        } catch (Exception e) {
            System.out.println("Problem found when classifying the text");
        }
        return "Unable to classify the text.";
    }
}
