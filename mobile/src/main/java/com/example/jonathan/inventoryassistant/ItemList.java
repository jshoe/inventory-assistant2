package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class ItemList extends Activity {

    private static final String PATH = "/database-action";
    private static final String ACTION_KEY = "action-key";
    private static final String DELETE_ITEM_KEY = "delete-item-key";
    private static final String ITEM_NAME_KEY = "item-name";
    private static final String GROUP_NAME_KEY = "group-name";

    ItemReaderDbHelper itemReaderDbHelper;
    String groupName = "";

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setLogo(R.drawable.action_bar_logo);
        getActionBar().setDisplayUseLogoEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.backArrow), PorterDuff.Mode.SRC_ATOP);
        getActionBar().setHomeAsUpIndicator(upArrow);

        itemReaderDbHelper = new ItemReaderDbHelper(this);
        makeItemList();

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
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        itemReaderDbHelper = new ItemReaderDbHelper(this);
        makeItemList();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.setClass(this, GroupList.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
        return true;
    }

    public void makeNewItem(View view) {
        Intent i = new Intent();
        i.setClass(this, NewItem.class);
        i.putExtra("groupName", groupName);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void startScan(View view) {
        Intent i = new Intent();
        i.setClass(this, GroupScanMode.class);
        i.putExtra("groupName", groupName);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Intent intent = new Intent(this, GroupList.class);
                startActivity(intent);
                finish();
                return true;
        }

        //noinspection SimplifiableIfStatement
        /** if (id == R.id.action_settings) {
            return true;
        } */

        return super.onOptionsItemSelected(item);
    }

    public void deleteEntryDialog(final String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg = "Delete item " + itemName + "?";
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        itemReaderDbHelper.deleteItem(groupName, itemName);
                        sendDeleteItemToWear(groupName, itemName);
                        makeItemList();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.setMessage(msg);
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(20);
    }

    private void sendDeleteItemToWear (String groupName, String itemName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, DELETE_ITEM_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    ArrayList<String> itemArray;

    public void showItemView(String itemName) {
        Intent i = new Intent();
        i.setClass(this, ItemInfo.class);
        i.putExtra("groupName", groupName);
        i.putExtra("itemName", itemName);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void makeItemList() {
        groupName = getIntent().getStringExtra("groupName");
        setTitle("Group: " + groupName);
        Cursor cursor = itemReaderDbHelper.getAllItemsInGroup(groupName);
        cursor.moveToPosition(-1);
        ListView itemList = (ListView) findViewById(R.id.itemList);
        itemArray = new ArrayList<>();

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.ITEM_NAME));
            itemArray.add(itemName);
        }
        cursor.close();
        if (itemArray.size() == 0) {
            itemArray.add("(no items)");
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, itemArray);
            itemList.setAdapter(arrayAdapter);
            itemList.setOnItemClickListener(null);
        } else {
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, itemArray);
            itemList.setAdapter(arrayAdapter);

            // register onClickListener to handle click events on each item
            itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                // argument position gives the index of item which is clicked
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    String selectedItem = itemArray.get(position);
                    Toast.makeText(getApplicationContext(), "Long press for options", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), "Item Selected : " + selectedItem, Toast.LENGTH_SHORT).show();
                    showItemView(selectedItem);
                }
            });

            itemList.setLongClickable(true);
            itemList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                    deleteEntryDialog(itemArray.get(pos));
                    return true;
                }
            });
        }
    }
}
