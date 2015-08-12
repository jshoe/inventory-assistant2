package com.example.jonathan.inventoryassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.jonathan.inventoryassistant.ItemReaderContract.ItemEntry;

/**
 * Created by randyramadhana on 7/31/15.
 */
public class ItemReaderDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 6;
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
        values.put(ItemEntry.NFC_TAG, "NULL");
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
        values.put(ItemEntry.LAT1, "NULL");
        values.put(ItemEntry.LON1, "NULL");
        values.put(ItemEntry.LAT2, "NULL");
        values.put(ItemEntry.LON2, "NULL");
        values.put(ItemEntry.LAT3, "NULL");
        values.put(ItemEntry.LON3, "NULL");
        values.put(ItemEntry.LAT4, "NULL");
        values.put(ItemEntry.LON4, "NULL");
        values.put(ItemEntry.LAT5, "NULL");
        values.put(ItemEntry.LON5, "NULL");
        values.put(ItemEntry.LAT6, "NULL");
        values.put(ItemEntry.LON6, "NULL");
        values.put(ItemEntry.LAT7, "NULL");
        values.put(ItemEntry.LON7, "NULL");
        values.put(ItemEntry.LAT8, "NULL");
        values.put(ItemEntry.LON8, "NULL");
        values.put(ItemEntry.LAT9, "NULL");
        values.put(ItemEntry.LON9, "NULL");
        values.put(ItemEntry.LAT10, "NULL");
        values.put(ItemEntry.LON10, "NULL");

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

        return db.rawQuery("select * from " + ItemEntry.TABLE_NAME +
                " where " + ItemEntry.GROUP_NAME + " = ?", new String[] {groupName}
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

    public void updateNfcTag(String groupName, String itemName, String tagData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update " + ItemEntry.TABLE_NAME + " set " + ItemEntry.NFC_TAG + " = '" + tagData + "'" +
                   " where " + ItemEntry.GROUP_NAME + " = '" + groupName + "' and " + ItemEntry.ITEM_NAME + " = '" + itemName + "'");
    }

    public void copyItem(String oldGroupName, String newGroupName, String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("insert into " + ItemEntry.TABLE_NAME + " (" + ItemEntry.GROUP_NAME + ", " + ItemEntry.ITEM_NAME + ", " + ItemEntry.NFC_TAG + ", " +
                        ItemEntry.DATE_CHECKED1 + ", " + ItemEntry.DATE_CHECKED2 + ", " + ItemEntry.DATE_CHECKED3 + ", " + ItemEntry.DATE_CHECKED4 + ", " +
                        ItemEntry.DATE_CHECKED5 + ", " + ItemEntry.DATE_CHECKED6 + ", " + ItemEntry.DATE_CHECKED7 + ", " + ItemEntry.DATE_CHECKED8 + ", " +
                        ItemEntry.DATE_CHECKED9 + ", " + ItemEntry.DATE_CHECKED10 + ") " +
                "select " + "'" + newGroupName + "'" + ", " + ItemEntry.ITEM_NAME + ", " + ItemEntry.NFC_TAG + ", " +
                        ItemEntry.DATE_CHECKED1 + ", " + ItemEntry.DATE_CHECKED2 + ", " + ItemEntry.DATE_CHECKED3 + ", " + ItemEntry.DATE_CHECKED4 + ", " +
                        ItemEntry.DATE_CHECKED5 + ", " + ItemEntry.DATE_CHECKED6 + ", " + ItemEntry.DATE_CHECKED7 + ", " + ItemEntry.DATE_CHECKED8 + ", " +
                        ItemEntry.DATE_CHECKED9 + ", " + ItemEntry.DATE_CHECKED10 +
                " from " + ItemEntry.TABLE_NAME +
                " where " + ItemEntry.GROUP_NAME + "='" + oldGroupName + "' and " + ItemEntry.ITEM_NAME + "='" + itemName + "'"
        );
    }

    public void renameItem(String groupName, String oldItemName, String newItemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update " + ItemEntry.TABLE_NAME +
                        " set " + ItemEntry.ITEM_NAME + "='" + newItemName + "'" +
                        " where " + ItemEntry.GROUP_NAME + "='" + groupName + "'" +
                        " and " + ItemEntry.ITEM_NAME + "='" + oldItemName + "'"
        );
    }

    public void renameGroup(String oldGroupName, String newGroupName) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("update " + ItemEntry.TABLE_NAME +
                        " set " + ItemEntry.GROUP_NAME + "='" + newGroupName + "'" +
                        " where " + ItemEntry.GROUP_NAME + "='" + oldGroupName + "'"
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

    public void insertLatestLatitude(String groupName, String itemName, float lat) {
        SQLiteDatabase db = this.getWritableDatabase();

        updateLatitudeHistory(groupName, itemName);

        db.execSQL("update " + ItemEntry.TABLE_NAME +
                        " set " + ItemEntry.LAT1 + "=" + lat +
                        " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                        " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
        );
    }

    public void insertLatestLongitude(String groupName, String itemName, float lon) {
        SQLiteDatabase db = this.getWritableDatabase();

        updateLongitudeHistory(groupName, itemName);

        db.execSQL("update " + ItemEntry.TABLE_NAME +
                        " set " + ItemEntry.LON1 + "=" + lon +
                        " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                        " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
        );
    }

    public void updateDateCheckedItem(String groupName, String itemName, Date utilDateChecked) {
        SQLiteDatabase db = this.getWritableDatabase();

        DateFormat df = new SimpleDateFormat("EEEE, MMMM, d, yyyy");
        Date now = Calendar.getInstance().getTime();
        String date = df.format(now);
        date = String.format("%-33s", date);

        df = new SimpleDateFormat("h:mm a");
        String time = df.format(now);
        time = String.format("%14s", time);

        String text = date + time;

        updateDateHistory(groupName, itemName);

        Log.d("updateDateCheckedItem", "Date to write: " + text);

        db.execSQL("update " + ItemEntry.TABLE_NAME +
                        " set " + ItemEntry.DATE_CHECKED1 + "='" + text + "'" +
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
                            " set " + ItemEntry.DATE_CHECKED10 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED8));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED9 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED7));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED8 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED6));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED7 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED5));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED6 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED4));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED5 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED3));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED4 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED2));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED3 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }

        date = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.DATE_CHECKED1));

        if (!date.equals("NULL")) {
            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + ItemEntry.DATE_CHECKED2 + "='" + date + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }
    }

    private void updateLatitudeHistory(String groupName, String itemName) {
        final String LAT_COL = "latitude";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = getItem(groupName, itemName);
        cursor.moveToFirst();
        float lat;

        for (int i = 9; i > 0; i--) {
            lat = cursor.getFloat(cursor.getColumnIndexOrThrow(LAT_COL + i));

            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + LAT_COL + (i+1) + "='" + lat + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }
    }

    private void updateLongitudeHistory(String groupName, String itemName) {
        final String LON_COL = "longitude";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = getItem(groupName, itemName);
        cursor.moveToFirst();
        float lon;

        for (int i = 9; i > 0; i--) {
            lon = cursor.getFloat(cursor.getColumnIndexOrThrow(LON_COL + i));

            db.execSQL("update " + ItemEntry.TABLE_NAME +
                            " set " + LON_COL + (i+1) + "='" + lon + "'" +
                            " where " + ItemEntry.GROUP_NAME + " ='" + groupName + "'" +
                            " and " + ItemEntry.ITEM_NAME + " ='" + itemName + "'"
            );
        }
    }
}
