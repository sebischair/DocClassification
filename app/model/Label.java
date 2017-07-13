package model;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by Manoj on 10/26/2016.
 */
@Embedded
public class Label {
    private String name;
    private String path;
    private String labelId;
    private String type;

    public Label() {};

    public Label(String name, String path, String labelId, String type) {
        this.name = name;
        this.path = path;
        this.labelId = labelId;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
