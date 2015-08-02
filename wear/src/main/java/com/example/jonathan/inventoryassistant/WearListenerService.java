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
    private static final String MAKE_GROUP_KEY = "com.example.jonathan.inventoryassistant.make_group_key";
    private static final String DELETE_ALL_GROUPS_KEY = "com.example.jonathan.inventoryassistant.delete_all_groups_key";
    private static final String GROUP_NAME = "group-name";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("Wear", "Wear received a message.");
        byte[] msg = messageEvent.getData();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("DATA", "DATA CHANGED");
        for (DataEvent event : dataEvents) {
            DataItem item = event.getDataItem();
            Log.d("DATA", "EVENT IS THERE");
            if (item.getUri().getPath().compareTo(PATH) == 0) {
                Log.d("DATA", "PATH IS CORRECT");
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                if (dataMap.getString(ACTION_KEY).compareTo(MAKE_GROUP_KEY) == 0) {
                    Log.d("DATA", "MAKE_GROUP_KEY RECEIVED");
                }
            }
        }
    }
}
