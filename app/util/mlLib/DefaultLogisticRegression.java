package util.mlLib;

import org.apache.spark.ml.classification.LogisticRegression;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultLogisticRegression {
    public static LogisticRegression get() {
        return new LogisticRegression().setMaxIter(10).setRegParam(0.001).setElasticNetParam(0.8);
    }
}
