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

        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String DATE_TYPE = " DATETIME";
        private static final String COMMA_SEP = ",";
        protected static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                        ItemEntry._ID + " INTEGER PRIMARY KEY," +
                        ItemEntry.GROUP_NAME + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.ITEM_NAME + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.NFC_TAG + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.CHECKED + INT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED1 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED2 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED3 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED4 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED5 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED6 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED7 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED8 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED9 + DATE_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED10 + DATE_TYPE +
                " )";

        protected static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;
    }
}
