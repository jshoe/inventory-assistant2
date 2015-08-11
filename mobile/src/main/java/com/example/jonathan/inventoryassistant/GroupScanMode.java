package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/** Source attribution: Some NFC reading code from this tutorial:
 *  http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
 */

public class GroupScanMode extends Activity {

    private static final String UPDATE_ITEM_LIST = "com.example.joanathan.inventoryassistant.update-item-list";
    private static final String UPDATE_KEY = "update-key";
    private static final String CHECK_ITEM = "check-item";
    private static final String UNCHECK_ITEM = "uncheck-item";
    private static final String UPDATE_LIST = "update-list";

    private static final String PATH = "/database-action";
    private static final String ACTION_KEY = "action-key";
    private static final String CHECK_KEY = "check-key";
    private static final String DATE_KEY = "date-key";
    private static final String GROUP_NAME_KEY = "group-name";
    private static final String ITEM_NAME_KEY = "item-name";

    ItemReaderDbHelper itemReaderDbHelper;
    String groupName = "";
    ArrayList<String> itemArray;
    ListView itemList;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;

    Location lastKnownLocation;

    ReceiveMessages myReceiver = null;
    Boolean myReceiverIsRegistered = false;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_scan_mode);

        groupName = getIntent().getStringExtra("groupName");
        itemReaderDbHelper = new ItemReaderDbHelper(this);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setLogo(R.drawable.action_bar_logo);
        getActionBar().setDisplayUseLogoEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.backArrow), PorterDuff.Mode.SRC_ATOP);
        getActionBar().setHomeAsUpIndicator(upArrow);

        myReceiver = new ReceiveMessages();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (location == null) {
                            Log.d("CURRENT LOCATION:", " IS NULL.");
                        }
                        else {
                            handleNewLocation(location);
                        };
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
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        initiateNfcComponents();
        makeItemList();
        showScanStartMessage();
    }

    private void handleNewLocation(Location location) {
        Log.d("CURRENT LOCATION IS: ", location.toString());
        lastKnownLocation = location;
    }

    public void initiateNfcComponents() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        } else {
            if (!mNfcAdapter.isEnabled()) {
                //mTextView.setText("NFC is disabled.");
            } else {
                //mTextView.setText(R.string.explanation);
            }
        }
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!myReceiverIsRegistered) {
            registerReceiver(myReceiver, new IntentFilter(UPDATE_ITEM_LIST));
            myReceiverIsRegistered = true;
        }
        if (mNfcAdapter != null) {
            setupForegroundDispatch(this, mNfcAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.setClass(this, ItemList.class);
        i.putExtra("groupName", groupName);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    protected void onPause() {
        if (myReceiverIsRegistered) {
            unregisterReceiver(myReceiver);
            myReceiverIsRegistered = false;
        }
        if (mNfcAdapter != null) {
            stopForegroundDispatch(this, mNfcAdapter);
        }
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) { // For NFC
        String action = intent.getAction();
        if (mNfcAdapter == null) {
            return;
        }
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();
            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public void showScanStartMessage() {
        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.nfc_pic);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Prepare to scan items in any order. Nearby tags will be auto-detected!\n");
        builder.setCancelable(true);
        builder.setPositiveButton("Start",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Log.d("showScanStartMessage", "User clicked OK");
                    }
                });
        builder.setView(image);
        AlertDialog alert = builder.create();
        alert.show();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(20);
    }

    public void cancelScan(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to end the scan process?");
        builder.setCancelable(true);
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder.setNegativeButton("Cancel",
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

    private void makeItemList() {
        setTitle("Group: " + groupName);
        Cursor cursor = itemReaderDbHelper.getAllItemsInGroup(groupName);
        cursor.moveToPosition(-1);
        itemList = (ListView) findViewById(R.id.itemList);
        itemList.setChoiceMode(itemList.CHOICE_MODE_MULTIPLE);
        itemArray = new ArrayList<>();

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.ITEM_NAME));
            itemArray.add(itemName);
            Log.d("ItemList", "Trying to print out all the items in the ItemList");
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
                    new ArrayAdapter<>(this,android.R.layout.simple_list_item_multiple_choice, itemArray);
            itemList.setAdapter(arrayAdapter);
            // register onClickListener to handle click events on each item
            itemList.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                // argument position gives the index of item which is clicked
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    String itemName = itemArray.get(position);
                    CheckedTextView item = (CheckedTextView) v;
                    if (item.isChecked()) {
                        sendCheckToWear(groupName, itemName);
                    } else {
                        sendUncheckToWear(groupName, itemName);
                    }
                }
            });
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
            }
        }
        showFinishDialog(unchecked, checkedOff);
    }

    public void showFinishDialog(ArrayList unchecked, final ArrayList checkedOff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg;
        if (unchecked.size() == 0) {
            msg = "All items checked off!";
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            checkOffItemsInDb(checkedOff);
                            finish();
                        }
                    });
        } else {
            msg = "Are you sure you want to exit scan mode? You're missing some things!\n\n";
            for (Object str : unchecked) {
                msg += "  * " + str.toString() + "\n";
            }
            builder.setPositiveButton("Ignore Missing",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            checkOffItemsInDb(checkedOff);
                            finish();
                        }
                    });
            builder.setNegativeButton("Keep Scanning",
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
        textView.setTextSize(20);
    }

    public void checkOffItemsInDb(ArrayList checkedOff) {
        for (Object i: checkedOff) {
            Log.d("checkOffGroupItemsInDb", "Trying to check off: " + i.toString());
            String itemName = i.toString();
            itemReaderDbHelper.checkItem(groupName, itemName);
            itemReaderDbHelper.updateDateCheckedItem(groupName, itemName, new Date());
            if (lastKnownLocation != null) {
                itemReaderDbHelper.insertLatestLatitude(groupName, itemName, (float) lastKnownLocation.getLatitude());
                itemReaderDbHelper.insertLatestLongitude(groupName, itemName, (float) lastKnownLocation.getLongitude());
            } else {
                itemReaderDbHelper.insertLatestLatitude(groupName, itemName, 0f);
                itemReaderDbHelper.insertLatestLongitude(groupName, itemName, 0f);
            }

            Log.d("checkOffGroupItemsInDb", "Current date is: " + (new Date()).toString());
        }
        Cursor cursor = itemReaderDbHelper.getAllItemsInGroup(groupName);
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.ITEM_NAME));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.CHECKED));
            Log.d("checkOffGroupItemsInDb", "Status of " + itemName + " is " + status);
        }
        cursor.close();
    }

    public void checkOffItemNfc(String NfcTag) {
        String itemName = "";
        if (NfcTag.contains(" --- ")) {
            /*String[] parts = NfcTag.split(" --- ");
            NfcTag = parts[1];*/

            itemName = compareTagToEachItemInGroup(NfcTag);
        }
        if (!itemName.equals("")) {
            Toast.makeText(getApplicationContext(), "Detected " + itemName + "!", Toast.LENGTH_SHORT).show();
            int p = getArrayPositionFromTitle(itemName);
            if (p != -1) {
                itemList.setItemChecked(p, true);
            }
            sendCheckToWear(groupName, NfcTag);
        }
        else {
            Toast.makeText(getApplicationContext(), "Item " + itemName + "is not in this group!", Toast.LENGTH_SHORT).show();
        }
    }

    private String compareTagToEachItemInGroup(String nfcTag) {
        Cursor cursor = itemReaderDbHelper.getAllItemsInGroup(groupName);
        cursor.moveToPosition(-1);
        String itemTag, itemName;
        while (cursor.moveToNext()) {
            itemTag = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.NFC_TAG));
            if (nfcTag.equals(itemTag)) {
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.ITEM_NAME));
                cursor.close();
                return itemName;
            }
        }

        cursor.close();
        return "";
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }
            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0063;
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                checkOffItemNfc(result);
            }
        }
    }

    private void sendCheckToWear(String groupName, String itemName) {
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

    private void sendUncheckToWear(String groupName, String itemName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, CHECK_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        putDataMapReq.getDataMap().putString(CHECK_KEY, "0");
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    private void sendUpdateDateToWear(String groupName, String itemName, Date date) {

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, DATE_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, groupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        putDataMapReq.getDataMap().putString(DATE_KEY, date.toString());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
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
            }
        }
    }
}
