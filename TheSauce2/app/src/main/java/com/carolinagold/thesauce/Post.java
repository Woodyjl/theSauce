package com.carolinagold.thesauce;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by woodyjean-louis on 12/9/16.
 */

public class Post extends Object implements Serializable {

    private final String firebase_storage_bucket_name = "gs://thesauce2-56d66.appspot.com";

    private String userId;
    private String userName;
    private String imagePath;
    private String date;
    private String location;
    private String caption;

    public Post() {

    }

    public Post(@NonNull String userID, String userName, @NonNull String imagePath, String date, String location, String caption) {
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

    public boolean pushToCloud(Context context) {

        // Will store the profile image and post image in storage
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(firebase_storage_bucket_name);
        storageRef = storageRef.child("Posts").child(userId);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imagePath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Post");

                    // Creates a post in the real time database with paths to the image file in storage
                    String key = myRef.child(userId).push().getKey();
                    myRef.child(userId).child(key).child("userProfilePicturePath").setValue("Hello, World!");
                    myRef.child(userId).child(key).child("userName").setValue(userName);
                    myRef.child(userId).child(key).child("uId").setValue(userId);
                    myRef.child(userId).child(key).child("location").setValue(location);
                    myRef.child(userId).child(key).child("caption").setValue(caption);
                    myRef.child(userId).child(key).child("date").setValue(date);
                    myRef.child(userId).child(key).child("imagePath").setValue(downloadUrl.toString());
                }
            });
        } catch (Exception e) {
            Log.i(Logs.ERROR_IN_TRY, "Exception happened");
            e.printStackTrace();
            Toast.makeText(context, "Error uploading photo", Toast.LENGTH_SHORT);
            return false;
        }




        return true;
    }
}
