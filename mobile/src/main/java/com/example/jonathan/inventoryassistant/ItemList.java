package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemList extends Activity {

    ItemReaderDbHelper itemReaderDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        itemReaderDbHelper = new ItemReaderDbHelper(this);
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

    private void makeItemList() {
        Cursor cursor = itemReaderDbHelper.getAllItems();
        cursor.moveToFirst();

        String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemReaderContract.ItemEntry.ITEM_NAME));
        TextView text = new TextView(this);
        text.setText(itemName);
        RelativeLayout topLayout = (RelativeLayout) findViewById(R.id.topLayout);
        topLayout.addView(text);
    }
}
