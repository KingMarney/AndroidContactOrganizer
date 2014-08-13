package org.examples.contactorganizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marneypt on 8/10/14.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "contactManager",
        TABLE_CONTACTS = "contacts",
        KEY_ID = "id",
        KEY_NAME= "name",
        KEY_PHONE = "phone",
        KEY_EMAIL = "email",
        KEY_ADDRESS = "address",
        KEY_IMAGEURI = "imageUri";

    public DatabaseHandler (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CONTACTS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                +KEY_NAME+" TEXT,"+KEY_PHONE+" TEXT,"+KEY_EMAIL+" TEXT,"
                + KEY_ADDRESS+" TEXT,"+ KEY_IMAGEURI+" TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    public void createContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID,contact.get_id());
        values.put(KEY_NAME,contact.get_name());
        values.put(KEY_PHONE,contact.get_phone());
        values.put(KEY_EMAIL,contact.get_email());
        values.put(KEY_ADDRESS,contact.get_addess());
        values.put(KEY_IMAGEURI,String.valueOf(contact.get_imageUri()));

        db.insert(TABLE_CONTACTS,null,values);

        db.close();
    }

    public Contact getContact(int id){

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS,new String[]{KEY_ID,KEY_NAME,KEY_PHONE,KEY_EMAIL,KEY_ADDRESS,KEY_IMAGEURI},KEY_ID + "=?", new String[]{ String.valueOf(id)},null,null,null,null);

        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4), Uri.parse(cursor.getString(5)));

        db.close();
        cursor.close();

        return contact;

    }

    public void deleteContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_CONTACTS,KEY_ID + "=?" , new String[] {String.valueOf(contact.get_id())});

        db.close();

    }

    public int getContactsCount(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_CONTACTS,null);
        int temp = cursor.getCount();
        db.close();
        cursor.close();
        return temp;
    }

    public int updateContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NAME,contact.get_name());
        values.put(KEY_PHONE,contact.get_phone());
        values.put(KEY_EMAIL,contact.get_email());
        values.put(KEY_ADDRESS,contact.get_addess());
        values.put(KEY_IMAGEURI,String.valueOf(contact.get_imageUri()));


        int temp = db.update(TABLE_CONTACTS,values,KEY_ID + "=?",new String[]{String.valueOf(contact.get_id())});
        db.close();

        //return the rows that was affected
        return temp;
    }


    public List<Contact> getAllContacts(){
        List<Contact> contacts = new ArrayList<Contact>();
         SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS,null);

        if (cursor.moveToFirst()){
            do {
                contacts.add(new Contact(Integer.parseInt(cursor.getString(0)),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4), Uri.parse(cursor.getString(5))));

            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contacts;
    }
}
