package weka.Classifiers;

import weka.classifiers.AbstractClassifier;

/**
 * Created by mahabaleshwar on 7/12/2017.
 */
public class ClassifierFactory {

    public AbstractClassifier get() {
        LibSVMClassifier libsvm = new LibSVMClassifier();
        libsvm.init();
        return libsvm.get();
    }

}
