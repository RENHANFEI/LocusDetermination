package com.example.renhanfei.mylocationmap;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by renhanfei on 17/3/29.
 */

public class AccelerometerDB {
    AccelerometerDBHelper DBHelper;
    SQLiteDatabase db;
    final Context context;

    public AccelerometerDB(Context ctx) {
        this.context = ctx;
        DBHelper = new AccelerometerDBHelper(this.context);
    }


    public AccelerometerDB open() {
        db = DBHelper.getWritableDatabase();

        // Toast.makeText(context, Environment.getDataDirectory().toString(), Toast.LENGTH_SHORT).show();

        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public long insertItem(String time_stamp, String record_time, String record_lat,
                           String record_lng, String record_x, String record_y, long record_id) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(AccelerometerDBHelper.columnName_itemTimeStamp, time_stamp);
        initialValues.put(AccelerometerDBHelper.columnName_itemTime, record_time);
        initialValues.put(AccelerometerDBHelper.columnName_itemLat, record_lat);
        initialValues.put(AccelerometerDBHelper.columnName_itemLng, record_lng);
        initialValues.put(AccelerometerDBHelper.columnName_itemX, record_x);
        initialValues.put(AccelerometerDBHelper.columnName_itemY, record_y);
        initialValues.put(AccelerometerDBHelper.columnName_itemRecordID, record_id);

        return db.insert(AccelerometerDBHelper.tableName, null, initialValues);
    }

    public long insertRecord(String time_stamp, String record_address, String record_orientation) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(AccelerometerDBHelper.columnName_recordTimeStamp, time_stamp);
        initialValues.put(AccelerometerDBHelper.columnName_recordAddress, record_address);
        initialValues.put(AccelerometerDBHelper.columnName_recordOrientation, record_orientation);

        return db.insert(AccelerometerDBHelper.recordTableName, null, initialValues);
    }

    public int updateRecord(long id, String record_time, String record_error, String other_error) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(AccelerometerDBHelper.columnName_recordTime, record_time);
        initialValues.put(AccelerometerDBHelper.columnName_recordError, record_error);
        initialValues.put(AccelerometerDBHelper.columnName_recordOtherError, other_error);
        return db.update(AccelerometerDBHelper.recordTableName, initialValues,
                AccelerometerDBHelper.columnName_recordID + "=" + id, null);
    }

    // delete all items & records
    public int deleteAllRecords() {

        return db.delete(AccelerometerDBHelper.tableName, "1", null) *
                db.delete(AccelerometerDBHelper.recordTableName, "1", null);// delete all records
    }

    // get records for listview
    public Cursor getAllRecords() {
        return db.query(
                AccelerometerDBHelper.recordTableName,
                new String[]{
                        AccelerometerDBHelper.columnName_recordID,
                        AccelerometerDBHelper.columnName_recordTimeStamp,
                        AccelerometerDBHelper.columnName_recordTime,
                        AccelerometerDBHelper.columnName_recordAddress,
                        AccelerometerDBHelper.columnName_recordOrientation,
                        AccelerometerDBHelper.columnName_recordError,
                        AccelerometerDBHelper.columnName_recordOtherError},
                null, null, null, null, null);
    }

    // get items accroding to record id
    public Cursor getItems(long record_id) {
        return db.query(AccelerometerDBHelper.tableName,
                new String[]{
                        AccelerometerDBHelper.columnName_itemID,
                        AccelerometerDBHelper.columnName_itemTimeStamp,
                        AccelerometerDBHelper.columnName_itemTime,
                        AccelerometerDBHelper.columnName_itemLat,
                        AccelerometerDBHelper.columnName_itemLng,
                        AccelerometerDBHelper.columnName_itemX,
                        AccelerometerDBHelper.columnName_itemY,
                        AccelerometerDBHelper.columnName_itemRecordID},
                AccelerometerDBHelper.columnName_itemRecordID + "=" + record_id,
                null, null, null, null, null);
    }

    // delete record
    public int deleteRecord(long id) {
        return db.delete(AccelerometerDBHelper.recordTableName,
                AccelerometerDBHelper.columnName_recordID + "=" + id, null)
                * db.delete(AccelerometerDBHelper.tableName,
                AccelerometerDBHelper.columnName_itemRecordID + "=" + id, null);
    }

    // delete item
    public int deleteItem(long id) {
        return db.delete(AccelerometerDBHelper.tableName,
                AccelerometerDBHelper.columnName_itemID + "=" + id, null);
    }


//    public Cursor getRecord(long id) {
//        Cursor mCursor = db.query(AccelerometerDBHelper.tableName,
//                new String[]{
//                        AccelerometerDBHelper.columnName_itemID,
//                        AccelerometerDBHelper.columnName_itemTimeStamp,
//                        AccelerometerDBHelper.columnName_itemTime,
//                        AccelerometerDBHelper.columnName_itemLat,
//                        AccelerometerDBHelper.columnName_itemLng,
//                        AccelerometerDBHelper.columnName_itemX,
//                        AccelerometerDBHelper.columnName_itemY},
//                AccelerometerDBHelper.columnName_itemID + "=" + id,
//                null, null, null, null, null);
//
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//        }
//        return mCursor;
//    }

}
