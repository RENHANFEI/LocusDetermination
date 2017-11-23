package com.example.renhanfei.phonelocusdetermination;

import android.content.Intent;
import android.icu.text.AlphabeticIndex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick_gotoRecord(View v) {
        Intent myIntent = new Intent(this, RecordActivity.class);
        startActivity(myIntent);
    }

    public void onClick_gotoHistory(View v) {
        Intent myIntent = new Intent(this, HistoryActivity.class);
        startActivity(myIntent);
    }

    public void onClick_gotoMap(View v) {
        Intent myIntent = new Intent(this, MapsActivity.class);
        startActivity(myIntent);
    }

}
