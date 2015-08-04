package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ScanInItems extends Activity {

    String groupName = "";
    ItemReaderDbHelper itemReaderDbHelper;
    ArrayList<String> itemArray;
    ListView itemList;
    String toCheckOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_in_items);
        groupName = getIntent().getStringExtra("groupName");
        toCheckOff = getIntent().getStringExtra("itemName");
        itemReaderDbHelper = new ItemReaderDbHelper(this);
        makeItemList();
        checkOffItem(toCheckOff);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scan_in_items, menu);
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

    public void checkOffItem(String tag) {
        Log.d("checkOffItem", "Going to try to check off " + tag);
        int p = getArrayPositionFromTitle(tag);
        if (p != -1) {
            itemList.setItemChecked(p, true);
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
                            finish();
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
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(15);
        alert.show();
    }

    public void deleteEntryDialog(final String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg = "Delete item " + itemName + "?";
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        itemReaderDbHelper.deleteItem(groupName, itemName);
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
        if (itemArray.size() == 0) {
            itemArray.add("(no items)");
        }
        cursor.close();

        /**
        for (int i = 0; i < checked.size(); i++) {
            if ((int) checked.get(i) == 1) {
                itemList.setItemChecked(i, true);
            }
        }
         */

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this,android.R.layout.simple_list_item_multiple_choice, itemArray);
        itemList.setAdapter(arrayAdapter);

        // register onClickListener to handle click events on each item
        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                //Toast.makeText(getApplicationContext(), "Long press to delete", Toast.LENGTH_SHORT).show();
                String selectedItem = itemArray.get(position);
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
