package com.example.renhanfei.mylocationmap;


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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecordItemsActivity extends Activity {

    private long recordID;
    private Long[] listViewDic = new Long[999]; // record listview item's id
    AccelerometerDB db;
    private AlertDialog.Builder builder;
    ArrayList<String[]> myItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_items);

        recordID = getIntent().getLongExtra("recordID", 0);
        db = new AccelerometerDB(this);

        myItems = getRecordItems();
        displayAllItems(myItems);

    }

    public void displayAllItems(ArrayList<String[]> myItems) {
        String temp;
        ArrayList<String> Items = new ArrayList<>();

        for (int i = 0; i < myItems.size(); i++) {
            String[] myItem = myItems.get(i);

            temp = "TimeStamp: " + myItem[1] + "  Time: " + myItem[2] +
                    "s\nX: " + myItem[5] + "\nY: " + myItem[6];
            // + "\nLat: " + myItem[3] + "  Lng: " + myItem[4];
            ;
            // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
            Items.add(temp);

            listViewDic[i] = Long.valueOf(myItem[0]);
        }

        ListView items_view = (ListView) findViewById(R.id.items_list);

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
                        c.getString(7), "\n"};
                // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
                items.add(item);
            } while (c.moveToNext());
        }
        db.close();
        return items;
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
        Intent myIntent = new Intent(this, AccelerometerRecordsActivity.class);
        startActivity(myIntent);
    }
}

