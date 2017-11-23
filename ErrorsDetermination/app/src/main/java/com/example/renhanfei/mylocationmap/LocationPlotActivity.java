package com.example.renhanfei.mylocationmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LocationPlotActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_plot);

    }

    public void onClick_goBack(View v) {
        finish();
    }

    public void onClick_switchMap(View v) {
        Intent myIntent = new Intent(this, MapPlotActivity.class);
        startActivity(myIntent);
    }

}
