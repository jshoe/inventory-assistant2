package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MakeItem extends Activity {

    private static final String PATH = "/database-action";
    private static final String ACTION_KEY = "action-key";
    private static final String MAKE_ITEM_KEY = "make-item-key";
    private static final String DELETE_ALL_ITEMS_IN_GROUP_KEY = "delete-all-items-in-group-key";
    private static final String GROUP_NAME_KEY = "group-name";
    private static final String ITEM_NAME_KEY = "item-name";

    GoogleApiClient mGoogleApiClient;
    ItemReaderDbHelper itemReaderDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_item);

        setTitle("New Item");
        itemReaderDbHelper = new ItemReaderDbHelper(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        // Now you can use the Data Layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_make_item, menu);
        return true;
    }

    public void makeNewItem(View view) {
        String itemName = ((EditText) findViewById(R.id.itemName)).getText().toString();
        String groupName = getIntent().getStringExtra("groupName");
        Log.d("CLICK", "MAKE NEW ITEM");
        if (itemName.compareTo("") != 0) {
            itemReaderDbHelper.insertItem(groupName, itemName);

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
            putDataMapReq.getDataMap().putString(ACTION_KEY, MAKE_ITEM_KEY);
            putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
            putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

            Intent i = new Intent(this, NfcWrite.class);
            i.putExtra("groupName", groupName);
            String textToWrite = groupName + " --- " + itemName;
            i.putExtra("textToWrite", textToWrite);
            startActivity(i);
        }
    }

    public void deleteAllItemsInGroup(View view) {
        Log.d("ClICK", "DELETE ALL ITEMS IN TEST GROUP");
        itemReaderDbHelper.deleteItemsInGroup("TestGroup");

        String groupName = getIntent().getStringExtra("groupName");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, DELETE_ALL_ITEMS_IN_GROUP_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /** if (id == R.id.action_settings) {
            return true;
        } */

        return super.onOptionsItemSelected(item);
    }
}
