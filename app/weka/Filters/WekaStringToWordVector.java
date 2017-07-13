package weka.Filters;

import weka.core.stemmers.NullStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Created by Manoj on 7/12/2017.
 */
public class WekaStringToWordVector {
    private StringToWordVector stw;
    private boolean tf = true;
    private boolean idf = true;
    private double periodicPruning = -1.0;
    private String attributeIndex = "first-last";
    private int minTermFrequency = 5;
    private int wordsToKeep = 1000;

    public void setTf(boolean tf) {
        this.tf = tf;
    }

    public void setIdf(boolean idf) {
        this.idf = idf;
    }

    public void setPeriodicPruning(double periodicPruning) {
        this.periodicPruning = periodicPruning;
    }

    public void setAttributeIndex(String attributeIndex) {
        this.attributeIndex = attributeIndex;
    }

    public void setMinTermFrequency(int minTermFrequency) {
        this.minTermFrequency = minTermFrequency;
    }

    public void setWordsToKeep(int wordsToKeep) {
        this.wordsToKeep = wordsToKeep;
    }

    public WekaStringToWordVector() {
        stw = new StringToWordVector();
        init();
    }

    public void init() {
        stw.setTFTransform(tf);
        stw.setIDFTransform(idf);
        stw.setPeriodicPruning(periodicPruning);
        stw.setAttributeIndices(attributeIndex);
        stw.setMinTermFreq(minTermFrequency);
        stw.setWordsToKeep(wordsToKeep);
        stw.setStemmer(new NullStemmer());
        stw.setTokenizer(WekaTokenizer.getDefaultWordTokenizer());
        stw.setDebug(false);
    }

    public StringToWordVector get() {
        return stw;
    }
}
