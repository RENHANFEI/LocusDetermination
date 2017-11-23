package com.example.renhanfei.mylocationmap;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GPSMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsmenu);
    }

    public void onClick_GoToDefaultMap(View view) {
        Intent myIntent = new Intent(this, DefaultMapActivity.class);
        startActivity(myIntent);
    }

    public void onClick_GoToSpecifiedLocationMap(View view) {
        Intent myIntent = new Intent(this, SpecifiedLocationMapActivity.class);
        startActivity(myIntent);
    }

    public void onClick_GoToCurrentLocation(View view) {
        Intent myIntent = new Intent(this, CurrentLocationMapActivity.class);
        startActivity(myIntent);
    }

    public void onClick_GoToRecordsHistory(View view) {
        Intent myIntent = new Intent(this, RecordsActivity.class);
        startActivity(myIntent);
    }
}
