package weka.Classifiers;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.LibSVM;
import weka.core.SelectedTag;

/**
 * Created by mahabaleshwar on 7/9/2017.
 */
public class LibSVMClassifier {
    private LibSVM svm;

    public void init() {
        svm = new LibSVM();
        svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
        svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
        svm.setBatchSize("100");
        svm.setCacheSize(40.0);
        svm.setCoef0(0.0);
        svm.setCost(1.0);
        svm.setDegree(3);
        svm.setSeed(1);
        svm.setEps(0.001);
        svm.setGamma(0.0);
        svm.setLoss(0.1);
        svm.setNormalize(false);
        svm.setNu(0.5);
        svm.setNumDecimalPlaces(2);
        svm.setDebug(false);
        svm.setShrinking(true);
        svm.setProbabilityEstimates(false);
        svm.setDoNotCheckCapabilities(false);
        svm.setDoNotReplaceMissingValues(false);
    }

    public AbstractClassifier get() {
        return svm;
    }
}
