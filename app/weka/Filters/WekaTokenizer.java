package weka.Filters;

import weka.core.tokenizers.WordTokenizer;

/**
 * Created by mahabaleshwar on 7/12/2017.
 */
public class WekaTokenizer {
    public static WordTokenizer getDefaultWordTokenizer() {
        WordTokenizer wt = new WordTokenizer();
        wt.setDelimiters(" \r \t.,;:\'\"()?!");
        return wt;
    }
}
