package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Pipeline;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj on 10/27/2016.
 */
public class LabelController extends Controller {

    public Result create() {
        JsonNode jo = request().body().asJson();
        String pipelineName = jo.get("pipelineName").asText();
        String labelName = jo.get("labelName").asText();
        String labelPath = jo.has("labelPath") ? jo.get("labelPath").asText("") : "";
        String labelId = jo.get("labelId").asText("");
        String labelType = jo.get("labelType").asText("");
        String label = jo.has("label") ? jo.get("label").asText("") : "";

        List<JsonNode> miningAttributes = jo.findValues("miningAttributes");
        ArrayList<String> attributes = new ArrayList<>();
        if (miningAttributes.size() == 1) {
            for (JsonNode ma : miningAttributes.get(0)) {
                attributes.add(ma.asText());
            }
            new Pipeline().updateMiningAttributes(pipelineName, attributes);
        }

        new Pipeline().updateLabel(pipelineName, label);

        ObjectNode result = Json.newObject();
        if (new Pipeline().addNewLabel(pipelineName, labelName, labelPath, labelId, labelType)) {
            result.put("status", "OK");
            result.put("result", StaticFunctions.deserializeToJSON(StaticFunctions.getPipeline(pipelineName)));
        } else {
            result.put("status", "KO");
            result.put("result", "{}");
        }
        return ok(result);
    }

    public Result remove() {
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String labelName = request().body().asJson().get("labelName").asText();
        String labelPath = request().body().asJson().get("labelPath").asText();
        String labelId = request().body().asJson().get("labelId").asText();
        String labelType = request().body().asJson().get("labelType").asText();

        ObjectNode result = Json.newObject();
        if (new Pipeline().removeLabel(pipelineName, labelName, labelPath, labelId, labelType)) {
            result.put("status", "OK");
            result.put("result", StaticFunctions.deserializeToJSON(StaticFunctions.getPipeline(pipelineName)));
        } else {
            result.put("status", "KO");
            result.put("result", "{}");
        }
        return ok(result);
    }
}
