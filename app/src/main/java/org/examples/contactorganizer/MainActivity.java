package org.examples.contactorganizer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends ActionBarActivity {

    private static final int EDIT = 0, DELETE = 1;

    EditText nameTxt, phoneTxt, emailTxt, addressTxt;
    ImageView ivContactImage;
    List<Contact> Contacts = new ArrayList<Contact>();
    ListView contactListView;
    Uri imageUir = Uri.parse("android.resource://org.examples.contactorganizer/drawable/contact_image.png");
    protected static DatabaseHandler dbHandler;
    int longClickedItemIndex;
    ArrayAdapter<Contact> contactAdapter;

    final static int RESULT_LOAD_IMAGE = 0;
    final static int RESULT_EDIT_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        nameTxt = (EditText) findViewById(R.id.txtName);
        phoneTxt = (EditText) findViewById(R.id.txtPhone);
        emailTxt = (EditText) findViewById(R.id.txtEmail);
        addressTxt = (EditText) findViewById(R.id.txtAddess);
        contactListView = (ListView) findViewById(R.id.listView);
        ivContactImage = (ImageView) findViewById(R.id.ivContactImage);
        dbHandler = new DatabaseHandler(getApplicationContext());
        ClearCreator();


        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        //This add an action lister to the click of the button.
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = new Contact(dbHandler.getContactsCount() + 1, String.valueOf(nameTxt.getText()), String.valueOf(phoneTxt.getText()), String.valueOf(emailTxt.getText()), String.valueOf(addressTxt.getText()), imageUir);
                //addContact(0,nameTxt.getText().toString(),phoneTxt.getText().toString(),emailTxt.getText().toString(),addressTxt.getText().toString(),imageUir);
                dbHandler.createContact(contact);
                Contacts.add(contact);
                SortContacts();
                //This is the warning message that does not need to dismiss
                Toast.makeText(getApplicationContext(), nameTxt.getText().toString() + " have been Created", Toast.LENGTH_SHORT).show();
            }
        });


        registerForContextMenu(contactListView);

        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longClickedItemIndex = position;
                return false;
            }
        });

        TabHost tabhost = (TabHost) findViewById(R.id.tabHost);

        tabhost.setup();

        TabHost.TabSpec tabSpec = tabhost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Create Contact");
        tabhost.addTab(tabSpec);

        tabSpec = tabhost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("Contact List");
        tabhost.addTab(tabSpec);

        ivContactImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        phoneTxt.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //This will enable the addBtn
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addBtn.setEnabled(String.valueOf(nameTxt.getText()).trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (dbHandler.getContactsCount() != 0)
            Contacts.addAll(dbHandler.getAllContacts());

        populateList();
    }

    private void populateList() {
        contactAdapter = new ContactListAdpter();
        contactListView.setAdapter(contactAdapter);
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderIcon(R.drawable.pencil_icon);
        menu.setHeaderTitle("Contact Options");
        menu.add(Menu.NONE, EDIT, menu.NONE, "Edit Contact");
        menu.add(Menu.NONE, DELETE, menu.NONE, "Delete Contact");

    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case EDIT:
                Intent intent = new Intent(this,EditContact.class);
                Contact contact = Contacts.get(longClickedItemIndex);

                intent.putExtra("name", String.valueOf(contact.get_name()));
                intent.putExtra("id", String.valueOf(contact.get_id()));
                intent.putExtra("phone", String.valueOf(contact.get_phone()));
                intent.putExtra("address", String.valueOf(contact.get_addess()));
                intent.putExtra("email", String.valueOf(contact.get_email()));
                intent.putExtra("image", String.valueOf(contact.get_imageUri()));

                startActivityForResult(intent, RESULT_EDIT_CONTACT);
                return true;
            case DELETE:
                dbHandler.deleteContact(Contacts.get(longClickedItemIndex));
                Contacts.remove(longClickedItemIndex);
                SortContacts();
                return true;
        }
        return false;
    }

    private boolean existingContact(Contact contact) {
        String name = contact.get_name();
        String phone = contact.get_phone();

        int contactCount = Contacts.size();

        for (int i = 0; i < contactCount; i++) {
            if (name.compareTo(Contacts.get(i).get_name()) == 0) {
                if (phone.compareTo(Contacts.get(i).get_phone()) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_EDIT_CONTACT && resultCode == RESULT_OK && null != data) {

            String name = data.getStringExtra("name");
            String email = data.getStringExtra("email");
            String phone = data.getStringExtra("phone");
            String address = data.getStringExtra("address");
            final String tempImageUir = data.getStringExtra("image");
            final String tempContactID = data.getStringExtra("id");
            int contactID = Integer.parseInt(tempContactID);
            Uri imageUri = Uri.parse(tempImageUir);

            Contact tempContact = new Contact(contactID,name,phone,email,address,imageUri);

            dbHandler.updateContact(tempContact);

            int contactCount = Contacts.size();
            for (int i = 0; i < contactCount; i++) {
                if (contactID == Contacts.get(i).get_id()) {
                    Contacts.remove(i);
                    Contacts.add(tempContact);
                    break;
                }
            }
            SortContacts();


        }
       else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            String filePath = null;
            imageUir = data.getData();
            Log.d("", "URI = " + imageUir);

            String url = imageUir.toString();
            Bitmap bitmap = null;
            InputStream is = null;
            if (url.startsWith("content://com.google.android.apps.photos.content")) {
                try {
                    is = this.getContentResolver().openInputStream(imageUir);
                    ivContactImage.setImageBitmap(BitmapFactory.decodeStream(is));

                } catch (Exception e) {
                    imageUir = Uri.parse("android.resource://org.examples.contactorganizer/drawable/contact_image.png");
                }
                return;
            } else if (imageUir != null && "content".equals(imageUir.getScheme())) {

                Cursor cursor = this.getContentResolver().query(imageUir, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
                cursor.moveToFirst();
                filePath = cursor.getString(0);
                cursor.close();
            } else {
                filePath = imageUir.getPath();
            }
            ivContactImage.setImageBitmap(BitmapFactory.decodeFile(filePath));
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class ContactListAdpter extends ArrayAdapter<Contact> {

        public ContactListAdpter() {

            super(MainActivity.this, R.layout.listview_item, Contacts);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Contact currentContact = Contacts.get(position);

            TextView name = (TextView) view.findViewById(R.id.contactName);
            name.setText(currentContact.get_name());

            TextView phone = (TextView) view.findViewById(R.id.phoneNumber);
            phone.setText(currentContact.get_phone());

            TextView email = (TextView) view.findViewById(R.id.emailAddress);
            email.setText(currentContact.get_email());

            TextView address = (TextView) view.findViewById(R.id.cAddress);
            address.setText(currentContact.get_addess());


            //ImageView contactImage = (ImageView) view.findViewById(R.id.ivContact);
            setContactImage((ImageView) view.findViewById(R.id.ivContact), currentContact,position);

            //contactImage.setImageURI(currentContact.get_imageUri());

            return view;
        }
    }


    private void setContactImage(ImageView imageView, Contact currentContact,int position) {
        // imageView.setImageURI(currentContact.get_imageUri())
        String filePath = null;
        Uri tempUri = currentContact.get_imageUri();
        Log.d("", "URI = " + tempUri);

        String url = tempUri.toString();

        if (url.startsWith("content://com.google.android.apps.photos.content")) {

            DownloadBitmap bitmap = new DownloadBitmap(imageView, currentContact,position);
            bitmap.execute();

        } else if (url.startsWith("android.resource://org.examples.contactorganizer/drawable/contact_image.png")){
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.contact_image));
        }else{
            imageView.setImageURI(currentContact.get_imageUri());
        }
    }
    private class DownloadBitmap extends AsyncTask<Void, Void, Void> {
        ImageView _image;
        Contact _contact;
        private int _position;
        private String _url = null;
        private Bitmap _bitmap = null;

        public DownloadBitmap(ImageView image, Contact contact,int position){
            _image = image;
            _contact = contact;
            _position = position;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Uri tempUri = _contact.get_imageUri();
            _url = parseGoogleUri(tempUri);

        }

        @Override
        protected Void doInBackground(Void... params) {
            _bitmap = getBitmapFromURL(_url);
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            View contactView = contactListView.getChildAt(_position);
            _image = (ImageView) contactView.findViewById(R.id.ivContact);
            _image.setImageBitmap(_bitmap);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String parseGoogleUri(Uri uri){
        String buildURL = "";
        String googleUri = String.valueOf(uri);
        String initParse = "https";
        String[] tokens = googleUri.split(initParse);

        buildURL = "https" + tokens[tokens.length-1];
        buildURL = buildURL.replace("%3A",":");
        buildURL = buildURL.replace("%3Ds0-d"," ");
        buildURL = buildURL.replace("%2F","/");

        return buildURL;
    }

    private void SortContacts(){
        Collections.sort(Contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact person1, Contact person2) {

                return person1.get_name().compareTo(person2.get_name());
            }
        });

        contactAdapter.notifyDataSetChanged();
    }

    private void ClearCreator(){
        nameTxt.setText("");
        phoneTxt.setText("");
        emailTxt.setText("");
        addressTxt.setText("");
        ivContactImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_image));
    }
}

