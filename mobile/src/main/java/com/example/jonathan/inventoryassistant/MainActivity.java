package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

    GroupReaderDbHelper groupReaderDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_new_grp);

        groupReaderDbHelper = new GroupReaderDbHelper(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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

    public void makeNewGroup(View view) {
        String groupName = ((EditText) findViewById(R.id.groupName)).getText().toString();
        Log.d("CLICK", "MAKE NEW GROUP");
        if (groupName.compareTo("") != 0) {
            groupReaderDbHelper.insertGroup(groupName);
            Intent intent = new Intent(this, GrpList.class);
            startActivity(intent);
        }
    }

    public void deleteAllGroups(View view) {
        Log.d("ClICK", "DELETE ALL GROUPS");
        groupReaderDbHelper.deleteAllGroups();
    }
}
