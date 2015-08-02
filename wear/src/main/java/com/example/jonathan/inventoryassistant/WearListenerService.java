package com.example.jonathan.inventoryassistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

// Service to run on the Wear to listen for messages coming from the Mobile.

public class WearListenerService extends WearableListenerService {

    private static final String PATH = "/database-action";
    private static final String ACTION_KEY = "action-key";
    private static final String MAKE_GROUP_KEY = "make-group-key";
    private static final String DELETE_GROUP_KEY = "delete-group-key";
    private static final String DELETE_ALL_GROUPS_KEY = "delete-all-groups-key";
    private static final String MAKE_ITEM_KEY = "make-item-key";
    private static final String DELETE_ITEM_KEY = "delete-item-key";
    private static final String DELETE_ALL_ITEMS_IN_GROUP_KEY = "delete-all-items-in-group-key";
    private static final String ITEM_NAME_KEY = "item-name";
    private static final String GROUP_NAME_KEY = "group-name";

    ItemReaderDbHelper itemReaderDbHelper;
    GroupReaderDbHelper groupReaderDbHelper;

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
        for (DataEvent event : dataEvents) {
            DataItem item = event.getDataItem();
            if (item.getUri().getPath().compareTo(PATH) == 0) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                String key = dataMap.getString(ACTION_KEY);
                String groupName, itemName;
                switch (key) {
                    case MAKE_GROUP_KEY:
                        Log.d("DATA", "MAKE_GROUP_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        groupReaderDbHelper.insertGroup(groupName);
                        break;
                    case DELETE_GROUP_KEY:
                        Log.d("DATA", "DELETE_GROUP_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        groupReaderDbHelper.deleteGroup(groupName);
                        itemReaderDbHelper.deleteItemsInGroup(groupName);
                        break;
                    case DELETE_ALL_GROUPS_KEY:
                        Log.d("DATA", "DELETE_ALL_GROUPS_KEY RECEIVED");
                        groupReaderDbHelper.deleteAllGroups();
                        itemReaderDbHelper.deleteAllItems();
                        break;
                    case MAKE_ITEM_KEY:
                        Log.d("DATA", "MAKE_ITEM_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemName = dataMap.getString(ITEM_NAME_KEY);
                        itemReaderDbHelper.insertItem(groupName, itemName);
                        break;
                    case DELETE_ITEM_KEY:
                        Log.d("DATA", "DELETE_ITEM_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemName = dataMap.getString(ITEM_NAME_KEY);
                        itemReaderDbHelper.deleteItem(groupName, itemName);
                        break;
                    case DELETE_ALL_ITEMS_IN_GROUP_KEY:
                        Log.d("DATA", "DELETE_ALL_ITEMS_IN_GROUP_KEY RECEIVED");
                        groupName = dataMap.getString(GROUP_NAME_KEY);
                        itemReaderDbHelper.deleteItemsInGroup(groupName);
                        break;
                }
            }
        }
    }
}
