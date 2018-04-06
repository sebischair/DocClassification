package controllers;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.ArrayList;
import java.util.List;

public class MorphiaObject {
    static public Morphia morphia;
    static public Datastore datastore;

    static public void connect(String dbUrl, Integer dbPort, String dbName, String dbUsername, String dbPwd) throws Exception{
        String dockerHost = "mongo";    // For docker, don't provide credentials to database
        ServerAddress sa = new ServerAddress(dbUrl, dbPort);
        List<MongoCredential> cl = new ArrayList<>();
        MongoCredential mc = MongoCredential.createCredential(dbUsername, dbName, dbPwd.toCharArray());
        cl.add(mc);

        morphia = new Morphia();
        morphia.mapPackage("app.model");
        if (dbUrl.equals(dockerHost)) {
            datastore = morphia.createDatastore(new MongoClient(sa), dbName);
        } else {
            datastore = morphia.createDatastore(new MongoClient(sa, cl), dbName);
        }
        datastore.ensureIndexes();
        datastore.ensureCaps();
    }
}