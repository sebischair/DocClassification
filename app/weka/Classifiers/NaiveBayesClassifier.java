package weka.Classifiers;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.RandomizableClassifier;
import weka.classifiers.bayes.NaiveBayes;

/**
 * Created by mahabaleshwar on 7/12/2017.
 */
public class NaiveBayesClassifier {
    private NaiveBayes nb;

    void init() {
        nb = new NaiveBayes();
        nb.setNumDecimalPlaces(2);
    }

    public AbstractClassifier get() {
        return nb;
    }
}
