package com.example.renhanfei.phonelocusdetermination;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends Activity {

    LocusDB db;
    private Long[] listViewDic = new Long[999]; // record listview item's id
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = new LocusDB(this);
        ArrayList<String[]> myRecords = getAllRecords();
        displayAllRecords(myRecords);


    }

    public void displayAllRecords(ArrayList<String[]> myRecords) {
        String temp;
        ArrayList<String> Records = new ArrayList<>();

        for (int i = 0; i < myRecords.size(); i++) {
            String[] myRecord = myRecords.get(i);

            temp = "Time: " + myRecord[1] + "  Duration: " + myRecord[2] +
                    "s\nAddress: " + myRecord[3];
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
                gotoHistoryItemsActivity(recordID);
            }
        });
    }

    public void gotoHistoryItemsActivity(long recordID) {
        Intent myIntent = new Intent(this, HistoryItemsActivity.class);
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
                        c.getString(3), "\n"};
                // 0:id 1:TimeStamp 2:Time 3:Address
                records.add(record);
            } while (c.moveToNext());
        }
        db.close();
        return records;
    }

    public void onClick_deleteAll(View view) {
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
    
}
