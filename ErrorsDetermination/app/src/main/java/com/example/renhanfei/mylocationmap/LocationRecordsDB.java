package com.example.renhanfei.mylocationmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

/**
 * Created by renhanfei on 17/3/9.
 */

public class LocationRecordsDB {
    LocationRecordsDBHelper DBHelper;
    SQLiteDatabase db;
    final Context context;

    public LocationRecordsDB(Context ctx) {
        this.context = ctx;
        DBHelper = new LocationRecordsDBHelper(this.context);
    }


    public LocationRecordsDB open() {
        db = DBHelper.getWritableDatabase();

        // Toast.makeText(context, Environment.getDataDirectory().toString(), Toast.LENGTH_SHORT).show();

        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public long insertLocation(String record_time, String record_position,String record_latitude,
                               String record_longitude,String record_address) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocationRecordsDBHelper.columnName_itemTime, record_time);
        initialValues.put(LocationRecordsDBHelper.columnName_itemPosition, record_position);
        initialValues.put(LocationRecordsDBHelper.columnName_itemLatitude, record_latitude);
        initialValues.put(LocationRecordsDBHelper.columnName_itemLongitude, record_longitude);
        initialValues.put(LocationRecordsDBHelper.columnName_itemAddress, record_address);

        return db.insert(LocationRecordsDBHelper.tableName, null, initialValues);
    }

    public int deleteLocation(long id) {
        return db.delete(LocationRecordsDBHelper.tableName, LocationRecordsDBHelper.columnName_itemID + "=" + id, null);
    }

    public int deleteAllLocation() {
        return db.delete(LocationRecordsDBHelper.tableName, "1", null);    // delete all records
    }

    public int updateLocation(long id, String record_position, String record_address) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocationRecordsDBHelper.columnName_itemPosition, record_position);
        initialValues.put(LocationRecordsDBHelper.columnName_itemAddress, record_address);
        return db.update(LocationRecordsDBHelper.tableName, initialValues,
                LocationRecordsDBHelper.columnName_itemID + "=" + id, null);
    }

    public Cursor getAllRecords() {
        return db.query(
                LocationRecordsDBHelper.tableName,
                new String[]{
                        LocationRecordsDBHelper.columnName_itemID,
                        LocationRecordsDBHelper.columnName_itemTime,
                        LocationRecordsDBHelper.columnName_itemPosition,
                        LocationRecordsDBHelper.columnName_itemLatitude,
                        LocationRecordsDBHelper.columnName_itemLongitude,
                        LocationRecordsDBHelper.columnName_itemAddress},
                null, null, null, null, null);
    }


    public Cursor getLocation(long id) {
        Cursor mCursor = db.query(LocationRecordsDBHelper.tableName,
                new String[]{
                        LocationRecordsDBHelper.columnName_itemID,
                        LocationRecordsDBHelper.columnName_itemPosition,
                        LocationRecordsDBHelper.columnName_itemLatitude,
                        LocationRecordsDBHelper.columnName_itemLongitude,
                        LocationRecordsDBHelper.columnName_itemAddress},
                LocationRecordsDBHelper.columnName_itemID + "=" + id,
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}
