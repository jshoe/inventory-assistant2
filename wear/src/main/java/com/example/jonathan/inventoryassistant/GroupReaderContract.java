package com.example.jonathan.inventoryassistant;

import android.provider.BaseColumns;

/**
 * Created by randyramadhana on 7/31/15.
 */
public class GroupReaderContract {
    public GroupReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class GroupEntry implements BaseColumns {
        public static final String TABLE_NAME = "Groups";
        public static final String GROUP_NAME = "groupName";

        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        protected static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + GroupEntry.TABLE_NAME + " (" +
                        GroupEntry._ID + " INTEGER PRIMARY KEY, " +
                        GroupEntry.GROUP_NAME + TEXT_TYPE +
                        " )";

        protected static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + GroupEntry.TABLE_NAME;
    }
}
