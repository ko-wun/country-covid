import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.*;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Ko-Wun Kim
 * Last Modified: April 8, 2021
 *
 * This model handles getting appropriate data from 3rd party API, logging data to MongoDB, and retrieving log information from MongoDB.
 * Appropriate methods are called from the servlet depending on passed parameters in the request.
 */
public class Project4Task2_CountryCovidModel {

    /**
     * If parameter is a country name, gets COVID case data of that country.
     * It then calls writeMongoDB method to log the information and response on MongoDB.
     * @param time time request was made
     * @param countryName country name to look for
     * @param model phone model
     * @return JSONObject of response back to Android
     */
    public JSONObject getCovidData(String time, String countryName, String model){ //param is country Name
        HashMap<String, Integer> result = new HashMap<>();

        JSONObject data = null;
        try{

            // retrieves JSON data from 3rd party API and parses the JSON.
            JSONObject json = readJSON("https://covid19-api.weedmark.systems/api/v1/stats?country="+modifyCountryName(countryName));
            data = json.getJSONObject("data");
            JSONArray jArray = data.getJSONArray("covid19Stats");

            for(int i=0; i<jArray.length(); i++) { // sum up total confirmed, deaths, and recovered cases across the country's provinces
                JSONObject dataObj = jArray.getJSONObject(i);
                if (dataObj.has("confirmed") && dataObj.get("confirmed")!=null){ //if there are confirmed cases
                    result.put("confirmed", result.getOrDefault("confirmed", 0)+dataObj.getInt("confirmed"));
                }
                if (dataObj.has("deaths") && dataObj.get("deaths")!=null){ //if there are deaths cases
                    result.put("deaths", result.getOrDefault("deaths", 0)+dataObj.getInt("deaths"));
                }
                if (dataObj.has("recovered") && !dataObj.get("recovered").equals(null)){ //if there are recovered cases
                    result.put("recovered", result.getOrDefault("recovered", 0)+dataObj.getInt("recovered"));
                }
            }

        }catch(JSONException e){
            e.printStackTrace();
        }

        JSONObject output =  new JSONObject(result); //response to send back to application

        //get the final data to write to MongoDB
        String confirmedData = null;
        String deathData = null;
        String recoveredData = null;

        if (result.containsKey("confirmed")) confirmedData = result.get("confirmed").toString();
        if (result.containsKey("deaths")) deathData = result.get("deaths").toString();
        if (result.containsKey("recovered")) recoveredData = result.get("recovered").toString();

        writeMongoDB(time, System.currentTimeMillis(), countryName, true, model, confirmedData, deathData, recoveredData);

        return output;
    }

    /**
     * White space in country name has to be replaced with "%20" to send over HTTP.
     * Although a modified country name sent from Android side, the %20 gets replaced as white space through PUT request.
     * Because this project uses one Android application for task 1 (uses GET) and 2 (uses GET and PUT),
     * Another modification has to be done in the model.
     * @param countryName country name to modify
     * @return
     */
    public String modifyCountryName(String countryName){ //modify space to %20
        StringBuilder sb = new StringBuilder();

        if (countryName.contains(" ")){ //if country countains whitespace, have to replace to "%20"
            for (int i=0;i<countryName.length();i++){
                char cur = countryName.charAt(i);
                if (Character.isWhitespace(cur)){
                    sb.append("%20");
                }else {
                    sb.append(cur);
                }
            }
            return sb.toString();
        }
        else return countryName;
    }

    /**
     * If user parameter was "countries", gets total list of countries.
     * Retrieves and parses JSON from 3rd party API to get total countries used to populate the Android application on initialization.
     * @param time time request was made.
     * @param param request that was made. Here it would be "countries".
     * @param model phone model making the request.
     * @return JSONObject of country lists
     */
    public JSONObject getCountries(String time, String param, String model) {
        ArrayList<String> countryList = null;
        JSONArray resultArr = null;
        JSONObject data = null;
        JSONObject json = null;

        try{
            //retrieves and parses JSON from 3rd parth API
            json = readJSON("https://covid19-api.weedmark.systems/api/v1/stats");
            HashSet<String> countriesHS = new HashSet<>();
            data = json.getJSONObject("data");
            JSONArray jArray = data.getJSONArray("covid19Stats");

            //only get the country name field and add to HashSet to prevent duplicate country values

            for(int i=0; i<jArray.length(); i++) {
                JSONObject dataObj = jArray.getJSONObject(i);
                String value = dataObj.getString("country");
                countriesHS.add(value);
            }

            countryList = new ArrayList<>(countriesHS);
            Collections.sort(countryList); //sort country list alphabetically

        }catch(JSONException e){
            e.printStackTrace();
        }
        //change the country list into appropriate JSON format
        resultArr = new JSONArray(countryList);
        JSONObject result = new JSONObject();
        result.put("countries", resultArr);

        //log the request and response to MongoDB
        writeMongoDB(time, System.currentTimeMillis(), param, false, model, null, null, null);

        return result;
    }

