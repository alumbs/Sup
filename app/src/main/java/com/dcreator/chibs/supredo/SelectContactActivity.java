package com.dcreator.chibs.supredo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectContactActivity extends Activity {
    private ArrayList<Contact> contactItems = null;
    private final Context context = this;

    private HashMap<String, Contact> contactMap = null;
    private ContactAdapter m_adapter = null;

    private static final Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private static final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private static final String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

    //This uses the right value based on if the sdk version is lower
    //than honeycomb or not
    private static final String DISPLAY_NAME =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                ContactsContract.Contacts.DISPLAY_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactItems = new ArrayList<Contact>();
        contactMap = new HashMap<String, Contact>();

        setContentView(R.layout.activity_select_contact);

        launchApp();
    }

    private void launchApp()
    {
        launchSupTextApp();
    }

    private void launchSupTextApp()
    {
        //Load all contacts first
        getContactsForContactClass();

        ListView lv = (ListView) findViewById(R.id.contactsListView);

        if(contactItems == null || contactItems.size() <= 0)
        {
            new AlertDialog.Builder(context)
                .setTitle("No contact available")
                .setMessage("You have no contacts on your phone. Add a contact and say Sup now!!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // close the activity
                        finish();
                    }
                })
                .show();
        }
        //if(contactItems != null && contactItems.size() > 0) {
        else{
            //Add the loaded contacts to the adapter
            this.m_adapter = new ContactAdapter(this, R.layout.contacts_list_item, contactItems);

            // SET THIS ADAPTER AS YOUR LISTACTIVITY'S ADAPTER
            lv.setAdapter(this.m_adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Contact contact = contactItems.get(i);
                    String contactName = contact.getDisplayName();

                    //If the contact has more than one phone number,
                    // Just send a message to the first number in the list - (FOR NOW)
                    if (contact.getPhoneList().size() > 0) {
                        //TODO:Fix this please and show a window for them to select a number
                        String phoneNumber = contact.getPhoneList().get(0);

                        //Send a Sup message to the contact selected
                        sendSupSms(phoneNumber);

                        //Log.i("Sup", "List view item got clicked " + contactName);
                        new AlertDialog.Builder(context)
                            .setTitle("Contact Sup'd")
                            .setMessage(contactName + " has been Sup'd")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // close the activity
                                    finish();
                                }
                            })
                            .show();
                    } else {
                        new AlertDialog.Builder(context)
                            .setTitle("Contact Not Sup'd")
                            .setMessage(contactName + " not Sup'd. " + contactName + " has no number listed")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // close the activity
                                    finish();
                                }
                            })
                            .show();
                    }
                }
            });
        }
    }

    private void getContactsForContactClass()
    {
        //reset the contactmap
        contactMap = new HashMap<String, Contact>();
        ContentResolver contentResolver = getContentResolver();

        // Query and loop for every phone number of the contact
        //Trying to go through the phone alone as opposed to going through contacts
        Cursor phoneCursor = contentResolver.query(PHONE_CONTENT_URI, null, null, null, "DISPLAY_NAME ASC");
        while (phoneCursor.moveToNext()) {
            String contactId = phoneCursor.getString(phoneCursor.getColumnIndex(PHONE_CONTACT_ID));
            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
            int nameIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            String name = phoneCursor.getString(nameIdx);
            if(!name.equals("") && !phoneNumber.equals("")) {
                //YOUR TREATMENT
                if(!contactMap.containsKey(contactId)) {
                    Contact contact = new Contact();
                    contact.setDisplayName(name);
                    contact.setID(contactId);
                    //Contact contact = contactMap.get(contactId);
                    contact.addPhoneNumber(phoneNumber);

                    contactItems.add(contact);

                    contactMap.put(contactId, contact);
                }
                else
                {
                    Contact contact = contactMap.get(contactId);
                    contact.addPhoneNumber(phoneNumber);
                }
            }
        }
        phoneCursor.close();
    }

    //Send SMS
    private void sendSupSms(String phoneNumber)
    {
        String message = "Sup \n\n -Sent from the Sup app";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //A private ContactAdapter to help display my Contact Object in a ListView
    private class ContactAdapter extends ArrayAdapter<Contact> {

        private ArrayList<Contact> items;

        public ContactAdapter(Context context, int textViewResourceId, ArrayList<Contact> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.contacts_list_item, null);
            }
            Contact o = items.get(position);
            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                if (tt != null) {
                    tt.setText(o.getDisplayName());
                }
            }
            return v;
        }
    }
}