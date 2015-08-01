package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ItemList extends Activity {

    ItemReaderDbHelper itemReaderDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        itemReaderDbHelper = new ItemReaderDbHelper(this);
        makeItemList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
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

    ArrayList<String> itemArray;

    private void makeItemList() {
        Cursor cursor = itemReaderDbHelper.getAllItemsInGroup("TestGroup");
        cursor.moveToPosition(-1);

        ListView itemList = (ListView) findViewById(R.id.itemList);
        itemArray = new ArrayList<>();

        while (cursor.moveToNext()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.ITEM_NAME));
            itemArray.add(itemName);
            Log.d("ItemList", "Trying to print out all the items in the ItemList");
        }
        cursor.close();

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, itemArray);
        itemList.setAdapter(arrayAdapter);

        // register onClickListener to handle click events on each item
        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
            {
                String selectedItem = itemArray.get(position);
                Toast.makeText(getApplicationContext(), "Item Selected : " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
