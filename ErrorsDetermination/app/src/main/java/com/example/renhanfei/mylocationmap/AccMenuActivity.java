package com.example.renhanfei.mylocationmap;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AccMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_menu);
    }

//    public void onClick_gotoSensorTest(View v){
//        Intent myIntent = new Intent(this, SensorTestActivity.class);
//        startActivity(myIntent);
//    }


    public void onClick_gotoSensorTest(View v) {
        Intent myIntent = new Intent(this, SensorCheckActivity.class);
        startActivity(myIntent);
    }

    public void onClick_gotoTraceRecord(View v) {
        Intent myIntent = new Intent(this, TraceRecordActivity.class);
        startActivity(myIntent);
    }

    public void onClick_gotoTraceHistory(View v) {
        Intent myIntent = new Intent(this, AccelerometerRecordsActivity.class);
        startActivity(myIntent);
    }

}

