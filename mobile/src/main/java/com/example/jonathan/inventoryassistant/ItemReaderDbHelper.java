package com.example.jonathan.inventoryassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.jonathan.inventoryassistant.ItemReaderContract.ItemEntry;

/**
 * Created by randyramadhana on 7/31/15.
 */
public class ItemReaderDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ItemReader.db";

    public ItemReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ItemEntry.SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(ItemEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertItem(String groupName, String itemName) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ItemEntry.GROUP_NAME, groupName);
        values.put(ItemEntry.ITEM_NAME, itemName);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                ItemEntry.TABLE_NAME,
                null,
                values);
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("select * from table", null);
    }

    public void deleteItem(String groupName, String itemName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = ItemEntry.GROUP_NAME + " = '" + groupName + "' " +
                ItemEntry.ITEM_NAME + " = '" + itemName + "'";

        String[] selectionArgs = {ItemEntry.GROUP_NAME};
        db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void deleteItemsInGroup(String groupName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = ItemEntry.GROUP_NAME + " = '" + groupName;
        String[] selectionArgs = {ItemEntry.GROUP_NAME};
        db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
    }
}
