package com.example.renhanfei.phonelocusdetermination;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by renhanfei on 17/4/2.
 */

public class LocusDBHelper extends SQLiteOpenHelper {
    public static final int databaseVersion = 3;
    public static final String databaseName = "locusDB";

    // it is for item
    public static final String tableName = "itemTable";
    public static final String columnName_itemID = "_id";
    public static final String columnName_itemTimeStamp = "itemTimeStamp";
    public static final String columnName_itemTime = "itemTime";
    public static final String columnName_itemLat = "itemLat";
    public static final String columnName_itemLng = "itemLng";
    public static final String columnName_itemX = "itemAX"; // acceleration
    public static final String columnName_itemY = "itemAY"; // acceleration
    public static final String columnName_itemPositionX = "itemX";
    public static final String columnName_itemPositionY = "itemY";
    public static final String columnName_itemFilteredX = "itemFilteredX";
    public static final String columnName_itemFilteredY = "itemFilteredY";
    public static final String columnName_itemRecordID = "recordID";
    public static final String columnName_itemVX = "itemVX";
    public static final String columnName_itemVY = "itemVY";
    private static final String SQLite_CREATE =
            "CREATE TABLE " + tableName + "(" + columnName_itemID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + columnName_itemTimeStamp + " TEXT NOT NULL,"
                    + columnName_itemTime + " TEXT NOT NULL,"
                    + columnName_itemLat + " TEXT NOT NULL,"
                    + columnName_itemLng + " TEXT NOT NULL,"
                    + columnName_itemX + " TEXT NOT NULL,"
                    + columnName_itemY + " TEXT NOT NULL,"
                    + columnName_itemPositionX + " TEXT NOT NULL,"
                    + columnName_itemPositionY + " TEXT NOT NULL,"
                    + columnName_itemFilteredX + " TEXT NOT NULL,"
                    + columnName_itemFilteredY + " TEXT NOT NULL,"
                    + columnName_itemRecordID + " INTEGER NOT NULL,"
                    + columnName_itemVX + " TEXT NOT NULL,"
                    + columnName_itemVY + " TEXT NOT NULL);";

    private static final String SQLite_DELETE = "DROP TABLE IF EXISTS " + tableName;


    // it is for records (every records have some items)
    public static final String recordTableName = "recordTable";
    public static final String columnName_recordID = "_id";
    public static final String columnName_recordTimeStamp = "recordTimeStamp";
    public static final String columnName_recordTime = "recordTime";
    public static final String columnName_recordAddress = "recordAddress";

    private static final String SQLite_RECORD_CREATE =
            "CREATE TABLE " + recordTableName + "(" + columnName_recordID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + columnName_recordTimeStamp + " TEXT NOT NULL,"
                    + columnName_recordTime + " TEXT,"
                    + columnName_recordAddress + " TEXT NOT NULL);";

    private static final String SQLite_RECORD_DELETE = "DROP TABLE IF EXISTS " + recordTableName;


    public LocusDBHelper(Context context) {
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
