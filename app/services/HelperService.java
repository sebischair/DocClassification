package services;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;

public class HelperService {
    WSClient ws;
    public static String SC_BASE_URL = "https://server.sociocortex.com/api/v1/";
    public static String USER_NAME = "";
    public static String PASSWORD = "";

    public HelperService(WSClient ws) {
        this.ws = ws;
    }

    public CompletionStage<JsonNode> getWSResponseWithAuth(String url) {
        return ws.url(url).setAuth(USER_NAME, PASSWORD, WSAuthScheme.BASIC).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> getWSResponse(String url) {
        return ws.url(url).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> postWSRequest(String url, JsonNode json) {
        return ws.url(url).post(json).thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> entitiesForPath(String path) {
        System.out.println(path);
        return getWSResponse(path);
    }

    public CompletionStage<JsonNode> entitiesForTypeUid(String typeId) {
        return getWSResponse(SC_BASE_URL + "entityTypes/" + typeId + "/entities");
    }

    public CompletionStage<JsonNode> entityForUid(String entityId) {
        return getWSResponse(SC_BASE_URL + "entities/" + entityId);
    }

    public CompletionStage<JsonNode> executeMxl(String workspaceId, String expression) {
        String url = SC_BASE_URL + "workspaces/" + workspaceId + "/mxlQuery";
        JsonNode json = Json.newObject().put("expression", expression);
        return postWSRequest(url, json);
    }
}
