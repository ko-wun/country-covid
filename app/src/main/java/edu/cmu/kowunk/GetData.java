package edu.cmu.kowunk;


import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Author: Ko-Wun Kim
 * Last Modified: April 8, 2021
 *
 * This class enables access to the web service for two tasks:
 * 1) get list of countries or
 * 2) get COVID data of use selected country.
 * This class uses two AsyncTask inner classes, AsyncDataSearch and AsyncCountrySearch to perform these two tasks.
 * In each of these AsyncTask classes, helper thread performs network connection and the main UI thread's onPostExecute
 * handles the update.
 */
public class GetData {

    CountryCOVIDAndroid cc = null;

    /**
     * This will be called upon app initialization to fill country list into spinner.
     * It executes AsyncCountrySearch class
     * @param cc CountryCOVIDAndroid object
     */
    public void getCountries(CountryCOVIDAndroid cc){
        this.cc = cc;
        new AsyncCountrySearch().execute();
    }

    /**
     * Called when user presses submit button with country selection.
     * It executes AsyncDataSearch class.
     * @param searchCountry name of country user selected.
     */
    public void getData(String searchCountry){
        new AsyncDataSearch().execute(searchCountry);
    }

    /**
     * Inner class to retrieve COVID case data of user selected country by connecting to the web service.
     * doInBackground runs in the helper thread while onPostExecute runs in the UI main thread.
     */
    private class AsyncDataSearch extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... country) {
            return getCovidData(country[0]);
        }

        protected void onPostExecute(JSONObject data){
            cc.dataReady(data);
        }

        /**
         * Connects with a Heroku deployed web service to get response in JSONObject format.
         * @param country country name
         * @return JSONObject of country's COVID case data
         */
        private JSONObject getCovidData(String country){

            try{
                //Uncomment this part for task 1
//                String urlString = "https://banana-cake-19132.herokuapp.com/";
//                urlString+="?param="+ modifyCountryName(country);
//                URL url = new URL(urlString);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setDoOutput(true);
//                connection.connect();
//                String result = "";
//                Scanner sc = new Scanner(url.openStream());
//
//                while (sc.hasNext()){
//                    result+=sc.nextLine();
//                }
//                JSONObject data = new JSONObject(result);
//                return data;


                //This part is task 2. Comment this out up to line 122 to test for task 1
                String urlString = "https://mighty-river-12710.herokuapp.com/";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                //Sending POST request through HTTPURLConnection.
                // This code is from https://www.baeldung.com/httpurlconnection-post
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String parameters = "time="+System.currentTimeMillis()+"&param="+modifyCountryName(country)+"&model="+Build.MODEL; //sends time request is made, request parameter, and phone model with the request
                PrintWriter out = new PrintWriter(connection.getOutputStream());
                out.print(parameters);
                out.flush();
                connection.connect();

                //receiving JSONobject response.
                // This part of code is from https://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object
                StringBuilder result = new StringBuilder();
                try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        result.append(responseLine.trim());
                    }
                }
                JSONObject data = new JSONObject(result.toString());

                return data;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        /**
         * Method to modify country name to send to web service because any white space in country name has to be
         * replaced with "%20".
         * @param country country name potentially containing white space
         * @return country name with white space replaced
         */
        private String modifyCountryName(String country){
            StringBuilder sb = new StringBuilder();

            if (country.contains(" ")){ //if country contains whitespace, have to replace to "%20"
                for (int i=0;i<country.length();i++){
                    char cur = country.charAt(i);
                    if (Character.isWhitespace(cur)){
                        sb.append("%20");
                    }else {
                        sb.append(cur);
                    }
                }
                return sb.toString();
            }
            else return country;


        }
    }


    /**
     * Inner class to retrieve list of countries by connecting to the web service.
     * doInBackground runs in the helper thread while onPostExecute runs in the UI main thread.
     */
    private class AsyncCountrySearch extends AsyncTask<String, Void, ArrayList<String>> {

        protected ArrayList<String> doInBackground(String... strings) {
            return getCountries();
        }

        protected void onPostExecute(ArrayList<String> countries){
            cc.fillCountries(countries);
        }

        /**
         * Connects with a Heroku deployed web service to get response in JSONObject format.
         * The JSONObject response is then parsed to extract country names into ArrayList.
         * This is because spinner is directly filled with ArrayList data structure.
         * @return ArrayList of country names
         */
        private ArrayList<String> getCountries() {
            try{
                //Uncomment this part for task 1
//                String urlString = "https://banana-cake-19132.herokuapp.com/";
//                urlString+="?param=countries";
//                URL url = new URL(urlString);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setDoOutput(true);
//                connection.connect();
//                String result = "";
//                Scanner sc = new Scanner(url.openStream());
//                while (sc.hasNext()){
//                    result+=sc.nextLine();
//                }
//                JSONObject countries = new JSONObject(result);
//                System.out.println(countries);
//
//                return jsonToList(countries);


                //This part is task 2. Comment this out up to line 229 to test for task 1
                String urlString = "https://mighty-river-12710.herokuapp.com/";
                URL url = new URL(urlString);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                //Sending POST request through HTTPURLConnection.
                // This code is from https://www.baeldung.com/httpurlconnection-post
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String parameters = "time="+System.currentTimeMillis()+"&param="+"countries"+"&model="+Build.MODEL; //sends time request is made, request parameter, and phone model with the request
                PrintWriter out = new PrintWriter(connection.getOutputStream());
                out.print(parameters);
                out.flush();
                connection.connect();

                //receiving JSONobject response.
                // This part of code is from https://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object
                StringBuilder result = new StringBuilder();
                try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        result.append(responseLine.trim());
                    }
                }

                JSONObject countries = new JSONObject(result.toString());
                return jsonToList(countries); //gets ArrayList of countries from JSONObject response

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;

        }


        /**
         * Parses JSONJSONObject received from the web service to extract country names.
         * @param json JSONObject of response received from web service.
         * @return ArrayList of pure country names
         */
        private ArrayList<String> jsonToList(JSONObject json){
            ArrayList<String> countryList = new ArrayList<>();
            try{
                JSONArray jArray = json.getJSONArray("countries"); //get values from countries field
                if (jArray!=null){
                    for (int i=0; i<jArray.length(); i++){
                        countryList.add(jArray.getString(i));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return countryList;
        }


    }
}
