package com.example.jonathan.inventoryassistant;

import android.provider.BaseColumns;

/**
 * Created by randyramadhana on 7/31/15.
 */
public class ItemReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ItemReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "Items";
        public static final String GROUP_NAME = "groupName";
        public static final String ITEM_NAME = "itemName";
        public static final String NFC_TAG = "nfcTag";
        public static final String CHECKED = "checked";
        public static final String DATE_CHECKED1 = "date_checked1";
        public static final String DATE_CHECKED2 = "date_checked2";
        public static final String DATE_CHECKED3 = "date_checked3";
        public static final String DATE_CHECKED4 = "date_checked4";
        public static final String DATE_CHECKED5 = "date_checked5";
        public static final String DATE_CHECKED6 = "date_checked6";
        public static final String DATE_CHECKED7 = "date_checked7";
        public static final String DATE_CHECKED8 = "date_checked8";
        public static final String DATE_CHECKED9 = "date_checked9";
        public static final String DATE_CHECKED10 = "date_checked10";
        public static final String LAT1 = "latitude1";
        public static final String LON1 = "longitude1";
        public static final String LAT2 = "latitude2";
        public static final String LON2 = "longitude2";
        public static final String LAT3 = "latitude3";
        public static final String LON3 = "longitude3";
        public static final String LAT4 = "latitude4";
        public static final String LON4 = "longitude4";
        public static final String LAT5 = "latitude5";
        public static final String LON5 = "longitude5";
        public static final String LAT6 = "latitude6";
        public static final String LON6 = "longitude6";
        public static final String LAT7 = "latitude7";
        public static final String LON7 = "longitude7";
        public static final String LAT8 = "latitude8";
        public static final String LON8 = "longitude8";
        public static final String LAT9 = "latitude9";
        public static final String LON9 = "longitude9";
        public static final String LAT10 = "latitude10";
        public static final String LON10 = "longitude10";

        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String REAL_TYPE = " REAL";
        private static final String COMMA_SEP = ",";
        protected static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                        ItemEntry._ID + " INTEGER PRIMARY KEY," +
                        ItemEntry.GROUP_NAME + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.ITEM_NAME + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.NFC_TAG + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.CHECKED + INT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED1 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED2 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED3 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED4 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED5 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED6 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED7 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED8 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED9 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED10 + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.LAT1 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON1 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT2 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON2 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT3 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON3 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT4 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON4 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT5 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON5 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT6 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON6 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT7 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON7 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT8 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON8 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT9 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON9 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LAT10 + REAL_TYPE + COMMA_SEP +
                        ItemEntry.LON10 + REAL_TYPE + COMMA_SEP +
                " )";

        protected static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;
    }
}
