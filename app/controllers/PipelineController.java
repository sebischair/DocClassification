package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import model.Classifier;
import model.Pipeline;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.HelperService;
import util.StaticFunctions;
import util.pipeline.AmelieDDTrainingPipeline;
import util.pipeline.ExamplePredictionPipeline;
import util.pipeline.ExampleTrainingPipeline;

import java.io.File;
import java.util.Date;

/**
 * Created by Manoj on 10/26/2016.
 */
public class PipelineController extends Controller {
    @Inject
    WSClient ws;

    public Result get(String name) {
        ObjectNode result = Json.newObject();
        Pipeline pipeline = StaticFunctions.getPipeline(name);
        if (pipeline != null) {
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

    public Result getClassifiers() {
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("result", StaticFunctions.deserializeToJSON(new Classifier().getAll()));
        return ok(result);
    }

    public Result create(String name) {
        ObjectNode result = Json.newObject();
        Pipeline pipeline = new Pipeline();
        pipeline.setName(name);
        pipeline.setCreatedAt(new Date());
        pipeline.save();
        result.put("status", "OK");
        result.put("result", StaticFunctions.deserializeToJSON(new Pipeline().getAll()));
        return ok(result);
    }

    public Result remove(String name) {
        ObjectNode result = Json.newObject();
        Pipeline pipeline = StaticFunctions.getPipeline(name);
        if (pipeline != null) {
            pipeline.delete();
        } else {
            result.put("status", "KO");
        }
        result.put("status", "OK");
        result.put("result", StaticFunctions.deserializeToJSON(new Pipeline().getAll()));
        return ok(result);
    }

    public Result setClassifier() {
        ObjectNode results = Json.newObject();
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String classifierName = request().body().asJson().get("classifierName").asText();
        boolean result = new Pipeline().updateClassifier(pipelineName, classifierName);
        results.put("result", result);
        return ok(results);
    }

    public Result train(String pipelineName) {
        ObjectNode results = Json.newObject();
        Pipeline pipeline = StaticFunctions.getPipeline(pipelineName);
        ObjectNode result = new AmelieDDTrainingPipeline().run(pipeline);
        results.put("status", "OK");
        results.put("result", result);
        return ok(results);
    }

    public Result predict() {
        ObjectNode results = Json.newObject();
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String text = request().body().asJson().get("textToClassify").asText();
        Pipeline pipeline = StaticFunctions.getPipeline(pipelineName);
        String result = new ExamplePredictionPipeline().execute(pipeline, text);
        results.put("status", "OK");
        results.put("result", result);
        return ok(results);
    }

    public Result predictAndClassify() {
        ObjectNode results = Json.newObject();
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String fileId = request().body().asJson().get("fileId").asText();
        String filePath = HelperService.SC_BASE_URL + "files/" + fileId;
        Pipeline pipeline = StaticFunctions.getPipeline(pipelineName);
        String result = new ExamplePredictionPipeline().execute(pipeline, filePath);
        /*for(int i=0; i<result.size(); i++) {
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
        }*/
        results.put("status", "OK");
        results.put("result", result);
        return StaticFunctions.jsonResult(ok(results));
    }

    public Result datasetUpload(){
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> dataset = body.getFile("file");
        if (dataset != null) {
            String fileName = dataset.getFilename();
            File file = dataset.getFile();
            String newPath = "myresources/datasets/"+fileName;
            file.renameTo(new File(newPath));
            return ok(Json.parse("{ \"results\": { \"path\": \"" + newPath+ " \"}}"));
        } else{
            return ok();
        }
    }

    public Result updatePipeline() {
        ObjectNode results = Json.newObject();
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String fileName = request().body().asJson().get("fileName").asText();
        String filePath = "myresources/datasets/" + fileName;
        boolean result = new Pipeline().updateFilePath(pipelineName, filePath);
        results.put("result", result);
        return ok(results);
    }
}
