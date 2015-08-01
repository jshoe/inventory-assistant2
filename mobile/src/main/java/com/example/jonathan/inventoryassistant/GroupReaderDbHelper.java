package com.example.jonathan.inventoryassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.jonathan.inventoryassistant.GroupReaderContract.GroupEntry;

/**
 * Created by randyramadhana on 7/31/15.
 */
public class GroupReaderDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GroupReader.db";

    public GroupReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GroupEntry.SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(GroupEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertGroup(String groupName) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(GroupEntry.GROUP_NAME, groupName);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                GroupEntry.TABLE_NAME,
                null,
                values);
    }

    public Cursor getAllGroups() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("select * from table", null);

    }

    public void deleteGroup(String groupName, String itemName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = GroupEntry.GROUP_NAME + " = '" + groupName + "'";

        String[] selectionArgs = {GroupEntry.GROUP_NAME};
        db.delete(GroupEntry.TABLE_NAME, selection, selectionArgs);
    }
}