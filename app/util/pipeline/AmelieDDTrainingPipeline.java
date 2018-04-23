package util.pipeline;

import com.fasterxml.jackson.databind.node.ArrayNode;
import model.Label;
import model.amelie.Issue;
import util.StaticFunctions;
import util.training.TrainingPipeline;
import weka.Classifiers.ClassifierFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Manoj on 12/20/2017.
 */
public class AmelieDDTrainingPipeline extends TrainingPipeline {

    @Override
    public void init() {
        crossValidation = 10;
        classifer = new ClassifierFactory().get(pipeline.getClassifier());
    }

    @Override
    public Map getData(List<Label> labels) {
        Map map = new HashMap();
        labels.forEach(label -> map.put(label.getName(), new ArrayList<String>()));
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllDesignDecisions();
        issues.forEach(issue -> {
            String l;
            if(modelFileName.toLowerCase().contains("category")) {
                l = issue.get("decisionCategory") != null ? issue.get("decisionCategory").asText("") : "";
            } else {
                l = issue.get("designDecision") != null ? issue.get("designDecision").asText("") : "";
            }
            String text = "";
            if(issue.has("summary")) text = issue.get("summary").asText("");
            if(issue.has("description")) text = text + " " + issue.get("description").asText("");
            if(map.containsKey(l) && text != "")
                ((ArrayList) map.get(l)).add(StaticFunctions.removeStopWords(text).replaceAll("class", ""));
        });
        return map;
    }
}
