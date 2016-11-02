package util.mlLib;

import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.OneVsRest;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultOneVsRest {
    public static OneVsRest get() {
        LogisticRegression classifier = new LogisticRegression()
                .setMaxIter(10)
                .setTol(1E-6)
                .setFitIntercept(true);

        return new OneVsRest().setClassifier(classifier);
    }
}
