import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import controllers.MorphiaObject;
import model.Classifier;
import model.Pipeline;
import org.mongodb.morphia.Morphia;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import util.StaticFunctions;

import java.util.Date;

/**
 * Created by Manoj on 10/24/2016.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app) {
        super.beforeStart(app);
        Logger.debug("** onStart **");
        MorphiaObject.mongo = new Mongo("127.0.0.1", 27017);
        MorphiaObject.morphia = new Morphia();
        MorphiaObject.morphia.mapPackage("app.model");
        MorphiaObject.datastore = MorphiaObject.morphia.createDatastore(new MongoClient(), "docclassifier");
        MorphiaObject.datastore.ensureIndexes();
        MorphiaObject.datastore.ensureCaps();
        Logger.debug("** Morphia datastore: " + MorphiaObject.datastore.getDB());
        Logger.info("Application has started");
        initDatabase();
    }

    private void initDatabase() {
        if(new Classifier().getAll().size() == 0) {
            Classifier classifier = new Classifier();
            classifier.setName(StaticFunctions.LIBSVM);
            classifier.save();
            classifier = new Classifier();
            classifier.setName(StaticFunctions.NAIVEBAYES);
            classifier.save();
        }
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }
}