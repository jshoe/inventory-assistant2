package com.example.jonathan.inventoryassistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class DataLayerListenerService extends WearableListenerService {

    private static final String MAKE_GROUP_KEY = "com.example.jonathan.inventoryassistant.make_group_key";
    private static final String DELETE_ALL_GROUPS_KEY = "com.example.jonathan.inventoryassistant.delete_all_groups_key";
    private static final String GROUP_NAME = "groupName";

    @Override
    public void onDataChanged (DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            DataItem item = event.getDataItem();
            if (item.getUri().getPath().compareTo(MAKE_GROUP_KEY) == 0) {
                Log.d("DATA RECEIVED", "MAKE GROUP KEY. GROUP NAME = " + DataMapItem.fromDataItem(item).getDataMap().getString(GROUP_NAME));
            }
            else if (item.getUri().getPath().compareTo(DELETE_ALL_GROUPS_KEY) == 0) {

            }
        }
    }

}
