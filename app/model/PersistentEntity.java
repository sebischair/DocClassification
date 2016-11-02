package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.MorphiaObject;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * Created by mahabaleshwar on 10/26/2016.
 */
public class PersistentEntity {

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public void delete() {
        MorphiaObject.datastore.delete(this);
    }

    public PersistentEntity findByName(String fieldName, String fieldValue) {
        List<? extends PersistentEntity> entities = MorphiaObject.datastore.createQuery(this.getClass()).field(fieldName).equalIgnoreCase(fieldValue).asList();
        if(entities.size() > 0) return entities.get(0);
        return null;
    }

    public List<? extends PersistentEntity> getAll() {
        final Query<? extends PersistentEntity> query = MorphiaObject.datastore.createQuery(this.getClass());
        return query.asList();
    }

    public String getJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
