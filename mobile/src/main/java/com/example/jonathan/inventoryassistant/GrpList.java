package com.example.jonathan.inventoryassistant;

import android.app.Activity;
import android.content.Intent;
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

    public void makeNewGroup(View view) {
        Intent i = new Intent();
        i.setClass(this, MakeNewGrp.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void showItemList(String groupName) {
        Intent i = new Intent();
        i.putExtra("groupName", groupName);
        i.setClass(this, ItemList.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
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

    ArrayList<String> groupArray;

    private void makeGroupList() {
        Cursor cursor = groupReaderDbHelper.getAllGroups();
        cursor.moveToPosition(-1);

        ListView groupList = (ListView) findViewById(R.id.groupList);
        groupArray = new ArrayList<>();

        while (cursor.moveToNext()) {
            String groupName = cursor.getString(cursor.getColumnIndexOrThrow(GroupReaderContract.GroupEntry.GROUP_NAME));
            groupArray.add(groupName);
            Log.d("GrpLst", "Trying to print out all the items in the GroupList");
        }
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, groupArray);
        groupList.setAdapter(arrayAdapter);
        cursor.close();

        // register onClickListener to handle click events on each item
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                String selectedGroup = groupArray.get(position);
                showItemList(selectedGroup);
                Toast.makeText(getApplicationContext(), "Group Selected : " + selectedGroup, Toast.LENGTH_SHORT).show();

            }
        });
    }
}
