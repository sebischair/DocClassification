package model;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by mahabaleshwar on 10/26/2016.
 */
@Embedded
public class Label {
    private String name;
    private String path;

    public Label() {};

    public Label(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
