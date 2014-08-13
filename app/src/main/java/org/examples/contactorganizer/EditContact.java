package org.examples.contactorganizer;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.examples.contactorganizer.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by marneypt on 8/12/14.
 */
public class EditContact  extends Activity {
    SQLiteDatabase mDB;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.edit_contact);

            final TextView txtPhone = (TextView) findViewById(R.id.txtPhoneEditor);
            ImageView ivContact = (ImageView) findViewById(R.id.ivContactImageEditor);
            final TextView txtAddress = (TextView) findViewById(R.id.txtAddessEditor);
            final TextView txtName = (TextView) findViewById(R.id.txtNameEditor);
            final TextView txtEmail = (TextView) findViewById(R.id.txtEmailEditor);
            Button btnSubmit = (Button) findViewById(R.id.btnSubmit);

            Intent i = getIntent();
            // Receiving the Data

            String name = i.getStringExtra("name");
            String email = i.getStringExtra("email");
            String phone = i.getStringExtra("phone");
            String address = i.getStringExtra("address");
            final String tempImageUir = i.getStringExtra("image");
            final String tempContactID = i.getStringExtra("id");
            int contactID = Integer.parseInt(tempContactID);
            Uri imageUri = Uri.parse(tempImageUir);

            txtPhone.setText(phone);
            txtEmail.setText(email);
            txtName.setText(name);
            txtAddress.setText(address);
            setContactImage(ivContact,imageUri);

            // Displaying Received data
            txtName.setText(name);
            txtEmail.setText(email);


           // getApplication().d

            // Binding Click event to Button
            btnSubmit.setOnClickListener(new View.OnClickListener() {

                public void onClick(View arg0) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("name", String.valueOf(txtName.getText()));
                    resultIntent.putExtra("id", String.valueOf(tempContactID));
                    resultIntent.putExtra("phone", String.valueOf(txtPhone.getText()));
                    resultIntent.putExtra("address", String.valueOf(txtAddress.getText()));
                    resultIntent.putExtra("email", String.valueOf(txtEmail.getText()));
                    resultIntent.putExtra("image", String.valueOf(tempImageUir));
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            });

        }
    private class DownloadBitmap extends AsyncTask<Void, Void, Void> {
        ImageView _image;
        Uri _imageUri;
        private String _url = null;
        private Bitmap _bitmap = null;

        public DownloadBitmap(ImageView imageView, Uri imageUri){
            _image = imageView;
            _imageUri = imageUri;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _url = parseGoogleUri(_imageUri);

        }

        @Override
        protected Void doInBackground(Void... params) {
            _bitmap = getBitmapFromURL(_url);
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            _image.setImageBitmap(_bitmap);
        }
    }

    private void setContactImage(ImageView imageView, Uri imageUri) {
        // imageView.setImageURI(currentContact.get_imageUri())
        String filePath = null;

        Log.d("", "URI = " + imageUri);

        String url = imageUri.toString();

        if (url.startsWith("content://com.google.android.apps.photos.content")) {

            DownloadBitmap bitmap = new DownloadBitmap(imageView, imageUri);
            bitmap.execute();

        }  else if (url.startsWith("android.resource://org.examples.contactorganizer/drawable/contact_image.png")){
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.contact_image));
        }else{
            imageView.setImageURI(imageUri);
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
    }
