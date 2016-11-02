package util.mlLib;

import org.apache.spark.ml.classification.RandomForestClassifier;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultRandomForestClassifier {
    public static RandomForestClassifier get() {
        return new RandomForestClassifier();
    }
}
