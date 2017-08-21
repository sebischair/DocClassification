import controllers.MorphiaObject;
import model.Classifier;
import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import util.StaticFunctions;

/**
 * Created by Manoj on 10/24/2016.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app) {
        super.beforeStart(app);
        Logger.debug("** onStart **");
        Configuration configuration = Configuration.root();
        try {
            MorphiaObject.connect(
                    configuration.getString("morphia.db.url"),
                    configuration.getInt("morphia.db.port"),
                    configuration.getString("morphia.db.name"),
                    configuration.getString("morphia.db.username"),
                    configuration.getString("morphia.db.pwd")
            );
            Logger.debug("** Morphia datastore: " + MorphiaObject.datastore.getDB());
        } catch (Exception e) {
            Logger.error("** Morphia datastore: " + e.toString());
        }

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