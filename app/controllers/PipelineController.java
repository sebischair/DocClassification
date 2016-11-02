package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Pipeline;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;
import util.pipeline.ExamplePredictionPipeline;
import util.pipeline.ExampleTrainingPipeline;

import java.util.Date;

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
        ObjectNode results = Json.newObject();
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String documentsPath = request().body().asJson().get("documentsPath").asText();
        Pipeline pipeline = StaticFunctions.getPipeline(pipelineName);

        results.put("status", "OK");
        ArrayNode result = new ExamplePredictionPipeline().execute(pipeline, documentsPath);
        results.put("result", result);
        return ok(results);
    }

}
