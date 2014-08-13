package org.examples.contactorganizer;

import android.net.Uri;

/**
 * Created by marneypt on 8/9/14.
 */
public class Contact {

    private  String _name, _phone ,_email, _addess;
    private  Uri _imageUri;
    private int _id;


    public Contact(int id, String name, String phone, String email, String addess,  Uri imageUri){

        _name = name;
        _phone = phone;
        _email = email;
        _addess = addess;
        _imageUri = imageUri;
        _id = id;

    }

    public String get_name() {
        return _name;
    }

    public String get_addess() {
        return _addess;
    }

    public String get_email() { return _email; }

    public String get_phone() {
        return _phone;
    }

    public Uri get_imageUri(){
        return _imageUri;
    }

    public int get_id(){
        return _id;
    }
}
