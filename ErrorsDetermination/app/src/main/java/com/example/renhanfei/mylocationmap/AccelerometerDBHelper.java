package com.example.renhanfei.mylocationmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.camera2.params.StreamConfigurationMap;

/**
 * Created by renhanfei on 17/3/29.
 */

public class AccelerometerDBHelper extends SQLiteOpenHelper {
    public static final int databaseVersion = 2;
    public static final String databaseName = "accelerometerDB";

    // it is for item
    public static final String tableName = "itemTable";
    public static final String columnName_itemID = "_id";
    public static final String columnName_itemTimeStamp = "itemTimeStamp";
    public static final String columnName_itemTime = "itemTime";
    public static final String columnName_itemLat = "itemLat";
    public static final String columnName_itemLng = "itemLng";
    public static final String columnName_itemX = "itemX";
    public static final String columnName_itemY = "itemY";
    public static final String columnName_itemRecordID = "recordID";

    private static final String SQLite_CREATE =
            "CREATE TABLE " + tableName + "(" + columnName_itemID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + columnName_itemTimeStamp + " TEXT NOT NULL,"
                    + columnName_itemTime + " TEXT NOT NULL,"
                    + columnName_itemLat + " TEXT NOT NULL,"
                    + columnName_itemLng + " TEXT NOT NULL,"
                    + columnName_itemX + " TEXT NOT NULL,"
                    + columnName_itemY + " TEXT NOT NULL,"
                    + columnName_itemRecordID + " INTEGER NOT NULL);";

    private static final String SQLite_DELETE = "DROP TABLE IF EXISTS " + tableName;


    // it is for records (every records have some items)
    public static final String recordTableName = "recordTable";
    public static final String columnName_recordID = "_id";
    public static final String columnName_recordTimeStamp = "recordTimeStamp";
    public static final String columnName_recordTime = "recordTime";
    public static final String columnName_recordAddress = "recordAddress";
    public static final String columnName_recordOrientation = "recordOrientation";
    public static final String columnName_recordError = "recordError";
    public static final String columnName_recordOtherError = "recordOtherError";

    private static final String SQLite_RECORD_CREATE =
            "CREATE TABLE " + recordTableName + "(" + columnName_recordID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + columnName_recordTimeStamp + " TEXT NOT NULL,"
                    + columnName_recordTime + " TEXT,"
                    + columnName_recordAddress + " TEXT NOT NULL,"
                    + columnName_recordOrientation + " TEXT NOT NULL,"
                    + columnName_recordError + " TEXT,"
                    + columnName_recordOtherError + " TEXT);";

    private static final String SQLite_RECORD_DELETE = "DROP TABLE IF EXISTS " + recordTableName;


    public AccelerometerDBHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }


    // note: becase MyDBHelper extends SQLiteOpenHelper, we need to implement onCreate
    //       and onUpgrade, else Android Studio will complain of error.

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLite_RECORD_CREATE);
        db.execSQL(SQLite_CREATE);
    }

    // onUpgrade is called if the database version is increased in your application code
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQLite_RECORD_DELETE);
        db.execSQL(SQLite_DELETE);
        onCreate(db);
    }


}
