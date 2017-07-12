package util;

import com.github.axet.wget.WGet;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 *
 * This is a DEMO-Application to test and demonstrate howto access SocioCortex via Java
 * It is still under Development.
 *
 * The API for SocioCortex is available here:
 * http://sebischair.github.io/dist/index.html
 *
 * Questions to this DEMO-Application:
 * thomas.grass@in.tum.de
 *
 * Questions to SocioCortex-API:
 * thomas.reschenhofer@tum.de
 *
 */
public class RestCaller {
    public static final String API_BASE_URL = "https://server.sociocortex.com/api/v1/";
    public static final String API_USERNAME = "manoj5864@gmail.com";
    public static final String API_PASSWORD = "@Sebis5864";

    // GET REQUESTS

    public static void printOutUsers() throws Exception{
        HttpURLConnection connection = connectionForGetRequest("users");
        System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
        String output = outputForConnection(connection);
        System.out.println(output);
        connection.disconnect();
    }

    public static void printOutGroups() throws IOException{
        HttpURLConnection connection = connectionForGetRequest("groups");
        System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
        String output = outputForConnection(connection);
        System.out.println(output);
        connection.disconnect();
    }

    public static void printOutTypes() throws IOException{
        HttpURLConnection connection = connectionForGetRequest("types");
        System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
        String output = outputForConnection(connection);
        System.out.println(output);
        connection.disconnect();
    }

    public static void printOutWorkspaces() throws IOException{
        HttpURLConnection connection = connectionForGetRequest("workspaces");
        System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
        String output = outputForConnection(connection);
        System.out.println(output);
        connection.disconnect();
    }

    public static void printOutTypesForWorkspace(String aWorkspaceUid) throws IOException{
        HttpURLConnection connection = connectionForGetRequest(aWorkspaceUid+"/types");
        System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
        String output = outputForConnection(connection);
        System.out.println(output);
        connection.disconnect();
    }

