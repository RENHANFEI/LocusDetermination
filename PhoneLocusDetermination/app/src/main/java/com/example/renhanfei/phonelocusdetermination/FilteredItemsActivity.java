package com.example.renhanfei.phonelocusdetermination;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

public class FilteredItemsActivity extends Activity {

    public long recordID;
    private Long[] listViewDic = new Long[4096]; // record listview item's id
    LocusDB db;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_items);

        recordID = getIntent().getLongExtra("recordID", 0);
        db = new LocusDB(this);

        ArrayList<String[]> myItems = getRecordItems();
        displayAllItems(myItems);
    }

    public void displayAllItems(ArrayList<String[]> myItems) {
        String temp;
        ArrayList<String> Items = new ArrayList<>();

        for (int i = 0; i < myItems.size(); i++) {
            String[] myItem = myItems.get(i);

            temp = "TimeStamp: " + myItem[1] + "  Time: " + myItem[2] +
                    "\nX: " + myItem[8] + "m\nY: " + myItem[9] +
                    "m\nfilteredX: " + myItem[10] +
                    "m\nfilteredY: " + myItem[11] +
                    "m\nVx: " + myItem[12] + "m/s\nVy: " + myItem[13] + "m/s";

            // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
            // 8:positionX 9:positionY 10:filteredX 11:filteredY 12:vx 13:vy
            Items.add(temp);

            listViewDic[i] = Long.valueOf(myItem[0]);
        }

        ListView items_view = (ListView) findViewById(R.id.filtered_items_list);

        ArrayAdapter<String> items_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, Items);

        items_view.setAdapter(items_adapter);

        items_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long itemID = listViewDic[position];
                confirmSingleDeletionDialog(view, itemID);

            }
        });
    }


    public ArrayList<String[]> getRecordItems() {

        db.open();

        Cursor c = db.getItems(recordID);


        ArrayList<String[]> items = new ArrayList<String[]>();
        int i = 1;

        if (c.moveToFirst()) {
            do {
                String[] item = {c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), c.getString(6),
                        c.getString(7), c.getString(8), c.getString(9), c.getString(10),
                        c.getString(11), c.getString(12), c.getString(13), "\n"};
                // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
                // 8:positionX 9:positionY 10:filteredX 11:filteredY 12:vx 13:vy
                items.add(item);
            } while (c.moveToNext());
        }
        db.close();
        return items;
    }

    public int updateItem(long id, String filteredX, String filteredY) {
        db.open();

        int num_records_updated = db.updateItem(id, filteredX, filteredY);

        db.close();

        return num_records_updated;
    }

    public int updateItemV(long id, String VX, String VY) {
        db.open();

        int num_records_updated = db.updateItemV(id, VX, VY);

        db.close();

        return num_records_updated;
    }

    public void onClick_deleteRecord(View v) {
        confirmDeletionDialog(v);
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

                int num_records_deleted = db.deleteRecord(recordID);

                db.close();


                Toast.makeText(getApplicationContext(), "Successfully delete. ",
                        Toast.LENGTH_SHORT).show();

                // if just finish(), cannot refresh
                goBackRecords();
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

    private void confirmSingleDeletionDialog(View view, final long itemID) {

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Confirm Delete this item?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // delete all records

                db.open();

                int num_deleted = db.deleteItem(itemID);

                db.close();

                ArrayList<String[]> myItems = getRecordItems();

                displayAllItems(myItems);
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

        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void goBackRecords() {
        Intent myIntent = new Intent(this, HistoryActivity.class);
        startActivity(myIntent);
    }

    public void onClick_UpdateFiltering(View view) {

        // updateVelocity();

        ArrayList<String[]> myItems = getRecordItems();

        if (myItems.size() == 0)
            return;

        KalmanFilter kalmanFilter = new KalmanFilter();
        KalmanStatus preStatus = new KalmanStatus();
        KalmanStatus curStatus = new KalmanStatus();

        // first
        preStatus.estimation[0] = Double.valueOf(myItems.get(0)[8]);
        preStatus.estimation[1] = Double.valueOf(myItems.get(0)[9]);
        preStatus.estimation[2] = Double.valueOf(myItems.get(0)[12]);
        preStatus.estimation[3] = Double.valueOf(myItems.get(0)[13]);
        double preAx = Double.valueOf(myItems.get(0)[5]);
        double preAy = Double.valueOf(myItems.get(0)[6]);

        // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
        // 8:positionX 9:positionY 10:filteredX 11:filteredY 12:vx 13:vy

        double curAx;
        double curAy;

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                preStatus.error[i][j] = 0;

        updateItem(Long.valueOf(myItems.get(0)[0]),
                String.valueOf(myItems.get(0)[8]),
                String.valueOf(myItems.get(0)[9]));

        for (int k = 1; k < myItems.size(); k++) {

            curStatus.measurement[0] = Double.valueOf(myItems.get(k)[8]);
            curStatus.measurement[1] = Double.valueOf(myItems.get(k)[9]);
            curStatus.measurement[2] = Double.valueOf(myItems.get(k)[12]);
            curStatus.measurement[3] = Double.valueOf(myItems.get(k)[13]);
            // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
            // 8:positionX 9:positionY 10:filteredX 11:filteredY 12:vx 13:vy


            curAx = Double.valueOf(myItems.get(k)[5]);
            curAy = Double.valueOf(myItems.get(k)[6]);


            kalmanFilter.preStatus = preStatus;
            kalmanFilter.curStatus = curStatus;
            kalmanFilter.accX = (preAx + curAx) / 2;
            kalmanFilter.accY = (preAy + curAy) / 2;
            kalmanFilter.doFiltering();

            double filteredX = kalmanFilter.curStatus.estimation[0];
            double filteredY = kalmanFilter.curStatus.estimation[1];

//            TextView debugview = (TextView) findViewById(R.id.debugview);
//            debugview.setText("k: " + String.valueOf(k) +
//                    "\nx: " + String.valueOf(myItems.get(k)[8]) +
//                    "\ny: " + String.valueOf(myItems.get(k)[9]) +
//                    "\nfx: " + String.valueOf(myItems.get(k)[10]) +
//                    "\nfy: " + String.valueOf(myItems.get(k)[11]) +
//                    "\nffx: " + String.valueOf(filteredX) +
//                    "\nffy: " + String.valueOf(filteredY));

            // update databse
            updateItem(Long.valueOf(myItems.get(k)[0]),
                    String.valueOf(filteredX),
                    String.valueOf(filteredY));

            // update
            cloneStatus(curStatus, preStatus);
            preAx = curAx;
            preAy = curAy;

        }

        ArrayList<String[]> newItems = getRecordItems();
        displayAllItems(newItems);
    }

    public void updateVelocity() {

        double preAx = 0, preAy = 0, preVx = 0, preVy = 0;
        double timeInterval = 0.2;

        ArrayList<String[]> myItems = getRecordItems();

        // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
        // 8:positionX 9:positionY 10:filteredX 11:filteredY 12:vx 13:vy

        for (int k = 0; k < myItems.size(); k++) {
            String[] myItem = myItems.get(k);

            double curAx = Double.valueOf(myItem[5]);
            double curAy = Double.valueOf(myItem[6]);
            double curVx = preVx + (preAx + curAx) / 2 * timeInterval;
            double curVy = preVy + (preAy + curAy) / 2 * timeInterval;

            updateItemV(Long.valueOf(myItem[0]), String.valueOf(curVx), String.valueOf(curVy));

            preAx = curAx;
            preAy = curAy;
            preVx = curVx;
            preVy = curVy;
        }
    }

    public void cloneStatus(KalmanStatus source, KalmanStatus target) {
//        public double[] advanceEstimation;
//        public double[] estimation;
//        public double[][] advanceError; // P-
//        public double[][] error; // P
//        public double[] measurement;
//        public double[][] kalmanGain;
//        public double time; // from start unit:s

        target.estimation = source.estimation.clone();

        for (int i = 0; i < 4; i++) {
            target.error[i] = source.error[i].clone();
        }
    }

    public void onClick_plot(View v) {
        Intent myIntent = new Intent(this, PlotActivity.class);
        myIntent.putExtra("recordID", recordID);
        startActivity(myIntent);
    }

    public void onClick_back(View v) {
        finish();
    }

}
