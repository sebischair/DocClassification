import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import util.RestCaller;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;


/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {
    public static final String API_BASE_URL = "https://server.sociocortex.com/api/v1/";
    public static final String API_USERNAME = "manoj5864@gmail.com";
    public static final String API_PASSWORD = "@Sebis5864";

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void testRestCall() {
        try {
            HttpURLConnection connection = RestCaller.connectionForGetRequest("entities/ose32apj5pa9/files");
            System.out.println("Status: " + connection.getResponseCode() + " - " + RestCaller.responseCodeDisplayForCode(connection.getResponseCode()));
            String output = RestCaller.outputForConnection(connection);
            JSONArray newArray = new JSONArray(output);

            for(int i=0;i<newArray.length(); i++) {
                JSONObject jo = newArray.getJSONObject(i);
                RestCaller.saveFile(jo.getString("href")+ "/content", jo.getString("name"), 0.2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
