package util.textProcessing;

import org.apache.spark.ml.feature.Tokenizer;
import util.StaticFunctions;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class DefaultTokenizer {
    public static Tokenizer get() {
        return new Tokenizer().setInputCol(StaticFunctions.TEXT).setOutputCol(StaticFunctions.WORDS);
    }
}
