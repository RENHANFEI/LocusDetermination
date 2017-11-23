package com.example.renhanfei.mylocationmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.camera2.params.StreamConfigurationMap;

/**
 * Created by renhanfei on 17/3/9.
 */

public class LocationRecordsDBHelper extends SQLiteOpenHelper {
    public static final int databaseVersion = 1;
    public static final String databaseName = "locationRecordsDB";

    public static final String tableName = "locationRecordsTable";
    public static final String columnName_itemID = "_id";
    public static final String columnName_itemTime = "recordTime";
    public static final String columnName_itemPosition = "recordPosition";
    public static final String columnName_itemLatitude = "recordLatitude";
    public static final String columnName_itemLongitude = "recordLongitude";
    public static final String columnName_itemAddress = "recordAddress";

    private static final String SQLite_CREATE =
            "CREATE TABLE " + tableName + "(" + columnName_itemID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + columnName_itemTime + " TEXT NOT NULL,"
                    + columnName_itemPosition + " TEXT NOT NULL,"
                    + columnName_itemLatitude + " TEXT NOT NULL,"
                    + columnName_itemLongitude + " TEXT NOT NULL,"
                    + columnName_itemAddress + " TEXT NOT NULL);";

    private static final String SQLite_DELETE = "DROP TABLE IF EXISTS " + tableName;


    public LocationRecordsDBHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }


    // note: becase MyDBHelper extends SQLiteOpenHelper, we need to implement onCreate
    //       and onUpgrade, else Android Studio will complain of error.

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLite_CREATE);
    }

    // onUpgrade is called if the database version is increased in your application code
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQLite_DELETE);
        onCreate(db);
    }


}
