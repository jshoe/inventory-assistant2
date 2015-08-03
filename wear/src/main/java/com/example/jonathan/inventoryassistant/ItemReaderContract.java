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
        public static final String CHECKED = "checked";
        public static final String DATE_CHECKED = "date_checked";

        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String DATE_TYPE = " DATETIME";
        private static final String COMMA_SEP = ",";
        protected static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                        ItemEntry._ID + " INTEGER PRIMARY KEY," +
                        ItemEntry.GROUP_NAME + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.ITEM_NAME + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.CHECKED + INT_TYPE + COMMA_SEP +
                        ItemEntry.DATE_CHECKED + DATE_TYPE +
                " )";

        protected static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;
    }
}
