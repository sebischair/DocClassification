import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import controllers.MorphiaObject;
import org.mongodb.morphia.Morphia;
import play.*;
import util.SparkSingleton;

/**
 * Created by mahabaleshwar on 10/24/2016.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app) {
        super.beforeStart(app);
        Logger.debug("** onStart **");
        SparkSingleton.getInstance();
        Logger.debug("** Spark instance created");
        MorphiaObject.mongo = new Mongo("127.0.0.1", 27017);
        MorphiaObject.morphia = new Morphia();
        MorphiaObject.morphia.mapPackage("app.model");
        MorphiaObject.datastore = MorphiaObject.morphia.createDatastore(new MongoClient(), "docclassifier");
        MorphiaObject.datastore.ensureIndexes();
        MorphiaObject.datastore.ensureCaps();
        Logger.debug("** Morphia datastore: " + MorphiaObject.datastore.getDB());
        Logger.info("Application has started");
    }

    public void onStop(Application app) {
        SparkSingleton.getInstance().getSparkContext().close();
        SparkSingleton.getInstance().getSparkContext().stop();
        Logger.info("Application shutdown...");
    }

}
