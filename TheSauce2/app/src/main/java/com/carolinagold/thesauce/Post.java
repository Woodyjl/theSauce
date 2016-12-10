package com.carolinagold.thesauce;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;

/**
 * Created by woodyjean-louis on 12/9/16.
 */

public class Post extends Object implements Serializable {

    private String userId;
    private String userName;
    private String imagePath;
    private String date;
    private String location;
    private String caption;

    public Post(String userID, String userName, @NonNull String imagePath, String date, String location, String caption) {
        this.userId = userID;
        this.userName = userName;
        this.imagePath = imagePath;
        this.date = date;
        this.location = location;
        this.caption = caption;
    }

    public String getUserId() {return userId;}

    public String getUserName() {return userName;}

    public String getImagePath() {return imagePath;}

    public String getDate() {return date;}

    public String getLocation() {return location;}

    public String getCaption() {return caption;}
}
