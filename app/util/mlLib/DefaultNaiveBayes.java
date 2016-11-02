package util.mlLib;


import org.apache.spark.ml.classification.NaiveBayes;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultNaiveBayes {
    public static NaiveBayes get() {
        return new NaiveBayes();
    }
}
