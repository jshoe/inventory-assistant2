package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
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
import java.util.Date;
import java.util.List;

public class ItemListWear extends Activity {

    private static final String UPDATE_ITEM_LIST = "com.example.joanathan.inventoryassistant.update-item-list";
    private static final String UPDATE_KEY = "update-key";
    private static final String CHECK_ITEM = "check-item";
    private static final String UNCHECK_ITEM = "uncheck-item";
    private static final String UPDATE_LIST = "update-list";
    private static final String ITEM_NAME_KEY = "item-name";
    private static final String MAKE_ITEM_KEY = "make-item-key";

    private static final String PATH = "/database-action-mobile";
    private static final String CHECK_KEY = "check-key";
    private static final String ACTION_KEY = "action-key";
    private static final String DELETE_ITEM_KEY = "delete-item-key";
    private static final String GROUP_NAME_KEY = "group-name";
    private static final String DATE_KEY = "date-key";

    private static final String DONE_KEY = "done-key";

    private static final int REQ_CODE_SPEECH_INPUT = 0;

    String groupName = "";
    ItemReaderDbHelper itemReaderDbHelper;
    ArrayList<String> itemArray;
    ListView itemList;

    ReceiveMessages myReceiver = null;
    Boolean myReceiverIsRegistered = false;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        groupName = getIntent().getStringExtra("groupName");
        setTitle(groupName + ":");
        itemReaderDbHelper = new ItemReaderDbHelper(this);

