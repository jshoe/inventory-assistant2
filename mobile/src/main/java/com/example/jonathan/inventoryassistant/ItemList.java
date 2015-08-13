package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class ItemList extends Activity {

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

    ItemReaderDbHelper itemReaderDbHelper;
    String groupName = "";

    ListView itemList;

    ReceiveMessages myReceiver = null;
    Boolean myReceiverIsRegistered = false;

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

        myReceiver = new ReceiveMessages();

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



        makeItemList();

        View footerView =  ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_list_footer, null, false);
        itemList.addFooterView(footerView);
    }


    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        if (!myReceiverIsRegistered) {
            registerReceiver(myReceiver, new IntentFilter(UPDATE_ITEM_LIST));
            myReceiverIsRegistered = true;
        }
        itemReaderDbHelper = new ItemReaderDbHelper(this);
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

    public void makeGroupTag(View view) {
        Intent i = new Intent();
        i.setClass(this, WriteNfcTag.class);
        i.putExtra("groupName", groupName);
        i.putExtra("textToWrite", groupName);
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
        Intent intent;
        switch (id) {
            case android.R.id.home:
                intent = new Intent(this, GroupList.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_help:
                intent = new Intent(this, HelpScreen.class);
                this.startActivity(intent);
                break;
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.itemList) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Item: " + itemArray.get(info.position));
            String[] menuItems = {"Rename", "Delete", "Copy to another group", "Add/rewrite NFC tag"};
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = {"Rename", "Delete", "Copy to another group", "Add/rewrite NFC tag"};
        String optionSelected = menuItems[menuItemIndex];
        String entrySelected = itemArray.get(info.position);
        switch (optionSelected) {
            case "Rename":
                renameItemDialog(entrySelected);
                break;
            case "Delete":
                itemReaderDbHelper.deleteItem(groupName, entrySelected);
                makeItemList();
                break;
            case "Copy to another group":
                copyItemDialog(entrySelected);
                break;
            case "Add/rewrite NFC tag":
                rewriteNfcTag(entrySelected);
                break;
            default:
                return true;
        }
        return true;
    }

    public void rewriteNfcTag(String itemName) {
        Intent i = new Intent(this, WriteNfcTag.class);
        i.putExtra("itemName", itemName);
        i.putExtra("groupName", groupName);
        String textToWrite = groupName + " --- " + itemName;
        i.putExtra("textToWrite", textToWrite);
        startActivity(i);
    }

    public void copyItemDialog(final String itemName) {
        GroupReaderDbHelper groupReaderDbHelper = new GroupReaderDbHelper(this);
        Cursor cursor = groupReaderDbHelper.getAllGroups();
        cursor.moveToPosition(-1);
        List<String> groupArray = new ArrayList<String>();
        while (cursor.moveToNext()) {
            String groupName = cursor.getString(cursor.getColumnIndexOrThrow(GroupReaderContract.GroupEntry.GROUP_NAME));
            groupArray.add(groupName);
        }
        cursor.close();
        final String groups[] = groupArray.toArray(new String[groupArray.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick target group:");
        builder.setItems(groups, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String targetGroup = groups[which];
                itemReaderDbHelper.copyItem(groupName, targetGroup, itemName);
                sendCopyItemToWear(groupName, targetGroup, itemName);
            }
        });
        builder.show();
        makeItemList();
        showCustomToast("Copied item successfully!");
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

    private void sendCopyItemToWear(String oldGroupName, String newGroupName, String itemName) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PATH);
        putDataMapReq.getDataMap().putString(ACTION_KEY, COPY_KEY);
        putDataMapReq.getDataMap().putString(GROUP_NAME_KEY, oldGroupName);
        putDataMapReq.getDataMap().putString(ITEM_NAME_KEY, itemName);
        putDataMapReq.getDataMap().putString(NEW_GROUP_NAME_KEY, newGroupName);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    public boolean renameItemDialog(final String oldName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ItemList.this);
        alert.setTitle("Enter a new name:");

        final EditText input = new EditText(ItemList.this);
        input.setText(oldName);
        alert.setView(input);

        alert.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = input.getEditableText().toString();
                itemReaderDbHelper.renameItem(groupName, oldName, newName);
                sendRenameItemToWear(groupName, oldName, newName);
                makeItemList();
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
        return false;
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

    private void makeItemList() {
        groupName = getIntent().getStringExtra("groupName");
        setTitle("Group: " + groupName);
        Cursor cursor = itemReaderDbHelper.getAllItemsInGroup(groupName);
        cursor.moveToPosition(-1);
        itemList = (ListView) findViewById(R.id.itemList);
        itemArray = new ArrayList<>();

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.ITEM_NAME));
            itemArray.add(itemName);
        }
        cursor.close();
        if (itemArray.size() == 0) {
            //itemArray.add("(no items)");
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
                    showCustomToast("Long press entries for options");
                    //Toast.makeText(getApplicationContext(), "Item Selected : " + selectedItem, Toast.LENGTH_SHORT).show();
                    showItemView(selectedItem);
                }
            });

            itemList.setLongClickable(true);
//            itemList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
//                    deleteEntryDialog(itemArray.get(pos));
//                    return true;
//                }
//            });

            registerForContextMenu(itemList);
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
            }
        }
    }
}
