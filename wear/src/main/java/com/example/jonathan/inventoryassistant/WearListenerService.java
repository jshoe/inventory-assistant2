package com.example.jonathan.inventoryassistant;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.text.SimpleDateFormat;
import java.util.Date;

// Service to run on the Wear to listen for messages coming from the Mobile.

public class WearListenerService extends WearableListenerService {

    private static final String PATH = "/database-action-wear";
    private static final String ACTION_KEY = "action-key";
    private static final String MAKE_GROUP_KEY = "make-group-key";
    private static final String DELETE_GROUP_KEY = "delete-group-key";
    private static final String DELETE_ALL_GROUPS_KEY = "delete-all-groups-key";
    private static final String MAKE_ITEM_KEY = "make-item-key";
    private static final String DELETE_ITEM_KEY = "delete-item-key";
    private static final String DELETE_ALL_ITEMS_IN_GROUP_KEY = "delete-all-items-in-group-key";
    private static final String ITEM_NAME_KEY = "item-name";
    private static final String GROUP_NAME_KEY = "group-name";
    private static final String CHECK_KEY = "check-key";
    private static final String DATE_KEY = "date-key";

    private static final String UPDATE_KEY = "update-key";
    private static final String CHECK_ITEM = "check-item";
    private static final String UNCHECK_ITEM = "uncheck-item";
    private static final String UPDATE_LIST = "update-list";
    private static final String DONE_KEY = "done-key";
    private static final String COPY_KEY = "copy-key";
    private static final String NEW_GROUP_NAME_KEY = "new-group-name";
    private static final String NEW_ITEM_NAME_KEY = "new-item-name";
    private static final String RENAME_ITEM_KEY = "rename-item-key";
    private static final String RENAME_GROUP_KEY = "rename-group-key";

    private static final String UPDATE_GROUP_LIST = "com.example.jonathan.inventoryassistant.update-group-list";
    private static final String UPDATE_ITEM_LIST = "com.example.joanathan.inventoryassistant.update-item-list";

    ItemReaderDbHelper itemReaderDbHelper;
    GroupReaderDbHelper groupReaderDbHelper;
    String groupName;
    String itemName;

    @Override
    public void onCreate() {
        super.onCreate();

        itemReaderDbHelper = new ItemReaderDbHelper(this);
        groupReaderDbHelper = new GroupReaderDbHelper(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("Wear", "Wear received a message.");
        byte[] msg = messageEvent.getData();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("onDataChanged", "Received a data change");
        for (DataEvent event : dataEvents) {
            DataItem item = event.getDataItem();
            if (item.getUri().getPath().compareTo(PATH) == 0) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                String key = dataMap.getString(ACTION_KEY);
                String checkString, dateString, newGroupName, newItemName;
                switch (key) {
                    case MAKE_GROUP_KEY:
                        Log.d("DATA", "MAKE_GROUP_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        groupReaderDbHelper.insertGroup(groupName);
                        updateGroupList();
                        break;
                    case DELETE_GROUP_KEY:
                        Log.d("DATA", "DELETE_GROUP_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        groupReaderDbHelper.deleteGroup(groupName);
                        itemReaderDbHelper.deleteItemsInGroup(groupName);
                        updateGroupList();
                        break;
                    case DELETE_ALL_GROUPS_KEY:
                        Log.d("DATA", "DELETE_ALL_GROUPS_KEY RECEIVED");
                        groupReaderDbHelper.deleteAllGroups();
                        itemReaderDbHelper.deleteAllItems();
                        updateGroupList();
                        break;
                    case MAKE_ITEM_KEY:
                        Log.d("DATA", "MAKE_ITEM_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemName = dataMap.getString(ITEM_NAME_KEY);
                        itemReaderDbHelper.insertItem(groupName, itemName);
                        updateItemList();
                        break;
                    case DELETE_ITEM_KEY:
                        Log.d("DATA", "DELETE_ITEM_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemName = dataMap.getString(ITEM_NAME_KEY);
                        itemReaderDbHelper.deleteItem(groupName, itemName);
                        updateItemList();
                        break;
                    case DELETE_ALL_ITEMS_IN_GROUP_KEY:
                        Log.d("DATA", "DELETE_ALL_ITEMS_IN_GROUP_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemReaderDbHelper.deleteItemsInGroup(groupName);
                        updateItemList();
                        break;
                    case CHECK_KEY:
                        Log.d("DATA", "CHECK_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemName = dataMap.getString(ITEM_NAME_KEY);
                        checkString = dataMap.getString(CHECK_KEY);
                        switch (checkString) {
                            case "0":
                                itemReaderDbHelper.uncheckItem(groupName, itemName);
                                uncheckItem(itemName);
                                break;
                            case "1":
                                // TODOs : change to using broadcast receiver instead
                                itemReaderDbHelper.checkItem(groupName, itemName);
                                checkItem(itemName);
                                break;
                        }
                        break;
                    case DATE_KEY:
                        Log.d("DATA", "DATE_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemName = dataMap.getString(ITEM_NAME_KEY);
                        dateString = dataMap.getString(DATE_KEY);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
                        try {
                            Date date = sdf.parse(dateString);
                            itemReaderDbHelper.updateDateCheckedItem(groupName, itemName, date);
                            updateItemList();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        break;
                    case COPY_KEY:
                        Log.d("DATA", "COPY_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemName = dataMap.getString(ITEM_NAME_KEY);
                        newGroupName = dataMap.getString(NEW_GROUP_NAME_KEY);
                        itemReaderDbHelper.copyItem(groupName, newGroupName, itemName);
                        updateItemList();
                        break;
                    case RENAME_ITEM_KEY:
                        Log.d("DATA", "RENAME_ITEM_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemName = dataMap.getString(ITEM_NAME_KEY);
                        newItemName = dataMap.getString(NEW_ITEM_NAME_KEY);
                        itemReaderDbHelper.renameItem(groupName, itemName, newItemName);
                        updateItemList();
                        break;
                    case RENAME_GROUP_KEY:
                        Log.d("DATA", "RENAME_GROUP_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        newGroupName = dataMap.getString(NEW_GROUP_NAME_KEY);
                        groupReaderDbHelper.renameGroup(groupName, newGroupName);
                        itemReaderDbHelper.renameGroup(groupName, newGroupName);
                        updateGroupList();
                        break;
                    case DONE_KEY:
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        doneScanning(groupName);
                        break;
                }
            }
        }
    }

    private void updateGroupList() {
        Intent i = new Intent(UPDATE_GROUP_LIST);
        sendBroadcast(i);
    }

    private void updateItemList() {
        Intent i = new Intent(UPDATE_ITEM_LIST);
        i.putExtra(UPDATE_KEY, UPDATE_LIST);
        sendBroadcast(i);
    }

    private void checkItem(String itemName) {
        Intent i = new Intent(UPDATE_ITEM_LIST);
        i.putExtra(UPDATE_KEY, CHECK_ITEM);
        i.putExtra(ITEM_NAME_KEY, itemName);
        sendBroadcast(i);
    }

    private void uncheckItem(String itemName) {
        Intent i = new Intent(UPDATE_ITEM_LIST);
        i.putExtra(UPDATE_KEY, UNCHECK_ITEM);
        i.putExtra(ITEM_NAME_KEY, itemName);
        sendBroadcast(i);
    }

    private void doneScanning(String groupName) {
        Intent i = new Intent(UPDATE_ITEM_LIST);
        i.putExtra(UPDATE_KEY, DONE_KEY);
        i.putExtra(GROUP_NAME_KEY, groupName);
        sendBroadcast(i);
    }
}
