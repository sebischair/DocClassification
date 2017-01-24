package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Label;
import model.Pipeline;
import org.apache.commons.io.FileUtils;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.RestCaller;
import util.SparkSingleton;
import util.StaticFunctions;
import util.pipeline.ExamplePredictionPipeline;
import util.pipeline.ExampleTrainingPipeline;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by mahabaleshwar on 10/26/2016.
 */
public class PipelineController extends Controller {

    public Result get(String name) {
        ObjectNode result = Json.newObject();
        Pipeline pipeline = StaticFunctions.getPipeline(name);
        if(pipeline != null) {
            result.put("status", "OK");
            result.put("result", StaticFunctions.deserializeToJSON(pipeline));
        } else {
            result.put("status", "KO");
            result.put("result", "{}");
        }

        return ok(result);
    }

    public Result getAll() {
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("result", StaticFunctions.deserializeToJSON(new Pipeline().getAll()));
        return ok(result);
    }

    public Result create(String name) {
        ObjectNode result = Json.newObject();
        result.put("status", "OK");

        Pipeline pipeline = new Pipeline();
        pipeline.setName(name);
        pipeline.setCreatedAt(new Date());
        pipeline.save();
        result.put("result", StaticFunctions.deserializeToJSON(new Pipeline().getAll()));

        return ok(result);
    }

    public Result remove(String name) {
        ObjectNode result = Json.newObject();
        result.put("status", "OK");

        Pipeline pipeline = StaticFunctions.getPipeline(name);
        if(pipeline != null) {
            pipeline.delete();
        } else {
            result.put("status", "KO");
        }

        result.put("result", StaticFunctions.deserializeToJSON(new Pipeline().getAll()));
        return ok(result);
    }

    public Result train(String pipelineName) {
        ObjectNode results = Json.newObject();
        Pipeline pipeline = StaticFunctions.getPipeline(pipelineName);

        ObjectNode result = new ExampleTrainingPipeline().execute(pipeline);
        results.put("status", "OK");
        results.put("result", result);

        String model_path = "sparkModels/" + pipelineName;
        new Pipeline().updateModelPath(pipelineName, model_path);

        return ok(results);
    }

    public Result predict() {
        SparkSingleton.getInstance();
        ObjectNode results = Json.newObject();
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String documentsPath = request().body().asJson().get("documentsPath").asText();
        Pipeline pipeline = StaticFunctions.getPipeline(pipelineName);

        results.put("status", "OK");
        ArrayNode result = new ExamplePredictionPipeline().execute(pipeline, documentsPath);
        results.put("result", result);
        cleanUpTempFolder();
        return ok(results);
    }

    public Result predictAndClassify() {
        SparkSingleton.getInstance();
        ObjectNode results = Json.newObject();
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String fileId = request().body().asJson().get("fileId").asText();
        String filePath = RestCaller.API_BASE_URL + "files/" + fileId;
        Pipeline pipeline = StaticFunctions.getPipeline(pipelineName);

        results.put("status", "OK");
        ArrayNode result = new ExamplePredictionPipeline().execute(pipeline, filePath);

        for(int i=0; i<result.size(); i++) {
            JsonNode r = result.get(i);
            List<Label> labels = pipeline.getLabels();
            if(labels.size() > 0 && r.has("prediction") && labels.size() >= r.get("prediction").asInt()) {
                Label label = labels.get(r.get("prediction").asInt());
                String entityId = label.getPath().split("entities/")[1];
                RestCaller.moveFile(filePath, entityId);
                System.out.println(filePath);
                System.out.println(entityId);
                System.out.println("....................");
            }
        }

        results.put("result", result);
        cleanUpTempFolder();
        return ok(results);
    }

    private void cleanUpTempFolder() {
        try {
            FileUtils.deleteDirectory(new File(play.Play.application().path().getAbsolutePath() + "/tmp/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
