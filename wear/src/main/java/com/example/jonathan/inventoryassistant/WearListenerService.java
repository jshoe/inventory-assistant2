package com.example.jonathan.inventoryassistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

// Service to run on the Wear to listen for messages coming from the Mobile.

public class WearListenerService extends WearableListenerService {
    private static final String RECEIVER_SERVICE_PATH = "/receiver-service";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("Wear", "Wear received a message.");
        byte[] msg = messageEvent.getData();
    }
}
