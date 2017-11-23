package com.example.renhanfei.mylocationmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AccelerometerRecordsActivity extends Activity {

    AccelerometerDB db;
    private Long[] listViewDic = new Long[999]; // record listview item's id
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_records);

        db = new AccelerometerDB(this);
        ArrayList<String[]> myRecords = getAllRecords();
        displayAllRecords(myRecords);
    }

    public void displayAllRecords(ArrayList<String[]> myRecords) {
        String temp;
        ArrayList<String> Records = new ArrayList<>();

        for (int i = 0; i < myRecords.size(); i++) {
            String[] myRecord = myRecords.get(i);

            temp = "Time: " + myRecord[1] + "  Duration: " + myRecord[2] +
                    "s\nAddress: " + myRecord[3] + "\nDirection: " + myRecord[4] +
                    "  Error:" + myRecord[5]; //+ "\nID: " + myRecord[0];
            Records.add(temp);

            listViewDic[i] = Long.valueOf(myRecord[0]);
        }

        ListView records_view = (ListView) findViewById(R.id.records_list);

        ArrayAdapter<String> records_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, Records);

        records_view.setAdapter(records_adapter);

        records_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long recordID = listViewDic[position];
                gotoRecordItemsActivity(recordID);
            }
        });
    }

    public void gotoRecordItemsActivity(long recordID) {
        Intent myIntent = new Intent(this, RecordItemsActivity.class);
        myIntent.putExtra("recordID", recordID);
        startActivity(myIntent);
    }


    public ArrayList<String[]> getAllRecords() {

        db.open();

        Cursor c = db.getAllRecords();

        ArrayList<String[]> records = new ArrayList<String[]>();

        if (c.moveToFirst()) {
            do {
                String[] record = {c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), "\n"};
                // 0:id 1:TimeStamp 2:Time 3:Address 4: Orientation 5:Error
                records.add(record);
            } while (c.moveToNext());
        }
        db.close();
        return records;
    }

    public void onClick_deleteAllRecords(View view) {
        confirmDeletionDialog(view);
    }


    private void confirmDeletionDialog(View view) {

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Confirm Deletion?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // delete all records

                db.open();

                int num_records_deleted = db.deleteAllRecords();

                db.close();

                ArrayList<String[]> myRecords = getAllRecords();

                displayAllRecords(myRecords);
                Toast.makeText(getApplicationContext(), "Successfully delete. ",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Deletion cancelled. ",
                        Toast.LENGTH_SHORT).show();
            }
        });

        //设置对话框是可取消的
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // show analysis (mean & variance)
    public void onClick_analysis(View v) {
        showAnalysis(v);
    }

    private void showAnalysis(View view) {

        String analysisInfo = getAnalysis();

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Analysis");
        builder.setMessage(analysisInfo);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                Toast.makeText(getApplicationContext(), "Show Analysis.",
//                        Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getAnalysis() {

        double[] xPlusError = new double[999];
        double[] xMinusError = new double[999];
        double[] yPlusError = new double[999];
        double[] yMinusError = new double[999];

        double xPlusErrorMean = 0;
        double xMinusErrorMean = 0;
        double yPlusErrorMean = 0;
        double yMinusErrorMean = 0;
        double xPlusErrorVar = 0;
        double xMinusErrorVar = 0;
        double yPlusErrorVar = 0;
        double yMinusErrorVar = 0;

        int xPlusCursor = 0;
        int xMinusCursor = 0;
        int yPlusCursor = 0;
        int yMinusCursor = 0;

        double xErrorMean = 0;
        double yErrorMean = 0;
        double xErrorVar = 0;
        double yErrorVar = 0;


        ArrayList<String[]> myRecords = getAllRecords();

        for (int i = 0; i < myRecords.size(); i++) {
            String[] myRecord = myRecords.get(i);

            String orientation = myRecord[4];
            double error = Double.valueOf(myRecord[5]);

            if (orientation.equalsIgnoreCase("xplus")) {
                xPlusError[xPlusCursor++] = error;
                xPlusErrorMean += error;
            } else if (orientation.equalsIgnoreCase("xminus")) {
                xMinusError[xMinusCursor++] = error;
                xMinusErrorMean += error;
            } else if (orientation.equalsIgnoreCase("yplus")) {
                yPlusError[yPlusCursor++] = error;
                yPlusErrorMean += error;
            } else { // yminus
                yMinusError[yMinusCursor++] = error;
                yMinusErrorMean += error;
            }

        }

        // get mean error
        if (xPlusCursor > 0) {
            xPlusErrorMean /= xPlusCursor;
        }
        if (xMinusCursor > 0) {
            xMinusErrorMean /= xMinusCursor;
        }
        if (yPlusCursor > 0) {
            yPlusErrorMean /= yPlusCursor;
        }
        if (yMinusCursor > 0) {
            yMinusErrorMean /= yMinusCursor;
        }

        // get variance
        for (int i = 0; i < xPlusCursor; i++) {
            xPlusErrorVar += (xPlusError[i] - xPlusErrorMean) * (xPlusError[i] - xPlusErrorMean);
        }
        for (int i = 0; i < xMinusCursor; i++) {
            xMinusErrorVar += (xMinusError[i] - xMinusErrorMean) * (xMinusError[i] - xMinusErrorMean);

        }
        for (int i = 0; i < yPlusCursor; i++) {
            yPlusErrorVar += (yPlusError[i] - yPlusErrorMean) * (yPlusError[i] - yPlusErrorMean);
        }
        for (int i = 0; i < yMinusCursor; i++) {
            yMinusErrorVar += (yMinusError[i] - yMinusErrorMean) * (yMinusError[i] - yMinusErrorMean);
        }

        // get 2 directions' analysis data
        // mean
        if (xPlusCursor + xMinusCursor > 0) {
            xErrorMean = (xPlusErrorMean * xPlusCursor - xMinusErrorMean * xMinusCursor)
                    / (xPlusCursor + xMinusCursor); // unify to +x
        }
        if (yPlusCursor + yMinusCursor > 0) {
            yErrorMean = (yPlusErrorMean * yPlusCursor - yMinusErrorMean * yMinusCursor)
                    / (yPlusCursor + yMinusCursor); // unify to +y
        }

        // variance
        if (xPlusCursor + xMinusCursor > 0) {
            xErrorVar = (xPlusErrorVar * xPlusCursor + xMinusErrorVar * xMinusCursor)
                    / (xPlusCursor + xMinusCursor);
        }
        if (yPlusCursor + yMinusCursor > 0) {
            yErrorVar = (yPlusErrorVar * yPlusCursor + yMinusErrorVar * yMinusCursor)
                    / (yPlusCursor + yMinusCursor);
        }

        String analysisInfo;

        analysisInfo =
                "Pos     |        Mean        |     Variance\n\n" +
                        " +X         " + String.format("%.7f", xPlusErrorMean) +
                        "       " + String.format("%.7f", xPlusErrorVar) + "\n" +
                        " -X         " + String.format("%.7f", xMinusErrorMean) +
                        "       " + String.format("%.7f", xMinusErrorVar) + "\n" +
                        " +Y        " + String.format("%.7f", yPlusErrorMean) +
                        "       " + String.format("%.7f", yPlusErrorVar) + "\n" +
                        " -Y         " + String.format("%.7f", yMinusErrorMean) +
                        "       " + String.format("%.7f", yMinusErrorVar) + "\n\n" +
                        "  X        " + String.format("%.7f", xErrorMean) +
                        "       " + String.format("%.7f", xErrorVar) + "\n" +
                        "  Y         " + String.format("%.7f", yErrorMean) +
                        "       " + String.format("%.7f", yErrorVar) + "\n";

        return analysisInfo;
    }
}