    /**
     * Reads JSON values given URL.
     * The following method is taken from https://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
     * @param url URL to read JSON from
     * @return JSONObject of the URL contents
     */
    private JSONObject readJSON(String url){
        try {
            InputStream is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);


            return json;

        } catch (Exception e) {
            System.out.print("Error: "+e);
            return null;
        }
    }

    /**
     * Reads all data given a Reader.
     * The following method is taken from https://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
     * @param rd
     * @return String of data
     */
    private String readAll(Reader rd) {
        StringBuilder sb = new StringBuilder();

        try{
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }catch (IOException e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * This method logs information about the request and response to MongoDB.
     * It connects with MongoDB Atlas and changes the data into readable format to log onto MongoDB.
     * @param requestTime time request was made from Android.
     * @param replyTime time request was completed from this model.
     * @param requestString request that was made from Android .
     * @param asksData boolean value. True if country COVID data was requested, false if country list was requested upon app initialization.
     * @param model phone model making the request
     * @param confirmed confirmed COVID cases. Null if request was for country list.
     * @param deaths deaths COVID cases. Null if request was for country list.
     * @param recovered recovered COVID cases. Null if request was for country list.
     */
    public void writeMongoDB(String requestTime, long replyTime, String requestString, boolean asksData, String model, String confirmed, String deaths, String recovered){

        //connect and store data on MongoDB database.
        MongoClientURI uri = new MongoClientURI(
                "mongodb+srv://kowunkDSProject4:dsproject4@cluster0.k5lyr.mongodb.net/countryCovid?retryWrites=true&w=majority");

        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("countryCovid");
        MongoCollection<Document> collection = database.getCollection("data");

        //change requestTime that is in string into timestamp for a readable format
        Timestamp requestTimeStamp = new Timestamp(Long.parseLong(requestTime));
        Timestamp requestCompleteTimeStamp = new Timestamp(replyTime);

        Document doc = null;
        if (asksData){ //if asked for country specific data
            doc = new Document("Request Time", requestTimeStamp)
                    .append("Request Complete Time", requestCompleteTimeStamp)
                    .append("Phone Model", model)
                    .append("Request Type", "Country Data")
                    .append("Request Country", requestString)
                    .append("Reply", new Document("confirmed", confirmed).append("deaths", deaths).append("recovered", recovered)); //create inner document of cases data
        } else { //if request was to populate entire country list (app initialization)
            doc = new Document("Request Time", requestTimeStamp)
                    .append("Request Complete Time", requestCompleteTimeStamp)
                    .append("Phone Model", model)
                    .append("Request Type", "Initialization Data")
                    .append("Request", requestString)
                    .append("Reply", "country list returned");
        }
        collection.insertOne(doc);


    }

    /**
     * This method retrieves log data from MongoDB to display on the dashboard.
     * @return ArrayList of MongoDB documents containing log information.
     */
    public ArrayList<Document> receiveMongoDB(){
        //connect and store data on MongoDB database.
        MongoClientURI uri = new MongoClientURI(
                "mongodb+srv://kowunkDSProject4:dsproject4@cluster0.k5lyr.mongodb.net/countryCovid?retryWrites=true&w=majority");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("countryCovid");
        MongoCollection<Document> collection = database.getCollection("data");

        //read through the data stored in MongoDB
        //this part is taken from http://mongodb.github.io/mongo-java-driver/4.1/driver/getting-started/quick-start/
        MongoCursor<Document> cursor = collection.find().iterator();
        ArrayList<Document> result = new ArrayList<>(); //returns current data log
        try {
            while (cursor.hasNext()) {
                result.add(cursor.next());
            }
        } finally {
            cursor.close();
        }

        return result;

    }

    /**
     * Gets top three countries searched from the data in MongoDB for the operation analytics to display on the dashboard.
     * @param data total log data from MongoDB
     * @return ArrayList of top three searched country names
     */
    public ArrayList<String> topThreeSearched(ArrayList<Document> data){
        //records total search count for each countries
        HashMap<String, Integer> counts = new HashMap<>();
        for (Document d : data){
            String request = (String) d.get("Request Country");
            if (request!=null && !request.equals("countries")){
                counts.put(request, counts.getOrDefault(request, 0)+1);
            }
        }
        //Getting top three counts from a hashMap of (country Name, count).
        //This code is from https://stackoverflow.com/questions/62077736/how-to-get-the-3-highest-values-in-a-hashmap
        List<String> topThreeCountries = counts.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(3).map(Map.Entry::getKey).collect(Collectors.toList());

        return (ArrayList<String>) topThreeCountries;

    }

    /**
     * Gets top three phone models that sent request for the operation analytics to display on the dashboard.
     * @param data total log data from MongoDB
     * @return ArrayList of top three phone models sending request
     */
    public ArrayList<String> topThreeModels(ArrayList<Document> data){
        //records total count for each phone models stored in MongoDB
        HashMap<String, Integer> counts = new HashMap<>();
        for (Document d : data){
            String request = (String) d.get("Phone Model");
            if (request!=null ){
                counts.put(request, counts.getOrDefault(request, 0)+1);
            }
        }

        //Getting top three counts from a hashMap of (phone model, count).
        //This code is from https://stackoverflow.com/questions/62077736/how-to-get-the-3-highest-values-in-a-hashmap
        List<String> topThreeModels = counts.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(3).map(Map.Entry::getKey).collect(Collectors.toList());
        return (ArrayList<String>) topThreeModels;

    }

    /**
     * Calculates average time it took to return a response based on MongoDB log data.
     * @param data total log data from MongoDB
     * @return double average time value
     */
    public double averageResponseTime(ArrayList<Document> data) {
        long totalDiff = 0;
        int counts = 0;

        //go through MongoDB log data and sum the time it took for request completion
        for (Document d : data){
            Date completeTime = (Date) d.get("Request Complete Time");
            Date requestTime = (Date)d.get("Request Time");

            if (completeTime !=null && requestTime !=null){
                totalDiff += completeTime.getTime() - requestTime.getTime();
                counts++;
            }

        }

        double average = (double) totalDiff / counts; //calculate average
        return average;
    }
}
