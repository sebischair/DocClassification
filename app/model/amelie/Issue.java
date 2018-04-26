package model.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.AmelieMongoClient;
import org.bson.Document;
import play.libs.Json;

/**
 * Created by Manoj on 11/28/2017.
 */
public class Issue {
    private MongoCollection<Document> issueCollection;

    public Issue() {
        issueCollection = AmelieMongoClient.amelieDatabase.getCollection("issues");
    }

    public ArrayNode findAllIssues() {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("fields.project.key", "AMELIE")).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(Json.toJson(cursor.next())));
        }
        return issues;
    }

    public ArrayNode findAllDesignDecisions() {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("amelie.designDecision", new BasicDBObject("$exists", true))).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(Json.toJson(cursor.next())));
        }
        return issues;
    }

    private ObjectNode getIssueDetails(JsonNode obj) {
        ObjectNode issue = Json.newObject();
        issue.put("name", obj.get("name"));
        if(obj.has("fields")) {
            JsonNode fields = obj.get("fields");
            String summary = fields.get("summary") != null ? fields.get("summary").asText("") : "";
            issue.put("summary", summary);
            String description = fields.get("description") != null ? fields.get("description").asText("") : "";
            issue.put("description", description);
        }
        if(obj.has("amelie")) {
            JsonNode amelie = obj.get("amelie");
            issue.put("designDecision", amelie.get("designDecision") != null ? amelie.get("designDecision").asText("") : "");
            issue.put("decisionCategory", amelie.get("decisionCategory") != null ? amelie.get("decisionCategory").asText("") : "");
        }
        return issue;
    }

    public void updateIssueByKey(String key, BasicDBObject newConcepts) {
        issueCollection.updateOne(new BasicDBObject().append("name", key), newConcepts);
    }
}
