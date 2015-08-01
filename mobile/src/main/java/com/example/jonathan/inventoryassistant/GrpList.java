package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GrpList extends Activity {

    GroupReaderDbHelper groupReaderDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grp_list);

        groupReaderDbHelper = new GroupReaderDbHelper(this);
        makeGroupList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grp_list, menu);
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

    private void makeGroupList() {
        Cursor cursor = groupReaderDbHelper.getAllGroups();
        cursor.moveToPosition(-1);

        while (cursor.moveToNext()) {
            String groupName = cursor.getString(cursor.getColumnIndexOrThrow(GroupReaderContract.GroupEntry.GROUP_NAME));
            TextView text = new TextView(this);
            text.setText(groupName);
            RelativeLayout topLayout = (RelativeLayout) findViewById(R.id.topLayout);
            topLayout.addView(text);
        }

        

        cursor.close();
    }
}
