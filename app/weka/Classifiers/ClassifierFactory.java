package weka.Classifiers;

import weka.classifiers.AbstractClassifier;

/**
 * Created by Manoj on 7/12/2017.
 */
public class ClassifierFactory {

    public AbstractClassifier get(String classifierName) {
        AbstractClassifier classifier;
        switch (classifierName) {
            case "NaiveBayes":
                classifier = new NaiveBayesClassifier().get();
                break;
            default:
                classifier = getDefaultClassifier();
        }
        return classifier;
    }

    public AbstractClassifier getDefaultClassifier() {
        return new LibSVMClassifier().get();
    }
}