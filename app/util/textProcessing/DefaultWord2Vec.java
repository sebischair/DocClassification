package util.textProcessing;

import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import util.StaticFunctions;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultWord2Vec {
    public static Word2Vec get(Tokenizer tokenizer) {
        return new Word2Vec().setInputCol(tokenizer.getOutputCol()).setOutputCol(StaticFunctions.FEATURES);
    }
}
