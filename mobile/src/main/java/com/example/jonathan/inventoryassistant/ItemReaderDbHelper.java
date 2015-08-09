package com.example.jonathan.inventoryassistant;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

import static com.example.jonathan.inventoryassistant.ItemReaderContract.ItemEntry;

/**
 * Created by randyramadhana on 7/31/15.
 */
public class ItemReaderDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
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
        values.put(ItemEntry.CHECKED, 0);
        values.put(ItemEntry.DATE_CHECKED1, "NULL");
        values.put(ItemEntry.DATE_CHECKED2, "NULL");
        values.put(ItemEntry.DATE_CHECKED3, "NULL");
        values.put(ItemEntry.DATE_CHECKED4, "NULL");
        values.put(ItemEntry.DATE_CHECKED5, "NULL");
        values.put(ItemEntry.DATE_CHECKED6, "NULL");
        values.put(ItemEntry.DATE_CHECKED7, "NULL");
        values.put(ItemEntry.DATE_CHECKED8, "NULL");
        values.put(ItemEntry.DATE_CHECKED9, "NULL");
        values.put(ItemEntry.DATE_CHECKED10, "NULL");

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                ItemEntry.TABLE_NAME,
                null,
                values);
    }

    public Cursor getItem(String groupName, String itemName) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("select * from " + ItemEntry.TABLE_NAME +
                " where " + ItemEntry.GROUP_NAME + " = ? and " + ItemEntry.ITEM_NAME + " = ?",
                new String[] {groupName, itemName});
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("select * from " + ItemEntry.TABLE_NAME, null);
    }

    public Cursor getAllItemsInGroup(String groupName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                ItemEntry._ID,
                ItemEntry.GROUP_NAME,
                ItemEntry.ITEM_NAME,
                ItemEntry.NFC_TAG,
                ItemEntry.CHECKED,
                ItemEntry.DATE_CHECKED1,
                ItemEntry.DATE_CHECKED2,
                ItemEntry.DATE_CHECKED3,
                ItemEntry.DATE_CHECKED4,
                ItemEntry.DATE_CHECKED5,
                ItemEntry.DATE_CHECKED6,
                ItemEntry.DATE_CHECKED7,
                ItemEntry.DATE_CHECKED8,
                ItemEntry.DATE_CHECKED9,
                ItemEntry.DATE_CHECKED10
        };

        String sortOrder = ItemEntry.ITEM_NAME;

        String selection = ItemEntry.GROUP_NAME + " = ?";
        String[] selectionArgs = {groupName};

        return db.query(
                ItemEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

    }

    public void deleteItem(String groupName, String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from " + ItemEntry.TABLE_NAME +
                        " where " + ItemEntry.GROUP_NAME + "='" + groupName + "'" +
                        " and " +ItemEntry.ITEM_NAME + "='" + itemName + "'"
        );
    }

    public void deleteItemsInGroup(String groupName) {
        Log.d("ItemReaderDbHelper", "Trying to deleteItemsInGroup");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + ItemEntry.TABLE_NAME +
                        " where " + ItemEntry.GROUP_NAME + "='" + groupName + "'"
        );
    }

    public void deleteAllItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + ItemEntry.TABLE_NAME);
    }
    
    public void copyItem(String oldGroupName, String newGroupName, String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("insert into " + ItemEntry.TABLE_NAME + " (" + ItemEntry.GROUP_NAME + ", " + ItemEntry.ITEM_NAME + ", " + ItemEntry.NFC_TAG + ", " +
                        ItemEntry.DATE_CHECKED1 + ", " + ItemEntry.DATE_CHECKED2 + ", " + ItemEntry.DATE_CHECKED3 + ", " + ItemEntry.DATE_CHECKED4 + ", " +
                        ItemEntry.DATE_CHECKED5 + ", " + ItemEntry.DATE_CHECKED6 + ItemEntry.DATE_CHECKED7 + ", " + ItemEntry.DATE_CHECKED8 +
                        ItemEntry.DATE_CHECKED9 + ", " + ItemEntry.DATE_CHECKED10 + ") " +
                "select " + newGroupName + ", " + ItemEntry.ITEM_NAME + ", " + ItemEntry.NFC_TAG + ", " +
                        ItemEntry.DATE_CHECKED1 + ", " + ItemEntry.DATE_CHECKED2 + ", " + ItemEntry.DATE_CHECKED3 + ", " + ItemEntry.DATE_CHECKED4 + ", " +
                        ItemEntry.DATE_CHECKED5 + ", " + ItemEntry.DATE_CHECKED6 + ItemEntry.DATE_CHECKED7 + ", " + ItemEntry.DATE_CHECKED8 +
                        ItemEntry.DATE_CHECKED9 + ", " + ItemEntry.DATE_CHECKED10 +
                " from " + ItemEntry.TABLE_NAME +
                " where " + ItemEntry.GROUP_NAME + " = " + oldGroupName + " and " + ItemEntry.ITEM_NAME + " = " + itemName
        );
    }

    public void checkItem(String groupName, String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update " + ItemEntry.TABLE_NAME +
                " set " + ItemEntry.CHECKED + "=" + 1 +
                " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
        );
    }

    public void uncheckItem(String groupName, String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update " + ItemEntry.TABLE_NAME +
                        " set " + ItemEntry.CHECKED + "=" + 0 +
                        " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                        " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
        );
    }

    public void updateDateCheckedItem(String groupName, String itemName, Date utilDateChecked) {
        SQLiteDatabase db = this.getWritableDatabase();
        java.sql.Date date = new java.sql.Date(utilDateChecked.getTime());

        updateDateHistory(groupName, itemName);

        db.execSQL("update " + ItemEntry.TABLE_NAME +
                        " set " + ItemEntry.DATE_CHECKED1 + "=" + date +
                        " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                        " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
        );
    }

    private void updateDateHistory (String groupName, String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = getItem(groupName, itemName);
        cursor.moveToFirst();
        String date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED9));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED10 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED8));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED9 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED7));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED8 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED6));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED7 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED5));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED6 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED4));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED5 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED3));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED4 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED2));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED3 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED1));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED2 + "=" + date +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }
    }
}
