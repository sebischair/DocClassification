package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Pipeline;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

/**
 * Created by mahabaleshwar on 10/27/2016.
 */
public class LabelController extends Controller {

    public Result create() {
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String labelName = request().body().asJson().get("labelName").asText();
        String labelPath = request().body().asJson().get("labelPath").asText();

        ObjectNode result = Json.newObject();
        if(new Pipeline().addNewLabel(pipelineName, labelName, labelPath)) {
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

        ObjectNode result = Json.newObject();
        if(new Pipeline().removeLabel(pipelineName, labelName, labelPath)) {
            result.put("status", "OK");
            result.put("result", StaticFunctions.deserializeToJSON(StaticFunctions.getPipeline(pipelineName)));
        } else {
            result.put("status", "KO");
            result.put("result", "{}");
        }
        return ok(result);
    }

}
