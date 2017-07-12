package weka.Classifiers;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.Filter;

/**
 * Created by mahabaleshwar on 7/12/2017.
 */
public class WekaFilteredClassifier {
    FilteredClassifier fc;
    String batchsize = "100";

    public WekaFilteredClassifier(Filter filter, AbstractClassifier classifer) {
        fc = new FilteredClassifier();
        init();
        setFilter(filter);
        setClassifier(classifer);
    }

    public void init() {
        fc.setBatchSize(batchsize);
        fc.setNumDecimalPlaces(2);
        fc.setDebug(true);
        fc.setDoNotCheckForModifiedClassAttribute(false);
    }

    public void setFilter(Filter filter) {
        fc.setFilter(filter);
    }

    public void setClassifier(AbstractClassifier classifer) {
        fc.setClassifier(classifer);
    }

    public FilteredClassifier getFC() {
        return fc;
    }
}
