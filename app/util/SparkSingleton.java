package util;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import play.Logger;

import javax.inject.Singleton;

/**
 * Created by mahabaleshwar on 10/23/2016.
 */
@Singleton
public class SparkSingleton {
    private static SparkSingleton sparkInstance = null;
    private JavaSparkContext sparkContext = null;
    private SQLContext sqlContext = null;

    private SparkSingleton() {
        Logger.debug("Initializing Spark context");

        /*
        SparkSession sparkSession = SparkSession.builder()
                .master("local[1]")
                .appName("Testing Spark").getOrCreate();
        JavaSparkContext sparkContext = new JavaSparkContext(sparkSession.sparkContext());
        */
        if(sparkContext == null) {
            SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local[1]");
            sparkContext = new JavaSparkContext(conf);
        }
        if(sqlContext == null) sqlContext = new SQLContext(sparkContext);
    }

    public static SparkSingleton getInstance() {
        if(sparkInstance == null) sparkInstance = new SparkSingleton();
        return sparkInstance;
    }

    public JavaSparkContext getSparkContext() {
        return sparkContext;
    }

    public SQLContext getSqlContext() { return sqlContext; }

}
