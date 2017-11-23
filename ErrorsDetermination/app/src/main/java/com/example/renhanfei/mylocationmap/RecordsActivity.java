package com.example.renhanfei.mylocationmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

public class RecordsActivity extends Activity {

    LocationRecordsDB db;
    private int clickedItem = 0;
    private Long[] listViewDic = new Long[999]; // record listview item's id
    int pointNum = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        db = new LocationRecordsDB(this);

        ArrayList<String[]> myRecords = getAllRecords();
        displayAllRecords(myRecords);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void displayAllRecords(ArrayList<String[]> myRecords) {
        String temp;
        ArrayList<String> Records = new ArrayList<>();

        for (int i = 0; i < myRecords.size(); i++) {
            String[] myRecord = myRecords.get(i);

            // Capitalizes the first letter of position
            char[] recordPosition = myRecord[2].toCharArray();
            recordPosition[0] -= 32;

            temp = myRecord[1] + " " + String.valueOf(recordPosition) + "\n"
                    + myRecord[3] + " " + myRecord[4];
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
                clickedItem = position;
                long itemID = listViewDic[clickedItem];
//                Toast.makeText(getApplicationContext(), String.valueOf(listViewDic[clickedItem]),
//                        Toast.LENGTH_SHORT).show();
                confirmSingleDeletionDialog(view, itemID);
                // showupdateDialog(parent);
            }
        });
    }


    public ArrayList<String[]> getAllRecords() {

        db.open();

        Cursor c = db.getAllRecords();

        ArrayList<String[]> records = new ArrayList<String[]>();

        if (c.moveToFirst()) {
            do {
                String[] record = {c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), "\n"};
                // 0:id 1:Time 2:Position 3:latitude 4:longitude 5:address
                records.add(record);
            } while (c.moveToNext());
        }
        db.close();
        return records;
    }


    public void onClick_deleteAllRecords(View view) {
        confirmDeletionDialog(view);
    }

    private AlertDialog.Builder builder;

    private void confirmDeletionDialog(View view) {

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Confirm Deletion?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // delete all records

                db.open();

                int num_records_deleted = db.deleteAllLocation();

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

    private void confirmSingleDeletionDialog(View view, final long itemID) {

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Confirm Delete this record?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // delete all records

                db.open();

                int num_records_deleted = deleteRecord(itemID);

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

    public int deleteRecord(long id) {
        db.open();

        int num_records_deleted = db.deleteLocation(id);

        db.close();

        return num_records_deleted;
    }

    // draw
    public void onClick_draw(View v) {
        Intent myIntent = new Intent(this, LocationPlotActivity.class);
        startActivity(myIntent);
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

        double[][] east = new double[pointNum][2];
        double[][] south = new double[pointNum][2];
        double[][] west = new double[pointNum][2];
        double[][] north = new double[pointNum][2];

        int eastCursor = 0;
        int southCursor = 0;
        int westCursor = 0;
        int northCursor = 0;

        // 0: lat mean, 1: lng mean, 2: lat variance, 3: lng variance
        double[] eastAnalysis = {0, 0, 0, 0};
        double[] southAnalysis = {0, 0, 0, 0};
        double[] westAnalysis = {0, 0, 0, 0};
        double[] northAnalysis = {0, 0, 0, 0};

        double[] tempPoint = new double[2];

        ArrayList<String[]> myRecords = getAllRecords();
        String temp, position;

        for (int k = 0; k < myRecords.size(); k++) {

            String[] myRecord = myRecords.get(k);
            // 0:id 1:Time 2:Position 3:latitude 4:longitude 5:address

            position = myRecord[2];
            tempPoint[0] = Double.valueOf(myRecord[3]);
            tempPoint[1] = Double.valueOf(myRecord[4]);

            if (myRecord[2].equalsIgnoreCase("east")) {
                eastAnalysis[0] += tempPoint[0];
                eastAnalysis[1] += tempPoint[1];
                east[eastCursor][0] = tempPoint[0];
                east[eastCursor][1] = tempPoint[1];
                eastCursor++;
            } else if (myRecord[2].equalsIgnoreCase("south")) {
                southAnalysis[0] += tempPoint[0];
                southAnalysis[1] += tempPoint[1];
                south[southCursor][0] = tempPoint[0];
                south[southCursor][1] = tempPoint[1];
                southCursor++;
            } else if (myRecord[2].equalsIgnoreCase("west")) {
                westAnalysis[0] += tempPoint[0];
                westAnalysis[1] += tempPoint[1];
                west[westCursor][0] = tempPoint[0];
                west[westCursor][1] = tempPoint[1];
                westCursor++;
            } else { // north
                northAnalysis[0] += tempPoint[0];
                northAnalysis[1] += tempPoint[1];
                north[northCursor][0] = tempPoint[0];
                north[northCursor][1] = tempPoint[1];
                northCursor++;
            }

        }

        if (eastCursor > 0) {
            eastAnalysis[0] /= eastCursor;
            eastAnalysis[1] /= eastCursor;
        }

        if (southCursor > 0) {
            southAnalysis[0] /= southCursor;
            southAnalysis[1] /= southCursor;
        }

        if (westCursor > 0) {
            westAnalysis[0] /= westCursor;
            westAnalysis[1] /= westCursor;
        }

        if (northCursor > 0) {
            northAnalysis[0] /= northCursor;
            northAnalysis[1] /= northCursor;
        }


        for (int k = 0; k < eastCursor; k++) {
            eastAnalysis[2] += (east[k][0] - eastAnalysis[0]) * (east[k][0] - eastAnalysis[0]);
            eastAnalysis[3] += (east[k][1] - eastAnalysis[1]) * (east[k][1] - eastAnalysis[1]);
        }

        for (int k = 0; k < southCursor; k++) {
            southAnalysis[2] += (south[k][0] - southAnalysis[0]) * (south[k][0] - southAnalysis[0]);
            southAnalysis[3] += (south[k][1] - southAnalysis[1]) * (south[k][1] - southAnalysis[1]);
        }

        for (int k = 0; k < westCursor; k++) {
            westAnalysis[2] += (west[k][0] - westAnalysis[0]) * (west[k][0] - westAnalysis[0]);
            westAnalysis[3] += (west[k][1] - westAnalysis[1]) * (west[k][1] - westAnalysis[1]);
        }

        for (int k = 0; k < northCursor; k++) {
            northAnalysis[2] += (north[k][0] - northAnalysis[0]) * (north[k][0] - northAnalysis[0]);
            northAnalysis[3] += (north[k][1] - northAnalysis[1]) * (north[k][1] - northAnalysis[1]);
        }


        int scale = 6;
        eastAnalysis[2] = significand(eastAnalysis[2] / eastCursor, scale);
        eastAnalysis[3] = significand(eastAnalysis[3] / eastCursor, scale);
        southAnalysis[2] = significand(southAnalysis[2] / southCursor, scale);
        southAnalysis[3] = significand(southAnalysis[3] / southCursor, scale);
        westAnalysis[2] = significand(westAnalysis[2] / westCursor, scale);
        westAnalysis[3] = significand(westAnalysis[3] / westCursor, scale);
        northAnalysis[2] = significand(northAnalysis[2] / northCursor, scale);
        northAnalysis[3] = significand(northAnalysis[3] / northCursor, scale);


        String analysisInfo;

        analysisInfo =
                "Pos  |     Lat Mean     |     Lng Mean\n" +
                        "  E         " + String.format("%.7f", eastAnalysis[0]) +
                        "       " + String.format("%.7f", eastAnalysis[1]) + "\n" +
                        "  S         " + String.format("%.7f", southAnalysis[0]) +
                        "       " + String.format("%.7f", southAnalysis[1]) + "\n" +
                        "  W        " + String.format("%.7f", westAnalysis[0]) +
                        "       " + String.format("%.7f", westAnalysis[1]) + "\n" +
                        "  N         " + String.format("%.7f", northAnalysis[0]) +
                        "       " + String.format("%.7f", northAnalysis[1]) + "\n\n";

        analysisInfo +=
                "Pos  |      Lat Var       |     Lng Var\n" +
                        "  E        " + String.valueOf(eastAnalysis[2]) +
                        "       " + String.valueOf(eastAnalysis[3]) + "\n" +
                        "  S        " + String.valueOf(southAnalysis[2]) +
                        "         " + String.valueOf(southAnalysis[3]) + "\n" +
                        "  W       " + String.valueOf(westAnalysis[2]) +
                        "       " + String.valueOf(westAnalysis[3]) + "\n" +
                        "  N        " + String.valueOf(northAnalysis[2]) +
                        "       " + String.valueOf(northAnalysis[3]) + "\n";

        return analysisInfo;
    }

    public static double significand(double oldDouble, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "scale指定的精度为非负值");
        }
        /**
         * RoundingMode：舍入模式 
         * UP：远离零方向舍入的舍入模式； 
         * DOWN：向零方向舍入的舍入模式； 
         * CEILING： 向正无限大方向舍入的舍入模式； 
         * FLOOR：向负无限大方向舍入的舍入模式； 
         * HALF_DOWN：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向下舍入； 
         * HALF_UP：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向上舍入； 
         * HALF_EVEN：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向相邻的偶数舍入;(在重复进行一系列计算时,此舍入模式可以将累加错误减到最小) 
         * UNNECESSARY：用于断言请求的操作具有精确结果的舍入模式，因此不需要舍入。 
         */
        RoundingMode rMode = null;
        //rMode=RoundingMode.FLOOR;  
        //下面这种情况，其实和FLOOR一样的。  
        if (oldDouble > 0) {
            rMode = RoundingMode.DOWN;
        } else {
            rMode = RoundingMode.UP;
        }
        //此处的scale表示的是，几位有效位数  
        BigDecimal b = new BigDecimal(Double.toString(oldDouble), new MathContext(scale, rMode));
        return b.doubleValue();
    }


//    public int updateRecord(long id, String record_position, String record_address) {
//        db.open();
//
//        int num_records_updated = db.updateLocation(id, record_position, record_address);
//
//        db.close();
//
//        return num_records_updated;
//    }
//
//    public String[] getRecord(long id) {
//
//        db.open();
//
//        Cursor c = db.getLocation(id);
//
//        String[] record = new String[3];
//
//        if (c.moveToFirst()) {
//            String[] temp = {c.getString(0), c.getString(1), c.getString(2),
//                    c.getString(3), c.getString(4), c.getString(5)};
//            record = temp;
//        } else
//            Toast.makeText(this, "No contact found.", Toast.LENGTH_SHORT).show();
//
//
//        db.close();
//
//        return record;
//    }
}
