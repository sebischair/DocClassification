package util.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import model.Label;
import play.libs.ws.WSClient;
import services.HelperService;
import util.StaticFunctions;
import util.training.TrainingPipeline;
import weka.Classifiers.ClassifierFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static play.mvc.Results.ok;
import static util.StaticFunctions.getStringValueFromSCObject;

/**
 * Created by Manoj on 10/24/2016.
 */
public class ExampleTrainingPipeline extends TrainingPipeline {
    WSClient ws;

    public ExampleTrainingPipeline(WSClient ws) {
        this.ws = ws;
    }

    @Override
    public void init() {
        crossValidation = 10;
        classifer = new ClassifierFactory().get(pipeline.getClassifier());
    }

    @Override
    public Map getData(List<Label> labels) {
        Map map = new HashMap();
        labels.forEach(label -> map.put(label.getName(), new ArrayList<String>()));
        if(pipeline.getFilePath() != null || pipeline.getFilePath() != "") {
            try {
                Scanner scanner = new Scanner(new File(pipeline.getFilePath()));
                scanner.useDelimiter(",");
                while(scanner.hasNext()) {
                    List<String> line = StaticFunctions.parseLine(scanner.nextLine(), ',', '"');
                    if(line.size() == 2) {
                        String l = line.get(0);
                        String t = line.get(1);
                        if(l != null && l != "" && map.containsKey(l) && t != "") ((ArrayList) map.get(l)).add(t.replaceAll("class", ""));
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            List<String> miningAttributes = pipeline.getMiningAttributes();
            HelperService hs = new HelperService(ws);
            hs.entitiesForPath(labels.get(0).getPath() + "/entities").thenApply(entityObject -> {
                entityObject.forEach(entity -> {
                    hs.entityForUid(entity.get("id").asText()).thenApply(e -> {
                        JsonNode entityAttributes = e.get("attributes");
                        String text = "";
                        for (int j = 0; j < labels.size(); j++) {
                            Label label = labels.get(j);
                            if (StaticFunctions.tagValuesMatch(entityAttributes, pipeline.getTag(), label)) {
                                for (String miningAttribute : miningAttributes) {
                                    String textValue = getStringValueFromSCObject(entityAttributes, miningAttribute);
                                    if (textValue != null) text += " " + textValue;
                                }
                                if (text != "") ((ArrayList) map.get(label.getName())).add(text.replaceAll("class", ""));
                            }
                        }
                        return ok();
                    }).toCompletableFuture().join();
                });
                return ok();
            }).toCompletableFuture().join();
        }
        return map;
    }
}