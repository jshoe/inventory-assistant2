package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class ItemInfo extends Activity {
    private static final String UPDATE_ITEM_LIST = "com.example.joanathan.inventoryassistant.update-item-list";
    private static final String UPDATE_KEY = "update-key";
    private static final String CHECK_ITEM = "check-item";
    private static final String UNCHECK_ITEM = "uncheck-item";
    private static final String UPDATE_LIST = "update-list";

    private static final String PATH = "/database-action-wear";
    private static final String ACTION_KEY = "action-key";
    private static final String DELETE_ITEM_KEY = "delete-item-key";
    private static final String ITEM_NAME_KEY = "item-name";
    private static final String GROUP_NAME_KEY = "group-name";

    private static final String COPY_KEY = "copy-key";
    private static final String NEW_GROUP_NAME_KEY = "new-group-name";
    private static final String NEW_ITEM_NAME_KEY = "new-item-name";
    private static final String RENAME_ITEM_KEY = "rename-item-key";

    GoogleApiClient mGoogleApiClient;

    String groupName = "";
    String itemName = "";
    ItemReaderDbHelper itemReaderDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_item_info);
        groupName = getIntent().getStringExtra("groupName");
        itemName = getIntent().getStringExtra("itemName");
        Log.d("GROUP NAME", groupName);
        Log.d("ITEM NAME", itemName);
        formatActionBar();
        showScanHistory();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.setClass(this, ItemList.class);
        i.putExtra("groupName", groupName);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void formatActionBar() {
        setTitle("Item: " + itemName);
        getActionBar().setDisplayShowHomeEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setLogo(R.drawable.action_bar_logo);
        getActionBar().setDisplayUseLogoEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.backArrow), PorterDuff.Mode.SRC_ATOP);
        getActionBar().setHomeAsUpIndicator(upArrow);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_view, menu);
        return true;
    }

    public void showCustomToast(String text) {
        final Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        t.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                t.cancel();
            }
        }, 1000);
    }

    public void showOnMap(Integer position) {
        String latFront = "latitude";
        String lonFront = "longitude";
        position++;
        latFront += position.toString();
        lonFront += position.toString();

        Cursor cursor = itemReaderDbHelper.getItem(groupName, itemName);
        cursor.moveToFirst();
        Float latitude;
        Float longitude;

        Double lat;
        Double lon;

        Log.d("showOnMap", "The thing we are trying to get is: " + latFront);

        try {
            latitude = cursor.getFloat(cursor.getColumnIndex(latFront));
        } catch (android.database.CursorIndexOutOfBoundsException e) {
            latitude = 0f;
        }
        try {
            longitude = cursor.getFloat(cursor.getColumnIndex(lonFront));
        } catch (android.database.CursorIndexOutOfBoundsException e) {
            longitude = 0f;
        }
        Log.d("showOnMap", "Value of lat from database manually is: " + latitude.toString());
        Log.d("showOnMap", "Value of lon from database manually is: " + longitude.toString());

        if (latitude == 0f && longitude == 0f) {
            showCustomToast("Entry has no location logged");
        } else {
            lat = Double.parseDouble(latitude.toString());
            lon = Double.parseDouble(longitude.toString());

            Intent i = new Intent();
            i.setClass(this, ScanLogMapView.class);
            Bundle b = new Bundle();
            b.putDouble("latitude", lat);
            b.putDouble("longitude", lon);
            b.putString("title", "Check-In");
            b.putString("groupName", groupName);
            b.putString("itemName", itemName);
            String snippet = lat.toString() + ", " + lon.toString();
            b.putString("snippet", snippet);
            i.putExtras(b);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    public void deleteItem(View view) {
        itemReaderDbHelper.deleteItem(groupName, itemName);
        onBackPressed();
    }

    public void renameItemDialog(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ItemInfo.this);
        alert.setTitle("Enter a new name:");

        final EditText input = new EditText(ItemInfo.this);
        final String oldName = itemName;
        input.setText(oldName);
        alert.setView(input);

        alert.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = input.getEditableText().toString();
                itemReaderDbHelper.renameItem(groupName, oldName, newName);
                sendRenameItemToWear(groupName, oldName, newName);
                itemName = newName;
                setTitle("Item: " + itemName);
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    private void sendRenameItemToWear(String groupName, String oldItemName, String newItemName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, RENAME_ITEM_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, oldItemName);
        putDataMapReq.getDataMap().putString(NEW_ITEM_NAME_KEY, newItemName);
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

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_help:
                Intent intent = new Intent(this, HelpScreen.class);
                this.startActivity(intent);
                break;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ArrayList logArray;

    private void showScanHistory() {
        ListView logList = (ListView) findViewById(R.id.scanLog);

        Cursor cursor = itemReaderDbHelper.getItem(groupName, itemName);
        cursor.moveToFirst();
        logArray = new ArrayList<>();
        ArrayList tempArray = new ArrayList();

        String cur;
        for (Integer n = 1; n <= 10; n++) {
            cur = "date_checked" + n.toString();
            tempArray.add(cursor.getString(cursor.getColumnIndex(cur)));
        }

        cursor.close();

        for (Object o: tempArray) {
            if (!o.equals("NULL")) {
                logArray.add(o);
            }
        }

        if (logArray.size() == 0) {
            logArray.add("(no scans logged)");
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, logArray);
            logList.setAdapter(arrayAdapter);
            logList.setOnItemClickListener(null);
        } else {
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, logArray);
            logList.setAdapter(arrayAdapter);

            // register onClickListener to handle click events on each item
            logList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                // argument position gives the index of item which is clicked
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    String selectedItem = (String) logArray.get(position);
                    showOnMap(position);
                    //Toast.makeText(getApplicationContext(), "Long press for options", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), "Item Selected : " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            });

            logList.setLongClickable(true);
            logList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                    //deleteEntryDialog(groupArray.get(pos));
                    return true;
                }
            });
        }
    }
}