        myReceiver = new ReceiveMessages();

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
        makeItemList();
        View footerView =  ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_list_footer, null, false);
        itemList.addFooterView(footerView);
    }

    public void makeNewItemSpeech(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_item_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String str = results.get(0);
            String spokenText = str.substring(0, 1).toUpperCase() + str.substring(1);
            itemReaderDbHelper.insertItem(groupName, spokenText);
            sendMakeItemToMobile(groupName, spokenText);
            makeItemList();
            // Do something with spokenText
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendMakeItemToMobile(String groupName, String itemName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, MAKE_ITEM_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    public void onBackPressed() {
        Intent i = new Intent();
        i.setClass(this, GroupListWear.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!myReceiverIsRegistered) {
            registerReceiver(myReceiver, new IntentFilter(UPDATE_ITEM_LIST));
            myReceiverIsRegistered = true;
        }
        Log.d("onResume", "ITEM LIST IS RESUMING");
        makeItemList();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myReceiverIsRegistered) {
            unregisterReceiver(myReceiver);
            myReceiverIsRegistered = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_list_wear, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getArrayPositionFromTitle(String title){
        for (int i = 0; i < itemArray.size(); i++) {
            if (itemArray.get(i).equals(title)) {
                return i;
            }
        }
        return -1;
    }

    private void checkOffItem(String tag) {
        Log.d("checkOffItem", "Going to try to check off " + tag);
        int p = getArrayPositionFromTitle(tag);
        if (p != -1) {
            itemList.setItemChecked(p, true);
        }
    }

    private void uncheckOffItem(String tag) {
        int p = getArrayPositionFromTitle(tag);
        if (p != -1) {
            itemList.setItemChecked(p, false);
        }
    }

    public void finishScan(View view) {
        SparseBooleanArray items = itemList.getCheckedItemPositions();
        ArrayList unchecked = new ArrayList();
        ArrayList checkedOff = new ArrayList();
        for (int i = 0; i < itemArray.size(); i++) {
            if (!items.get(i)) {
                unchecked.add(itemArray.get(i));
            } else {
                checkedOff.add(itemArray.get(i));
                sendScanInfoToMobile(groupName, itemList.getItemAtPosition(i).toString());
            }
        }
        showFinishDialog(unchecked, checkedOff);
    }

    public void backToGroupList() {
        Intent i = new Intent();
        i.putExtra("groupName", groupName);
        i.setClass(this, GroupListWear.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void showFinishDialog(ArrayList unchecked, final ArrayList checkedOff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg;
        if (unchecked.size() == 0) {
            msg = "All items checked off!";
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sendDoneToMobile();
                            backToGroupList();
                        }
                    });
        } else {
            msg = "Missing items!\n\n";
            for (Object str : unchecked) {
                msg += " * " + str.toString() + "\n";
            }
            builder.setPositiveButton("Ignore",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //checkOffItemsInDb(checkedOff);
                            sendDoneToMobile();
                            finish();
                        }
                    });
            builder.setNegativeButton("Back to Scan",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        }
        builder.setMessage(msg);
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(18);
    }

    public void deleteEntryDialog(final String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg = "Delete item " + itemName + "?";
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        itemReaderDbHelper.deleteItem(groupName, itemName);
                        sendDeleteItemToMobile(groupName, itemName);
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

    private void sendDeleteItemToMobile(String groupName, String itemName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, DELETE_ITEM_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    private void makeItemList() {
        Log.d("ScanInItems", "Trying makeItemList");
        setTitle("Group: " + groupName);
        Cursor cursor = itemReaderDbHelper.getAllItemsInGroup(groupName);
        cursor.moveToPosition(-1);
        itemList = (ListView) findViewById(R.id.itemList);
        itemList.setChoiceMode(itemList.CHOICE_MODE_MULTIPLE);
        itemArray = new ArrayList<>();

        ArrayList checked = new ArrayList();

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.ITEM_NAME));
            String groupName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.GROUP_NAME));
            itemArray.add(itemName);
            //int status = cursor.getInt(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.CHECKED));
            //checked.add(status);
        }
        cursor.close();
        if (itemArray.size() == 0) {
            itemArray.add("          (no items)");
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, itemArray);
            itemList.setAdapter(arrayAdapter);
            itemList.setOnItemClickListener(null);
        } else {
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this,android.R.layout.simple_list_item_multiple_choice, itemArray);
            itemList.setAdapter(arrayAdapter);

            // register onClickListener to handle click events on each item
            itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                // argument position gives the index of item which is clicked
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    //Toast.makeText(getApplicationContext(), "Long press to delete", Toast.LENGTH_SHORT).show();
                    String itemName = itemArray.get(position);
                    CheckedTextView item = (CheckedTextView) v;
                    if (item.isChecked()) {
                        sendCheckToMobile(groupName, itemName);
                    } else {
                        sendUncheckToMobile(groupName, itemName);
                    }
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

    private void sendCheckToMobile(String groupName, String itemName) {
        Log.d("sendCheckToWear", "Trying to check off: " + groupName + ", " + itemName);

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, CHECK_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        putDataMapReq.getDataMap().putString(CHECK_KEY, "1");
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    private void sendUncheckToMobile(String groupName, String itemName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, CHECK_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        putDataMapReq.getDataMap().putString(CHECK_KEY, "0");
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    private void sendScanInfoToMobile(String groupName, String itemName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, DATE_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        putDataMapReq.getDataMap().putString(DATE_KEY, (new Date()).toString());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    private void sendDoneToMobile() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, DONE_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    public class ReceiveMessages extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String updateKey = intent.getStringExtra(UPDATE_KEY);

            switch (updateKey) {
                case UPDATE_LIST:
                    Log.d("RECEIVE BROADCAST", "UPDATE_LIST RECEIVED");
                    makeItemList();
                    break;
                case CHECK_ITEM:
                    Log.d("RECEIVE BROADCAST", "CHECK_ITEM RECEIVED");
                    checkOffItem(intent.getStringExtra(ITEM_NAME_KEY));
                    break;
                case UNCHECK_ITEM:
                    Log.d("RECEIVE BROADCAST", "UNCHECK_ITEM RECEIVED");
                    uncheckOffItem(intent.getStringExtra(ITEM_NAME_KEY));
                    break;
                case DONE_KEY:
                    Log.d("RECEIVE BROADCAST", "DONE_KEY RECEIVED");
                    String receivedGroupName = intent.getStringExtra(GROUP_NAME_KEY);
                    if (groupName.equals(receivedGroupName)) {
                        finishScan((View) findViewById(R.id.done));
                    }
                    break;
            }
        }
    }
}
