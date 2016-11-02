package util.mlLib;

import org.apache.spark.ml.classification.DecisionTreeClassifier;
import util.StaticFunctions;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultDecisionTreeClassifier {
    public static DecisionTreeClassifier get() {
        return new DecisionTreeClassifier()
                .setLabelCol(StaticFunctions.LABEL)
                .setFeaturesCol(StaticFunctions.FEATURES);
    }
}
