package com.example.jonathan.inventoryassistant;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/** Source attribution: Some NFC help from this tutorial:
 *  http://tapintonfc.blogspot.com/2012/07/the-above-footage-from-our-nfc-workshop.html
 */

public class WriteNfcTag extends Activity {
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mWriteTagFilters;
    private PendingIntent mNfcPendingIntent;
    private boolean writeProtect = false;
    private Context context;
    String groupName;
    String itemName;
    String textToWrite;
    boolean workingWithGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_nfc_tag);
        context = getApplicationContext();

        getActionBar().setDisplayShowHomeEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setLogo(R.drawable.action_bar_logo);
        getActionBar().setDisplayUseLogoEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.backArrow), PorterDuff.Mode.SRC_ATOP);
        getActionBar().setHomeAsUpIndicator(upArrow);

        groupName = getIntent().getStringExtra("groupName");
        itemName = getIntent().getExtras().getString("itemName", "");
        textToWrite = getIntent().getStringExtra("textToWrite");
        setTitle("Write Data to Tag");
        confirmTitle(textToWrite);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter discovery=new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] { discovery };
    }

    public void confirmTitle(String textToWrite) {
        if (!textToWrite.contains("---")) {
            workingWithGroup = true;
            setTitle("Group Tag: " + groupName);
            setContentView(R.layout.activity_write_nfc_group_tag);
            TextView t = (TextView) findViewById(R.id.help_msg);
            t.setText("You can attach a tag to a container (e.g. backpack) to count for all the items in the Group. When the tag is scanned, all the Group's Items will be checked off. Now hold your phone to the tag!");
            t.setTextSize(21);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_write_nfc_tag, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        if (workingWithGroup) {
            i.setClass(this, GroupList.class);
        } else {
            i.setClass(this, ItemList.class);
            i.putExtra("groupName", groupName);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled()){
                LayoutInflater inflater = getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.activity_write_nfc_tag,(ViewGroup) findViewById(R.id.settings));
                new AlertDialog.Builder(this).setView(dialoglayout)
                        .setPositiveButton("Update Settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(setnfc);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                finish(); // exit application if user cancels
                            }
                        }).create().show();
            }
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
        } else {
            Toast.makeText(context, "Sorry, No NFC Adapter found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        textToWrite = getIntent().getStringExtra("textToWrite");
        groupName = getIntent().getStringExtra("groupName");
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            // validate that this tag can be written
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(supportedTechs(detectedTag.getTechList())) {
                if(writableTag(detectedTag)) {
                    WriteResponse wr = writeTag(getTagAsNdef(textToWrite), detectedTag);
                    String message = (wr.getStatus() == 1? "" : "") + wr.getMessage();
                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,"This tag is not writable",Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context,"This tag type is not supported",Toast.LENGTH_SHORT).show();
            }
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void skipScan(View view) {
        Intent i = new Intent();
        if (workingWithGroup) {
            i.setClass(this, GroupList.class);
        } else {
            i.setClass(this, ItemList.class);
            i.putExtra("groupName", groupName);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public WriteResponse writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        String mess = "";

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    return new WriteResponse(0, "Tag is read-only");
                }
                if (ndef.getMaxSize() < size) {
                    mess = "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.";
                    return new WriteResponse(0,mess);
                }

                ndef.writeNdefMessage(message);
                if(writeProtect)  ndef.makeReadOnly();

                Intent i;
                Log.d("writeTag", "About to write the tag");
                if (!itemName.equals("")) {
                    ItemReaderDbHelper itemReaderDbHelper = new ItemReaderDbHelper(this);
                    Log.d("writeTag", "Things we're writing are: " + groupName + ", " + itemName + ", " + textToWrite);
                    itemReaderDbHelper.updateNfcTag(groupName, itemName, textToWrite);
                    mess = "Tag written successfully!";
                    i = new Intent(this, ItemList.class);
                    i.putExtra("groupName", groupName);
                } else {
                    Log.d("writeTag", "Writing the Group Tag!!");
                    GroupReaderDbHelper groupReaderDbHelper = new GroupReaderDbHelper(this);
                    groupReaderDbHelper.updateNfcTag(groupName, textToWrite);
                    mess = "Tag written successfully!";
                    i = new Intent(this, GroupList.class);
                    //i.putExtra("groupName", groupName);
                }
                Log.d("writeTag", "Past the SQL block");
                startActivity(i);
                return new WriteResponse(1, mess);
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        mess = "Formatted tag and wrote message";
                        return new WriteResponse(1,mess);
                    } catch (IOException e) {
                        mess = "Failed to format tag.";
                        return new WriteResponse(0,mess);
                    }
                } else {
                    mess = "Tag doesn't support NDEF.";
                    return new WriteResponse(0,mess);
                }
            }
        } catch (Exception e) {
            mess = "Failed to write tag!";
            Toast.makeText(context, "Failed to write tag!", Toast.LENGTH_SHORT).show();
            writeTag(message, tag);
            return new WriteResponse(0, "Write finished");
        }
    }

    private class WriteResponse {
        int status;
        String message;
        WriteResponse(int Status, String Message) {
            this.status = Status;
            this.message = Message;
        }
        public int getStatus() {
            return status;
        }
        public String getMessage() {
            return message;
        }
    }

    public static boolean supportedTechs(String[] techs) {
        boolean ultralight=false;
        boolean nfcA=false;
        boolean ndef=false;
        for(String tech:techs) {
            if(tech.equals("android.nfc.tech.MifareUltralight")) {
                ultralight=true;
            }else if(tech.equals("android.nfc.tech.NfcA")) {
                nfcA=true;
            } else if(tech.equals("android.nfc.tech.Ndef") || tech.equals("android.nfc.tech.NdefFormatable")) {
                ndef=true;
            }
        }
        if(ultralight && nfcA && ndef) {
            return true;
        } else {
            return false;
        }
    }

    private boolean writableTag(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(context,"Tag is read-only.",Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return false;
                }
                ndef.close();
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(context,"Failed to read tag",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private NdefMessage getTagAsNdef(String textToWrite) {
        byte[] text = textToWrite.getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[text.length + 3];
        payload[0] = 0x02; // 0x02 = UTF8
        payload[1] = 'e'; // Language = en
        payload[2] = 'n';
        System.arraycopy(text, 0, payload, 3, text.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return new NdefMessage(new NdefRecord[]{record});
    }
}