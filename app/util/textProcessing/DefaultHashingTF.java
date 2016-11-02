package util.textProcessing;

import org.apache.spark.ml.feature.HashingTF;
import util.StaticFunctions;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultHashingTF {
    public static HashingTF get(String outCol, int noOfFeatures) {
        noOfFeatures = ((noOfFeatures == 0) ? 5000 : noOfFeatures);
        return new HashingTF()
                .setNumFeatures(noOfFeatures)
                .setInputCol(outCol)
                .setOutputCol(StaticFunctions.FEATURES);
    }
}
