package com.example.renhanfei.phonelocusdetermination;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class PlotActivity extends Activity {

    public long recordID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent myIntent = getIntent();
        recordID = myIntent.getLongExtra("recordID",1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        Log.i("activityRecordID", String.valueOf(recordID));
    }

    public void onClick_back(View v) {
        finish();
    }
}
