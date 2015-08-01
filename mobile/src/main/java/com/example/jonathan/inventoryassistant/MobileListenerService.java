package com.example.jonathan.inventoryassistant;

import android.content.Intent;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

// Service to run on the Mobile to listen for messages coming from the Wear.

public class MobileListenerService extends WearableListenerService {
    private static final String RECEIVER_SERVICE_PATH = "/receiver-service";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("Mobile", "Mobile received a message.");
        byte[] msg = messageEvent.getData();
    }
}
