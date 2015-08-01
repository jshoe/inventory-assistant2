package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MakeItem extends Activity {

    ItemReaderDbHelper itemReaderDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_item);

        itemReaderDbHelper = new ItemReaderDbHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_make_item, menu);
        return true;
    }

    public void makeNewItem(View view) {
        String itemName = ((EditText) findViewById(R.id.itemName)).getText().toString();
        String groupName = "TestGroup";
        Log.d("CLICK", "MAKE NEW ITEM");
        if (itemName.compareTo("") != 0) {
            itemReaderDbHelper.insertItem(groupName, itemName);
            Intent intent = new Intent(this, ItemList.class);
            startActivity(intent);
        }
    }

    public void deleteAllItems(View view) {
        Log.d("ClICK", "DELETE ALL ITEMS IN TEST GROUP");
        itemReaderDbHelper.deleteItemsInGroup("TestGroup");
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
}
