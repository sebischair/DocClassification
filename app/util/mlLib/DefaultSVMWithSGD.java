package util.mlLib;

import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.optimization.L1Updater;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultSVMWithSGD {
    public static SVMWithSGD get() {
        SVMWithSGD svmAlg = new SVMWithSGD();
        svmAlg.optimizer()
                .setNumIterations(200)
                .setRegParam(0.1)
                .setUpdater(new L1Updater());
        return svmAlg;
    }
}
