import db.AmelieMongoClient;
import db.DefaultMongoClient;
import model.Classifier;
import play.Application;
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
        try {
            DefaultMongoClient.connect();
            AmelieMongoClient.connect();
            Logger.info("Application has started");
            initDatabase();
        } catch (Exception e) {
            Logger.error("** Cannot connect to mongo: " + e.toString());
        }
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