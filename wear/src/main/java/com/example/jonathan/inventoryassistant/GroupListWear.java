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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.util.List;
import java.util.Locale;

public class GroupListWear extends Activity {

    private static final String PATH = "/database-action";
    private static final String ACTION_KEY = "action-key";
    private static final String DELETE_GROUP_KEY = "delete-group-key";
    private static final String GROUP_NAME_KEY = "group-name";
    private static final String MAKE_GROUP_KEY = "make-group-key";

    private static final String UPDATE_GROUP_LIST = "com.example.jonathan.inventoryassistant.update-group-list";

    private final int REQ_CODE_SPEECH_INPUT = 0;

    GroupReaderDbHelper groupReaderDbHelper;
    ArrayList<String> groupArray;

    ReceiveMessages myReceiver = null;
    Boolean myReceiverIsRegistered = false;

    ListView groupList;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Intent intent = new Intent(this, WearListenerService.class);
        startService(intent);

        myReceiver = new ReceiveMessages();

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

        makeGroupList();

        View footerView =  ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.group_list_footer, null, false);
        groupList.addFooterView(footerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_list, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!myReceiverIsRegistered) {
            registerReceiver(myReceiver, new IntentFilter(UPDATE_GROUP_LIST));
            myReceiverIsRegistered = true;
        }
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

    public void makeNewGroupSpeech(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_group_prompt));
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
            String spokenText = results.get(0);
            groupReaderDbHelper.insertGroup(spokenText);
            sendMakeGroupToMobile(spokenText);
            makeGroupList();
            // Do something with spokenText
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendMakeGroupToMobile(String groupName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, MAKE_GROUP_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    private void makeGroupList() {
        Cursor cursor = groupReaderDbHelper.getAllGroups();
        cursor.moveToPosition(-1);

        groupList = (ListView) findViewById(R.id.groupList);
        groupArray = new ArrayList<>();

        while (cursor.moveToNext()) {
            String groupName = cursor.getString(cursor.getColumnIndexOrThrow(GroupReaderContract.GroupEntry.GROUP_NAME));
            groupArray.add(groupName);
            Log.d("GrpLst", "Trying to print out all the items in the GroupList");
        }
        cursor.close();
        if (groupArray.size() == 0) {
            groupArray.add("       (no groups)");
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupArray);
            groupList.setAdapter(arrayAdapter);
            groupList.setOnItemClickListener(null);
        } else {
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupArray);
            groupList.setAdapter(arrayAdapter);

            groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                // argument position gives the index of item which is clicked
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    //Toast.makeText(getApplicationContext(), "Long press to delete", Toast.LENGTH_SHORT).show();
                    String selectedGroup = groupArray.get(position);
                    showItemList(selectedGroup);
                    //showScanStartMessage();
                    //Toast.makeText(getApplicationContext(), "Group Selected : " + selectedGroup, Toast.LENGTH_SHORT).show();
                }
            });

            groupList.setLongClickable(true);
            groupList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                    deleteEntryDialog(groupArray.get(pos));
                    return true;
                    }
            });
        }
    }

    public void showScanStartMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Scan items in any order. Nearby tags have been auto-detected!\n");
        builder.setCancelable(true);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(20);
    }

    public void deleteEntryDialog(final String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg = "Delete the " + groupName + " group?";
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        groupReaderDbHelper.deleteGroup(groupName);
                        sendDeleteGroupToMobile(groupName);
                        makeGroupList();
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

    private void sendDeleteGroupToMobile (String groupName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, DELETE_GROUP_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    public void showItemList(String groupName) {
        Intent i = new Intent();
        i.putExtra("groupName", groupName);
        i.setClass(this, ItemListWear.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public class ReceiveMessages extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UPDATE_GROUP_LIST)) {
                makeGroupList();
            }
        }
    }
}
