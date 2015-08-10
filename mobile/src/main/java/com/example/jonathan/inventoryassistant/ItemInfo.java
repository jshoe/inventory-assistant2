package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ItemInfo extends Activity {

    String groupName = "";
    String itemName = "";
    ItemReaderDbHelper itemReaderDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemReaderDbHelper = new ItemReaderDbHelper(this);

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
        getActionBar().setDisplayHomeAsUpEnabled(true);
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
            Toast.makeText(getApplicationContext(), "Entry has no location logged", Toast.LENGTH_SHORT).show();
        } else {
            lat = Double.parseDouble(latitude.toString());
            lon = Double.parseDouble(longitude.toString());

            Intent i = new Intent();
            i.setClass(this, ScanLogMapView.class);
            Bundle b = new Bundle();
            b.putDouble("latitude", lat);
            b.putDouble("longitude", lon);
            b.putString("title", "Check-In");
            //b.putString("snippet", "Location");
            i.putExtras(b);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
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

        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED1)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED2)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED3)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED4)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED5)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED6)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED7)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED8)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED9)));
        tempArray.add(cursor.getString(cursor.getColumnIndex(ItemReaderContract.ItemEntry.DATE_CHECKED10)));
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
                    //deleteEntryDialog(itemArray.get(pos));
                    return true;
                }
            });
        }
    }
}
