package model;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.Date;
import java.util.List;

/**
 * Created by mahabaleshwar on 10/26/2016.
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

    private List<String> stages;

    private int split;

    private String modelPath;

    private Date createdAt;

    public Pipeline() { }

    public Pipeline(String name, List<Label> labels, int split, List<String> stages, String model_path, Date created_at) {
        this.name = name;
        this.labels = labels;
        this.split = split;
        this.stages = stages;
        this.modelPath = model_path;
        this.createdAt = created_at;
    }

    public boolean updateModelPath(String pipelineName, String modelPath) {
        try {
            Query<Pipeline> query = (Query<Pipeline>) MorphiaObject.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).set("modelPath", modelPath);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addNewLabel(String pipelineName, String labelName, String labelPath, String labelId, String labelType) {
        try {
            Label label = new Label(labelName, labelPath, labelId, labelType);
            Query<Pipeline> query = (Query<Pipeline>) MorphiaObject.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).add("labels", label, false);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return false;
    }

    public boolean removeLabel(String pipelineName, String labelName, String labelPath, String labelId, String labelType) {
        try {
            Label label = new Label(labelName, labelPath, labelId, labelType);
            Query<Pipeline> query = (Query<Pipeline>) MorphiaObject.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(pipelineName);
            UpdateOperations<Pipeline> ops = (UpdateOperations<Pipeline>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).removeAll("labels", label);
            MorphiaObject.datastore.update(query, ops);
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
}
