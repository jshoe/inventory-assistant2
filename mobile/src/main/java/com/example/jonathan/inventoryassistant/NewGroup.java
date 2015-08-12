package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class NewGroup extends Activity {

    private static final String PATH = "/database-action-wear";
    private static final String ACTION_KEY = "action-key";
    private static final String MAKE_GROUP_KEY = "make-group-key";
    private static final String DELETE_ALL_GROUPS_KEY = "delete-all-groups-key";
    private static final String GROUP_NAME_KEY = "group-name";

    GroupReaderDbHelper groupReaderDbHelper;
    ItemReaderDbHelper itemReaderDbHelper;
    GoogleApiClient mGoogleApiClient;
    String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setLogo(R.drawable.action_bar_logo);
        getActionBar().setDisplayUseLogoEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.backArrow), PorterDuff.Mode.SRC_ATOP);
        getActionBar().setHomeAsUpIndicator(upArrow);

        setTitle("New Group");
        Toast.makeText(getApplicationContext(), "Enter a name for your group!", Toast.LENGTH_LONG).show();

        groupReaderDbHelper = new GroupReaderDbHelper(this);
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
    public void onBackPressed() {
        Intent i = new Intent();
        i.setClass(this, GroupList.class);
        i.putExtra("groupName", groupName);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_group, menu);
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
        groupName = ((EditText) findViewById(R.id.groupName)).getText().toString();
        if (groupName.compareTo("") != 0) {
            groupReaderDbHelper.insertGroup(groupName);
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
            putDataMapReq.getDataMap().putString(ACTION_KEY, MAKE_GROUP_KEY);
            putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
            startAddingItems();
            //finishCreation();
        }
    }

    public void startAddingItems() {
        Intent i = new Intent();
        i.setClass(this, ItemList.class);
        i.putExtra("groupName", groupName);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void finishCreation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Now it's time to add items to track!");
        builder.setCancelable(true);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        startAddingItems();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(22);
    }

    public void deleteAllGroups(View view) {
        groupReaderDbHelper.deleteAllGroups();
        itemReaderDbHelper.deleteAllItems();
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, DELETE_ALL_GROUPS_KEY);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }


}