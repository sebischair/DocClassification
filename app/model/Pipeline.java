package model;

import db.DefaultMongoClient;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Manoj on 10/26/2016.
 */
@Entity("pipelines")
@Indexes(@Index(value = "name", fields = @Field("name")))
public class Pipeline extends  PersistentEntity {
    @Id
    private ObjectId _id;

    @Indexed
    private String name;

    @Embedded
    List<Label> labels;

    private String classifier;

    private List<String> miningAttributes;

    private int split;

    private String modelPath;

    private Date createdAt;

    private String tag;

    private String filePath;

    public Pipeline() { }

    public Pipeline(String name, List<Label> labels, int split, List<String> stages, String model_path, Date created_at) {
        this.name = name;
        this.labels = labels;
        this.split = split;
        this.modelPath = model_path;
        this.createdAt = created_at;
    }

    public boolean updateClassifier(String pipelineName, String classifier) {
        try {
            Query<Pipeline> query = (Query<Pipeline>) DefaultMongoClient.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) DefaultMongoClient.datastore.createUpdateOperations(this.getClass()).set("classifier", classifier);
            DefaultMongoClient.datastore.update(query, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateFilePath(String pipelineName, String filePath) {
        try {
            Query<Pipeline> query = (Query<Pipeline>) DefaultMongoClient.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) DefaultMongoClient.datastore.createUpdateOperations(this.getClass()).set("filePath", filePath);
            DefaultMongoClient.datastore.update(query, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateModelPath(String pipelineName, String modelPath) {
        try {
            Query<Pipeline> query = (Query<Pipeline>) DefaultMongoClient.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) DefaultMongoClient.datastore.createUpdateOperations(this.getClass()).set("modelPath", modelPath);
            DefaultMongoClient.datastore.update(query, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addNewLabel(String pipelineName, String labelName, String labelPath, String labelId, String labelType) {
        try {
            Label label = new Label(labelName, labelPath, labelId, labelType);
            Query<Pipeline> query = (Query<Pipeline>) DefaultMongoClient.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) DefaultMongoClient.datastore.createUpdateOperations(this.getClass()).add("labels", label, false);
            DefaultMongoClient.datastore.update(query, ops);
            return true;
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return false;
    }

    public boolean removeLabel(String pipelineName, String labelName, String labelPath, String labelId, String labelType) {
        try {
            Label label = new Label(labelName, labelPath, labelId, labelType);
            Query<Pipeline> query = (Query<Pipeline>) DefaultMongoClient.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) DefaultMongoClient.datastore.createUpdateOperations(this.getClass()).removeAll("labels", label);
            DefaultMongoClient.datastore.update(query, ops);
            return true;
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return false;
    }

    public boolean updateMiningAttributes(String pipelineName, ArrayList<String> miningAttributes) {
        try {
            Query<Pipeline> query = (Query<Pipeline>) DefaultMongoClient.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) DefaultMongoClient.datastore.createUpdateOperations(this.getClass()).set("miningAttributes", miningAttributes);
            DefaultMongoClient.datastore.update(query, ops);
            return true;
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return false;
    }

    public boolean updateLabel(String pipelineName, String tag) {
        try {
            Query<Pipeline> query = (Query<Pipeline>) DefaultMongoClient.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) DefaultMongoClient.datastore.createUpdateOperations(this.getClass()).set("tag", tag);
            DefaultMongoClient.datastore.update(query, ops);
            return true;
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return false;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Label> getLabels() {
        return this.labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public int getSplit() {
        return split;
    }

    public void setSplit(int split) {
        this.split = split;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getMiningAttributes() {
        return miningAttributes;
    }

    public void setMiningAttributes(List<String> miningAttributes) {
        this.miningAttributes = miningAttributes;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