    /**
     * Get the workspace as JSONObject by searching for it's uid
     * @param aWorkspaceUid: the workspace's uid, eg. workspaces/52sezz1s0ij8
     * @return the workspace as JSONObject, null if error occures
     */
    public static JSONObject workspaceForUid(String aWorkspaceUid) {
        try {
            HttpURLConnection connection = connectionForGetRequest(aWorkspaceUid);
            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject newObject = new JSONObject(output);
            connection.disconnect();
            return newObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find an Entity for entities UID
     * @param anEntityUid: the entity's uid, eg. entities/52sezz1s0ij8
     * @return the entity as JSONObject, null if error occures
     */
    public static JSONObject entityForUid(String anEntityUid) {
        try {
            HttpURLConnection connection = connectionForGetRequest(anEntityUid);
            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject newObject = new JSONObject(output);
            connection.disconnect();
            return newObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find a Type for types UID
     * @param aTypeUid: the entity's uid, eg. types/52sezz1s0ij8
     * @return the entity as JSONObject, null if error occures
     */
    public static JSONObject typeForUid(String aTypeUid) {
        try {
            HttpURLConnection connection = connectionForGetRequest(aTypeUid);
            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject newObject = new JSONObject(output);
            connection.disconnect();
            return newObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the types of a workspace as JSONArray by searching for workspaces uid
     * @param aWorkspaceUid: the workspace's uid, eg. workspaces/52sezz1s0ij8
     * @return the types as JSONArray, null if error occures
     */
    public static JSONArray typesForWorkspaceUid(String aWorkspaceUid) {
        try {
            HttpURLConnection connection = connectionForGetRequest(aWorkspaceUid + "/types");
            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONArray newArray = new JSONArray(output);
            connection.disconnect();
            return newArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all entities of an workspace
     * @param aWorkspaceUid : the workspace's uid, eg. workspaces/52sezz1s0ij8
     * @return the entities as JSONArray, null if error occures
     */
    public static JSONArray entitiesForWorkspaceUid(String aWorkspaceUid) {
        try {
            HttpURLConnection connection = connectionForGetRequest(aWorkspaceUid + "/entities");
            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONArray newArray = new JSONArray(output);
            connection.disconnect();
            return newArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // POST REQUESTS

    /**
     * Create a new workspace using Post.
     * @param workspaceName
     * @return the created workspace as JSONObject returned from the server, null if error occured (e.g. workspace with name already exsists, etc.)
     */
    public static JSONObject postWorkspace(String workspaceName) {
        try {
            HttpURLConnection connection = connectionForPostRequest("workspaces", "POST");

            JSONObject newWorkspace = new JSONObject();
            newWorkspace.put("name", workspaceName);

            OutputStream os = connection.getOutputStream();
            os.write(newWorkspace.toString().getBytes());
            os.flush();

            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);

            JSONObject newObject = new JSONObject(output);
            connection.disconnect();

            return newObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new type using Post
     * @param aWorkspace : a JSONObject of the workspace
     * @return the created type as JSONObject returned from the server, null if error occurs
     */
    public static JSONObject postType(String name, String namePlural, JSONObject aWorkspace) {
        try {
            HttpURLConnection connection = connectionForPostRequest("types", "POST");

            JSONObject newType = new JSONObject();
            newType.put("name", name);
            newType.put("namePlural", namePlural);
            newType.put("workspace", aWorkspace);

            OutputStream os = connection.getOutputStream();
            os.write(newType.toString().getBytes());
            os.flush();

            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);

            JSONObject newObject = new JSONObject(output);
            connection.disconnect();

            return newObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Create a new Entity by using Post
     * @param anEntityName: the name of the new entity
     * @param aWorkspace: an JSONObject of the workspace
     * @param aType: an JSONObject of the type
     * @return the created entity as JSONObject returned from the server, null if error occurs
     */
    public static JSONObject postEntity(String anEntityName, JSONObject aWorkspace, JSONObject aType, JSONArray attributes) {
        try {
            HttpURLConnection connection = connectionForPostRequest("entities", "POST");

            JSONObject newEntity = new JSONObject();
            newEntity.put("name", anEntityName);
            newEntity.put("workspace", aWorkspace);
            newEntity.put("type", aType);
            newEntity.put("attributes", attributes);

            System.out.println(newEntity.toString());

            OutputStream os = connection.getOutputStream();
            os.write(newEntity.toString().getBytes());
            os.flush();

            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);

            JSONObject newObject = new JSONObject(output);
            connection.disconnect();

            return newObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject postProperty(String aPropertyName, JSONObject aType) {
        try {
            HttpURLConnection connection = connectionForPostRequest(aType.getString("uid") + "/properties", "POST");

            JSONObject newEntity = new JSONObject();
            newEntity.put("name", aPropertyName);

            System.out.println(newEntity.toString());

            OutputStream os = connection.getOutputStream();
            os.write(newEntity.toString().getBytes());
            os.flush();

            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);

            JSONObject newObject = new JSONObject(output);
            connection.disconnect();

            return newObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // DELETE FUNCTIONS

    /**
     * Delete an Entity
     * @param anEntityUid: the entities Uid, e.g. entities/38xa88sa
     * @return true if object deleted, false if error occurs
     */
    public static boolean deleteEntity(String anEntityUid) {
        try {
            HttpURLConnection connection = connectionForDeleteRequest(anEntityUid);
            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject resultObject = new JSONObject(output);
            connection.disconnect();
            if(resultObject.getBoolean("success"))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete a type
     * @param aTypeUid: the types Uid, e.g. types/38xa88sa
     * @return true if object deleted, false if error occurs
     */
    public static boolean deleteType(String aTypeUid) {
        try {
            HttpURLConnection connection = connectionForDeleteRequest(aTypeUid);
            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject resultObject = new JSONObject(output);
            connection.disconnect();
            if(resultObject.getBoolean("success"))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean deleteWorkspace(String aWorkspaceUID) {
        try {
            HttpURLConnection connection = connectionForDeleteRequest(aWorkspaceUID);
            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject resultObject = new JSONObject(output);
            connection.disconnect();
            if(resultObject.getBoolean("success"))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // PUT REQUESTS

    /**
     * Edit a type
     * @param aTypeName: String of the new type name
     * @param aTypeNamePlural: String of the new type name plural
     * @param aTypeUid: the type's uid
     * @return a JSONObject of the new edited type, null if error occurs
     */
    public static JSONObject editType(String aTypeName, String aTypeNamePlural, String aTypeUid) {
        try {
            HttpURLConnection connection = connectionForPutRequest(aTypeUid);


            JSONObject editType = new JSONObject();
            editType.put("name", aTypeName);
            editType.put("namePlural", aTypeNamePlural);

            System.out.println(editType.toString());

            OutputStream os = connection.getOutputStream();
            os.write(editType.toString().getBytes());
            os.flush();


            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject resultObject = new JSONObject(output);
            connection.disconnect();

            return resultObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static JSONObject editEntity(String anEntityName, String anEntityUid) {
        try {
            HttpURLConnection connection = connectionForPutRequest(anEntityUid);


            JSONObject editType = new JSONObject();
            editType.put("name", anEntityName);

            System.out.println(editType.toString());

            OutputStream os = connection.getOutputStream();
            os.write(editType.toString().getBytes());
            os.flush();


            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject resultObject = new JSONObject(output);
            connection.disconnect();

            return resultObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static JSONObject editWorkspace(String aWorkspaceName, String aWorkspaceUid) {
        try {
            HttpURLConnection connection = connectionForPutRequest(aWorkspaceUid);


            JSONObject editWorkspace = new JSONObject();
            editWorkspace.put("name", aWorkspaceName);

            System.out.println(editWorkspace.toString());

            OutputStream os = connection.getOutputStream();
            os.write(editWorkspace.toString().getBytes());
            os.flush();


            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);
            JSONObject resultObject = new JSONObject(output);
            connection.disconnect();

            return resultObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // BASIC HELPER-FUNCTIONS TO ACCESS AND READOUT DATA

    public static void saveFile(HttpURLConnection connection, String fileName, double folderName) {
        String output = outputForConnection(connection);
        if (!Files.exists(Paths.get(play.Play.application().path().getAbsolutePath() + "/tmp/"+folderName))) {
            try {
                Files.createDirectories(Paths.get(play.Play.application().path().getAbsolutePath() + "/tmp/"+folderName));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        output = connection.getURL().getPath().replace("/content", " ---- ") + output;
        File file = new File(play.Play.application().path().getAbsolutePath() + "/tmp/"+folderName+"/"+fileName);
        try {
            FileUtils.writeStringToFile(file, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(String requestUrl, String fileName, double folderName) {
        try {
            Authenticator.setDefault(new CustomAuthenticator());
            URL url = new URL(API_BASE_URL + (requestUrl == null ? "" : requestUrl));
            File target = new File(play.Play.application().path().getAbsolutePath() + "/tmp/"+folderName+"/"+fileName);
            WGet w = new WGet(url, target);
            w.download();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static class CustomAuthenticator extends Authenticator {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(API_USERNAME, API_PASSWORD.toCharArray());
        }
    }

    /**
     * Create a connection for request (including login).
     * @param request : A request String. Will be appended to the API_BASE_URL, "/" as prefix is not needed/allowed
     * @return an connection of type HttpURLConnection, null if error occurs
     */
    public static HttpURLConnection connectionForGetRequest(String request) {
        try {
            URL url = null;
            if(!request.contains(API_BASE_URL)) {
                url = new URL(API_BASE_URL + (request == null ? "" : request));
            } else {
                url = new URL(request);
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String login = "username:password";
            String userpass = API_USERNAME + ":" + API_PASSWORD;
            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
            connection.setRequestProperty("Authorization", basicAuth);

            return connection;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a connection for delete requests (including login).
     * @param request : A request String. Will be appended to the API_BASE_URL, "/" as prefix is not needed/allowed
     * @return an connection of type HttpURLConnection, null if error occurs
     */
    public static HttpURLConnection connectionForDeleteRequest(String request) {
        try {
            URL url = new URL(API_BASE_URL + (request == null ? "" : request));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            String login = "username:password";
            String userpass = API_USERNAME + ":" + API_PASSWORD;
            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestMethod("DELETE");

            return connection;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a connection for put requests (including login).
     * @param request : A request String. Will be appended to the API_BASE_URL, "/" as prefix is not needed/allowed
     * @return an connection of type HttpURLConnection, null if error occurs
     */
    public static HttpURLConnection connectionForPutRequest(String request) {
        try {
            URL url = new URL(API_BASE_URL + (request == null ? "" : request));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            String login = "username:password";
            String userpass = API_USERNAME + ":" + API_PASSWORD;
            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            return connection;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a connection for a Post request (including login).
     * @param request : A request String. Will be appended to the API_BASE_URL, "/" as prefix is not needed/allowed
     * @return an connection of type HttpURLConnection, null if error occurs
     */
    public static HttpURLConnection connectionForPostRequest(String request, String requestMethod) {
        try {
            URL url;
            if(request.contains(API_BASE_URL)) {
                url = new URL(request);
            } else {
                url = new URL(API_BASE_URL + (request == null ? "" : request));
            }
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            String login = "username:password";
            String userpass = API_USERNAME + ":" + API_PASSWORD;
            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            return connection;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Readout output for connection as String
     * @param connection : an open HttpURLConnection, e.g. use connectionForGetRequest(String request)
     * @return a String with the complete output, null if error occurs or connection is null or not in Http-Status 200.
     */
    public static String outputForConnection(HttpURLConnection connection) {
        if(connection==null) return null;
        try {
            if(connection.getResponseCode() != 200) return null;

            StringBuffer allOutput = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));

            String output;
            while ((output = br.readLine()) != null) {
                allOutput.append(output);
            }
            return allOutput.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Returns a readable version of the HTTP-StatusCode
     * @param aStatusCode : a HTTP-StatusCode, e.g. 200
     * @return a readable String, e.g. "OK [Successful]"
     */
    public static String responseCodeDisplayForCode(int aStatusCode) {
        HashMap<Integer, String> responseCodes = new HashMap<Integer, String>();
        responseCodes.put(100,"Continue [Informational]");
        responseCodes.put(101,"Switching Protocols [Informational]");
        responseCodes.put(200,"OK [Successful]");
        responseCodes.put(201,"Created [Successful]");
        responseCodes.put(202,"Accepted [Successful]");
        responseCodes.put(203,"Non-Authoritative Information [Successful]");
        responseCodes.put(204,"No Content [Successful]");
        responseCodes.put(205,"Reset Content [Successful]");
        responseCodes.put(206,"Partial Content [Successful]");
        responseCodes.put(300,"Multiple Choices [Redirection]");
        responseCodes.put(301,"Moved Permanently [Redirection]");
        responseCodes.put(302,"Found [Redirection]");
        responseCodes.put(303,"See Other [Redirection]");
        responseCodes.put(304,"Not Modified [Redirection]");
        responseCodes.put(305,"Use Proxy [Redirection]");
        responseCodes.put(307,"Temporary Redirect [Redirection]");
        responseCodes.put(400,"Bad Request [Client Error]");
        responseCodes.put(401,"Unauthorized [Client Error]");
        responseCodes.put(402,"Payment Required [Client Error]");
        responseCodes.put(403,"Forbidden [Client Error]");
        responseCodes.put(404,"Not Found [Client Error]");
        responseCodes.put(405,"Method Not Allowed [Client Error]");
        responseCodes.put(406,"Not Acceptable [Client Error]");
        responseCodes.put(407,"Proxy Authentication Required [Client Error]");
        responseCodes.put(408,"Request Timeout [Client Error]");
        responseCodes.put(409,"Conflict [Client Error]");
        responseCodes.put(410,"Gone [Client Error]");
        responseCodes.put(411,"Length Required [Client Error]");
        responseCodes.put(412,"Precondition Failed [Client Error]");
        responseCodes.put(413,"Request Entity Too Large [Client Error]");
        responseCodes.put(414,"Request-URI Too Long [Client Error]");
        responseCodes.put(415,"Unsupported Media Type [Client Error]");
        responseCodes.put(416,"Requested Range Not Satisfiable [Client Error]");
        responseCodes.put(417,"Expectation Failed [Client Error]");
        responseCodes.put(500,"Internal Server Error [Server Error]");
        responseCodes.put(501,"Not Implemented [Server Error]");
        responseCodes.put(502,"Bad Gateway [Server Error]");
        responseCodes.put(503,"Service Unavailable [Server Error]");
        responseCodes.put(504,"Gateway Timeout [Server Error]");
        responseCodes.put(505,"HTTP Version Not Supported [Server Error]");

        if(!responseCodes.containsKey(aStatusCode)) return "STATUSCODE NOT FOUND";
        return responseCodes.get(aStatusCode);
    }


    // BASIS FUNCTIONS FOR HANDLING JSON

    /**
     * Parse a JSON-Object out of an Raw JSON-Object-String
     * @param rawString, e.g. {"uid":"users/12345678","name":"John von Neumann"}
     * @return an JSONObject if no errors occur, null if errors occur
     */
    public static JSONObject parseJSONObject(String rawString) {
        try {
            JSONObject jo = new JSONObject(rawString);
            /*Iterator i = jo.keys();
            while (i.hasNext()) {
                String key = (String) i.next();
                try {
                    String value = (String) jo.getString(key);
                    System.out.println(key + "\t" + value);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }*/
            return jo;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse a JSON-Array out of an Raw JSON-Array-String
     * @param rawString, e.g. [{"uid":"users/1234","name":"John von Neumann"},{"uid":"users/1235","name":"Conrad Zuse"}]
     * @return an JSONArray if no errors occur, null if errors occur
     */
    public static JSONArray parseJSONArray(String rawString) {
        try {
            JSONArray ja = null;
            ja = new JSONArray(rawString);
            return ja;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Move a file to a different entity.
     * @param filePath
     * @param entityId
     * @return
     */
    public static JSONObject moveFile(String filePath, String entityId) {
        try {
            System.out.println(filePath);
            HttpURLConnection connection = connectionForPostRequest(filePath, "PUT");

            String fileId = filePath.split("files/")[1];
            JSONObject post = new JSONObject();
            post.put("id", fileId);

            JSONObject entity = new JSONObject();
            entity.put("id", entityId);
            post.put("entity", entity);

            OutputStream os = connection.getOutputStream();
            os.write(post.toString().getBytes());
            os.flush();

            System.out.println("Status: " + connection.getResponseCode() + " - " + responseCodeDisplayForCode(connection.getResponseCode()));
            String output = outputForConnection(connection);
            System.out.println(output);

            JSONObject newObject = new JSONObject(output);
            connection.disconnect();

            return newObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}