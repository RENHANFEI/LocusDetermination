package com.example.renhanfei.mylocationmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void onClick_gotoGPS(View v){
        Intent myIntent = new Intent(this, GPSMenuActivity.class);
        startActivity(myIntent);
    }

    public void onClick_gotoAcc(View v){
        Intent myIntent = new Intent(this, AccMenuActivity.class);
        startActivity(myIntent);
    }

    public void onClick_gotoMeasurementCov(View v){
        Intent myIntent = new Intent(this, MeasurementAnalysisActivity.class);
        startActivity(myIntent);
    }
}
