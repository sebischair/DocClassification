package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by Manoj on 7/13/2017.
 */
@Entity("classifiers")
@Indexes(@Index(value = "name", fields = @Field("name")))
public class Classifier extends  PersistentEntity {
    @Id
    private ObjectId _id;

    @Indexed
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
