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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MakeNewGrp extends Activity {

    private static final String PATH = "/database-action";
    private static final String ACTION_KEY = "action-key";
    private static final String MAKE_GROUP_KEY = "com.example.jonathan.inventoryassistant.make_group_key";
    private static final String DELETE_ALL_GROUPS_KEY = "com.example.jonathan.inventoryassistant.delete_all_groups_key";
    private static final String GROUP_NAME = "group-name";

    GroupReaderDbHelper groupReaderDbHelper;
    GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_new_grp);
        setTitle("New Group");
        groupReaderDbHelper = new GroupReaderDbHelper(this);
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
        getMenuInflater().inflate(R.menu.menu_make_new_grp, menu);
        return true;
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

    public void makeNewGroup(View view) {
        String groupName = ((EditText) findViewById(R.id.groupName)).getText().toString();
        if (groupName.compareTo("") != 0) {
            groupReaderDbHelper.insertGroup(groupName);
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
            putDataMapReq.getDataMap().putString(ACTION_KEY, MAKE_GROUP_KEY);
            putDataMapReq.getDataMap().putString(GROUP_NAME, groupName);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(final DataApi.DataItemResult result) {
                    if(result.getStatus().isSuccess()) {
                        Log.d("DATA", "Data item set: " + result.getDataItem().getUri());
                    }
                }
            });
            Intent intent = new Intent(this, GrpList.class);
            startActivity(intent);
        }
    }

    public void deleteAllGroups(View view) {
        groupReaderDbHelper.deleteAllGroups();
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(DELETE_ALL_GROUPS_KEY);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }
}