package com.example.renhanfei.phonelocusdetermination;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by renhanfei on 17/4/2.
 */

public class LocusDB {
    LocusDBHelper DBHelper;
    SQLiteDatabase db;
    final Context context;

    public LocusDB(Context ctx) {
        this.context = ctx;
        DBHelper = new LocusDBHelper(this.context);
    }


    public LocusDB open() {
        db = DBHelper.getWritableDatabase();

        // Toast.makeText(context, Environment.getDataDirectory().toString(), Toast.LENGTH_SHORT).show();

        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public long insertItem(String time_stamp, String record_time, String record_lat,
                           String record_lng, String record_x, String record_y, String position_x,
                           String position_y, String filtered_x, String filtered_y,
                           long record_id, String vx, String vy) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocusDBHelper.columnName_itemTimeStamp, time_stamp);
        initialValues.put(LocusDBHelper.columnName_itemTime, record_time);
        initialValues.put(LocusDBHelper.columnName_itemLat, record_lat);
        initialValues.put(LocusDBHelper.columnName_itemLng, record_lng);
        initialValues.put(LocusDBHelper.columnName_itemX, record_x);
        initialValues.put(LocusDBHelper.columnName_itemY, record_y);
        initialValues.put(LocusDBHelper.columnName_itemPositionX, position_x);
        initialValues.put(LocusDBHelper.columnName_itemPositionY, position_y);
        initialValues.put(LocusDBHelper.columnName_itemFilteredX, filtered_x);
        initialValues.put(LocusDBHelper.columnName_itemFilteredY, filtered_y);
        initialValues.put(LocusDBHelper.columnName_itemRecordID, record_id);
        initialValues.put(LocusDBHelper.columnName_itemVX, vx);
        initialValues.put(LocusDBHelper.columnName_itemVY, vy);

        return db.insert(LocusDBHelper.tableName, null, initialValues);
    }

    // update filtered data
    public int updateItem(long id, String filtered_x, String filtered_y) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocusDBHelper.columnName_itemFilteredX, filtered_x);
        initialValues.put(LocusDBHelper.columnName_itemFilteredY, filtered_y);
        return db.update(LocusDBHelper.tableName, initialValues,
                LocusDBHelper.columnName_itemID + "=" + id, null);
    }

    // update V
    public int updateItemV(long id, String vx, String vy) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocusDBHelper.columnName_itemVX, vx);
        initialValues.put(LocusDBHelper.columnName_itemVY, vy);
        return db.update(LocusDBHelper.tableName, initialValues,
                LocusDBHelper.columnName_itemID + "=" + id, null);
    }

    public long insertRecord(String time_stamp, String record_address) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocusDBHelper.columnName_recordTimeStamp, time_stamp);
        initialValues.put(LocusDBHelper.columnName_recordAddress, record_address);
        long result = -1;
        try {
            result = db.insert(LocusDBHelper.recordTableName, null, initialValues);
        } catch (SQLException e) {
            // Sep 12, 2013 6:50:17 AM
            Log.e("Exception", "SQLException" + String.valueOf(e.getMessage()));
            e.printStackTrace();
        }

        return result;
        //return db.insert(LocusDBHelper.recordTableName, null, initialValues);
    }

    public int updateRecord(long id, String record_time) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocusDBHelper.columnName_recordTime, record_time);
        return db.update(LocusDBHelper.recordTableName, initialValues,
                LocusDBHelper.columnName_recordID + "=" + id, null);
    }

    // delete all items & records
    public int deleteAllRecords() {

        return db.delete(LocusDBHelper.tableName, "1", null) *
                db.delete(LocusDBHelper.recordTableName, "1", null);// delete all records
    }

    // get records for listview
    public Cursor getAllRecords() {
        return db.query(
                LocusDBHelper.recordTableName,
                new String[]{
                        LocusDBHelper.columnName_recordID,
                        LocusDBHelper.columnName_recordTimeStamp,
                        LocusDBHelper.columnName_recordTime,
                        LocusDBHelper.columnName_recordAddress},
                null, null, null, null, null);
    }

    // get items accroding to record id
    public Cursor getItems(long record_id) {
        return db.query(LocusDBHelper.tableName,
                new String[]{
                        LocusDBHelper.columnName_itemID,
                        LocusDBHelper.columnName_itemTimeStamp,
                        LocusDBHelper.columnName_itemTime,
                        LocusDBHelper.columnName_itemLat,
                        LocusDBHelper.columnName_itemLng,
                        LocusDBHelper.columnName_itemX,
                        LocusDBHelper.columnName_itemY,
                        LocusDBHelper.columnName_itemRecordID,
                        LocusDBHelper.columnName_itemPositionX,
                        LocusDBHelper.columnName_itemPositionY,
                        LocusDBHelper.columnName_itemFilteredX,
                        LocusDBHelper.columnName_itemFilteredY,
                        LocusDBHelper.columnName_itemVX,
                        LocusDBHelper.columnName_itemVY},
                LocusDBHelper.columnName_itemRecordID + "=" + record_id,
                null, null, null, null, null);
    }

    // delete record
    public int deleteRecord(long id) {
        return db.delete(LocusDBHelper.recordTableName,
                LocusDBHelper.columnName_recordID + "=" + id, null)
                * db.delete(LocusDBHelper.tableName,
                LocusDBHelper.columnName_itemRecordID + "=" + id, null);
    }

    // delete item
    public int deleteItem(long id) {
        return db.delete(LocusDBHelper.tableName,
                LocusDBHelper.columnName_itemID + "=" + id, null);
    }

}
