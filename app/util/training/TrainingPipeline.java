package util.training;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Pipeline;

/**
 * Created by Manoj on 10/24/2016.
 */
public abstract class TrainingPipeline {
    public Pipeline pipeline;

    public abstract void init();
    public abstract void load();
    public abstract void process();
    public abstract ObjectNode evaluate();
    public abstract void save();
    public ObjectNode run(Pipeline pipeline) {
        this.pipeline = pipeline;
        init();
        load();
        process();
        ObjectNode result = evaluate();
        save();
        return result;
    }
}
