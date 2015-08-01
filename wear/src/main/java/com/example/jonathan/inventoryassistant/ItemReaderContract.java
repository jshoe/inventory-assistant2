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
        public static final String TABLE_NAME = "items";
        public static final String GROUP_NAME = "group";
        public static final String ITEM_NAME = "item";

        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        protected static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                        ItemEntry._ID + " INTEGER PRIMARY KEY," +
                        ItemEntry.GROUP_NAME + TEXT_TYPE + COMMA_SEP +
                        ItemEntry.ITEM_NAME + TEXT_TYPE + COMMA_SEP +
                " )";

        protected static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;
    }
}