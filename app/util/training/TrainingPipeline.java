package util.training;

import model.Pipeline;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public abstract class TrainingPipeline {
    public Pipeline pipeline;

    public abstract void init();
    public abstract void load();
    public abstract void process();
    public abstract String evaluate();
    public abstract void save();
    public String run(Pipeline pipeline) {
        this.pipeline = pipeline;
        init();
        load();
        process();
        String result = evaluate();
        save();
        return result;
    }
}
