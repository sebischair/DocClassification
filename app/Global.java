import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import controllers.MorphiaObject;
import model.Classifier;
import org.mongodb.morphia.Morphia;
import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj on 10/24/2016.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app) {
        super.beforeStart(app);
        Logger.debug("** onStart **");
        connect();
        Logger.debug("** Morphia datastore: " + MorphiaObject.datastore.getDB());
        Logger.info("Application has started");
        initDatabase();
    }

    private void connect() {
        Configuration configuration = Configuration.root();
        String dbUrl = configuration.getString("morphia.db.url");
        int dbPort = configuration.getInt("morphia.db.port");
        String userName = configuration.getString("morphia.db.username");
        String password = configuration.getString("morphia.db.pwd");
        String dbName = configuration.getString("morphia.db.name");

        ServerAddress sa = new ServerAddress(dbUrl, dbPort);
        List<MongoCredential> cl = new ArrayList<MongoCredential>();
        MongoCredential mc = MongoCredential.createCredential(userName, dbName, password.toCharArray());
        cl.add(mc);

        MorphiaObject.morphia = new Morphia();
        MorphiaObject.morphia.mapPackage("app.model");
        MorphiaObject.datastore = MorphiaObject.morphia.createDatastore(new MongoClient(sa, cl), dbName);
        MorphiaObject.datastore.ensureIndexes();
        MorphiaObject.datastore.ensureCaps();
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