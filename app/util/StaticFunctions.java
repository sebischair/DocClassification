package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import controllers.MorphiaObject;
import model.Label;
import model.Pipeline;
import play.mvc.Result;

import java.util.*;

/**
 * Created by Manoj on 10/24/2016.
 */
public class StaticFunctions {
    public static String LIBSVM = "LibSVM";
    public static String NAIVEBAYES = "NaiveBayes";
    public static String LABEL = "label";
    public static String TEXT = "text";
    public static String FEATURES = "features";
    public static String WORDS = "words";
    public static String[] STOPWORDS = {"a", "an", "and", "the", "that", "they", "which", "we", "us", "i", "me", "like", "that", "only", "much", "kafka", "it", "is", "if", "by", "basically", "are", "as", "but", "those", "spark", "also"};
    public static final Set<String> STOPWORDS_SET = new HashSet<String>(Arrays.asList(STOPWORDS));

    public static boolean tagValuesMatch(JsonNode entityAttributes, String tag, Label label) {
        if(entityAttributes == null) return false;
        for (int j = 0; j < entityAttributes.size(); j++) {
            JsonNode entityAttribute = entityAttributes.get(j);
            if (entityAttribute.get("name").asText("").equals(tag)) {
                JsonNode jsonValue = entityAttribute.get("values");
                if (jsonValue.size() > 0) {
                    if (label.getType().equals("Boolean")) {
                        Boolean value = jsonValue.get(0).asBoolean(false);
                        return (value && label.getName().equals("1")) || (!value && label.getName().equals("0"));
                    } else {
                        return jsonValue.get(0).get("name").asText("").equalsIgnoreCase(label.getName());
                    }
                }
            }
        }
        return false;
    }

    public static String getStringValueFromSCObject(JsonNode entityAttributes, String attributeName) {
        for (int j = 0; j < entityAttributes.size(); j++) {
            JsonNode attr = entityAttributes.get(j);
            if (attr.get("name").asText("").equals(attributeName)) {
                JsonNode jsonValue = attr.get("values");
                if (jsonValue.size() > 0) {
                    return jsonValue.get(0).asText("");
                }
            }
        }

        return null;
    }

    public static String deserializeToJSON(List<?> objList, String... removeAttributes) {
        List<DBObject> dbObjList = new ArrayList<>(objList.size());
        DBObject dbObj;
        for (Object obj : objList) {
            dbObj = MorphiaObject.morphia.toDBObject(obj);
            for (String removeAttribute : removeAttributes) {
                dbObj.removeField(removeAttribute);
            }
            dbObjList.add(dbObj);
        }
        return JSON.serialize(dbObjList);
    }

    public static String deserializeToJSON(Object obj, String... removeAttributes) {
        DBObject dbObj;
        dbObj = MorphiaObject.morphia.toDBObject(obj);
        for (String removeAttribute : removeAttributes) {
            dbObj.removeField(removeAttribute);
        }
        return JSON.serialize(dbObj);
    }

    public static Pipeline getPipeline(String pipelineName) {
        return (Pipeline) new Pipeline().findByName("name", pipelineName);
    }

    public static Result jsonResult(Result httpResponse) {
        return httpResponse.as("application/json; charset=utf-8");
    }

    public static String removeStopWords(String text) {
        text = text.toLowerCase();
        for (String s : STOPWORDS) {
            text = text.replaceAll("\\b" + s + "\\b", "");
        }
        return text.replaceAll("[()]", "method");
    }

    public static List<String> parseLine(String cvsLine, char separators, char customQuote) {
        List<String> result = new ArrayList<>();
        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }
        if (customQuote == ' ') {
            customQuote = '"';
        }
        if (separators == ' ') {
            separators = ',';
        }
        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;
        char[] chars = cvsLine.toCharArray();
        for (char ch : chars) {
            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {
                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }
                }
            } else {
                if (ch == customQuote) {
                    inQuotes = true;
                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }
                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }
                } else if (ch == separators) {
                    result.add(curVal.toString());
                    curVal = new StringBuffer();
                    startCollectChar = false;
                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }
        }
        result.add(curVal.toString());
        return result;
    }
}
