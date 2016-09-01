package com.projects.codeyasam.threatmap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 * Created by codeyasam on 9/1/16.
 */
public class Client_TM {

    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private Bitmap displayPicture;
    private String displayPicturePath = "DISPLAY_PICTURES/default_avatar.png";
    private String address;
    private String lat;
    private String lng;
    private String contactNo;
    private String username;
    private String password;
    private String personNotif;
    private String relation;
    private String identificationNo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Bitmap getDisplayPicture() {
        return displayPicture;
    }

    public void setDisplayPicture(Bitmap displayPicture) {
        this.displayPicture = displayPicture;
    }

    public String getDisplayPicturePath() {
        return displayPicturePath;
    }

    public void setDisplayPicturePath(String displayPicturePath) {
        this.displayPicturePath = displayPicturePath;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPersonNotif() {
        return personNotif;
    }

    public void setPersonNotif(String personNotif) {
        this.personNotif = personNotif;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getIdentificationNo() {
        return identificationNo;
    }

    public void setIdentificationNo(String identificationNo) {
        this.identificationNo = identificationNo;
    }

    public static void setDefaultImage(Activity activity, int id, int drawable) {
        ImageView ivImage = (ImageView) activity.findViewById(id);
        Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), drawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        ivImage.setImageBitmap(CYM_UTILITY.getRoundedCornerBitmap(bm));
    }
}
