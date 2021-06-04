package edu.cmu.kowunk;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.*;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Author: Ko-Wun Kim
 * Last Modified: April 8, 2021
 *
 * This is an Android App that gets country's COVID confirmed, deaths, and recovered cases.
 * It connects to a web service to send request and receive response.
 * Then the received response is displayed back to the user.
 */
public class CountryCOVIDAndroid extends AppCompatActivity {

    Spinner countrySpinner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final CountryCOVIDAndroid my = this;

        countrySpinner = (Spinner) findViewById(R.id.countrySpinner);

        Button submitButton = (Button) findViewById(R.id.submitButton);

        GetData gd = new GetData();
        gd.getCountries(my); //call GetData object's getCountries method to populate country list on app initialization

        //add listener to submit button
        submitButton.setOnClickListener(viewParam -> {
            String selectedCountry = countrySpinner.getSelectedItem().toString();
            gd.getData(selectedCountry); //done asynchronously in another thread. It calls dataReady method when complete.

        });

    }

    /**
     * Called by GetData object when country list is ready. The country list is used to initialize the spinner
     * @param countries list of all countries
     */
    public void fillCountries(ArrayList<String> countries){
        if (countries!=null){
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countries);
            countrySpinner.setAdapter(adapter);
        }

    }

    /**
     * Called by GetData object when country's COVID case data is ready.
     * @param data JSONObject of COVID case data of the user selected country
     */
    public void dataReady(JSONObject data){
        TextView confirmedDataView = (TextView) findViewById(R.id.confirmedData);
        TextView confirmedDataLabel = (TextView) findViewById(R.id.confirmedLabel);
        TextView deathsDataView = (TextView) findViewById(R.id.deathsData);
        TextView deathsDataLabel = (TextView) findViewById(R.id.deathsLabel);
        TextView recoveredDataView = (TextView) findViewById(R.id.recoveredData);
        TextView recoveredDataLabel = (TextView) findViewById(R.id.recoveredLabel);

        try{
            if (data!= null){
                if (data.has("confirmed")) {
                    Integer value = data.getInt("confirmed");

                    //formatting number into #,###,### format. This code is from
                    //https://developer.android.com/reference/java/text/NumberFormat
                    String formattedValue = NumberFormat.getInstance().format(value);
                    confirmedDataView.setText(formattedValue);
                    confirmedDataLabel.setText("Confirmed: ");
                }
                if (data.has("deaths")) {
                    Integer value = data.getInt("deaths");
                    String formattedValue = NumberFormat.getInstance().format(value);
                    deathsDataView.setText(formattedValue);
                    deathsDataLabel.setText("Deaths: ");
                }
                if (data.has("recovered")) {
                    Integer value = data.getInt("recovered");
                    String formattedValue = NumberFormat.getInstance().format(value);
                    recoveredDataView.setText(formattedValue);
                    recoveredDataLabel.setText("Recovered: ");
                }


            }
        } catch(JSONException e){
            e.printStackTrace();
        }



    }

}