package com.carolinagold.thesauce;

import android.app.Activity;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostCreator extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {


    private String uId;
    private String displayName;
    private String caption;

    private ImageView photoImageView;
    private EditText captionEditText;
    private TextView locationTextView;
    private Uri pictureUri;

    private static final int RESULT_FROM_GALLERY = 1;
    private static final int RESULT_FROM_CAMERA = 2;

    public boolean photoChosen = false;

    private GoogleApiClient googleAPIClient;
    private Location currentLocation;
    private LocationRequest locationRequest;

    private double latitude;
    private double longitude;

    private FirebaseDatabase dbRef = FirebaseDatabase.getInstance();



    private String decodedAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creator);

        uId = this.getIntent().getStringExtra("uId");
        displayName = this.getIntent().getStringExtra("displayName");

        photoImageView = (ImageView) findViewById(R.id.edit_image);
        captionEditText = (EditText) findViewById(R.id.caption_text);
        locationTextView = (TextView)findViewById(R.id.location_text);

        googleAPIClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this).addOnConnectionFailedListener(this).
                addApi(LocationServices.API).build();

    }
    public void onStart() {
        googleAPIClient.connect();
        super.onStart();
    }
    public void onStop() {
        Log.i("test", "is stopped");

        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleAPIClient,this);
        googleAPIClient.disconnect();
        super.onStop();
    }
    public void onConnected(Bundle connectionHint) {
        Log.i("test", "is connected");
        String errorMessage = "";
        LocationSettingsRequest.Builder settingsBuilder;
        PendingResult<LocationSettingsResult> pendingResult;
        LocationSettingsResult settingsResult;

        locationRequest = new LocationRequest();

        locationRequest.setInterval(getResources().getInteger(
                R.integer.time_between_location_updates_ms));
        locationRequest.setFastestInterval(getResources().getInteger(
                R.integer.time_between_location_updates_ms) / 2);
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        settingsBuilder = new LocationSettingsRequest.Builder();
        settingsBuilder.addLocationRequest(locationRequest);
        pendingResult = LocationServices.SettingsApi.checkLocationSettings(
                googleAPIClient,settingsBuilder.build());

        startLocationUpdates();
    }
    private void startLocationUpdates() {
        Log.i("TEST", "starting location updates");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleAPIClient,locationRequest,this);
        } catch (SecurityException e) {
            Toast.makeText(this,"Cannot get updates",Toast.LENGTH_LONG).show();
            finish();
        }

    }
    public void onLocationChanged(Location newLocation) {

        Log.i("TEST", "entered onLocationChanged");
        if(currentLocation == null) {
            currentLocation = newLocation;

            TextView locationText;
            String currentText;
            Time timeOfChange;

            locationText = (TextView) findViewById(R.id.location_text);

            //currentText = locationText.getText().toString();


            timeOfChange = new Time();
            timeOfChange.set(currentLocation.getTime());
            //currentText += timeOfChange.format("%A %D %T") + "   ";
            //currentText += "\nProvider " + currentLocation.getProvider() + " found location\n";

            latitude = newLocation.getLatitude();
            longitude = newLocation.getLongitude();

            decodedAddress = getCompleteAddressString(latitude, longitude);

            currentText = decodedAddress;

            //currentText += String.format("%.2f %s", newLocation.getLatitude(),
               //     newLocation.getLatitude() >= 0.0 ? "N" : "S") + "   ";
            //currentText += String.format("%.2f %s", newLocation.getLongitude(),
             //       newLocation.getLongitude() >= 0.0 ? "E" : "W") + "   ";
            //if (newLocation.hasAccuracy()) {
            //    currentText += String.format("%.2fm", newLocation.getAccuracy());
            //}
           // currentText += "\n\n";
            locationText.setText(currentText);

        }

    }

    public void onConnectionFailed(ConnectionResult result) {
        Log.i("test", "connection failed");
    }
    //-----------------------------------------------------------------------------
    public void onConnectionSuspended(int cause) {
        Log.i("test", "connection suspended");

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                android.location.Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("Current loction address", "" + strReturnedAddress.toString());
            } else {
                Log.w("Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Current loction address", "Canont get Address!");
        }
        return strAdd;
    }
<<<<<<< HEAD
=======
    protected void startIntentService() {
        Log.i("TEST", "entered startIntentService");

        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, currentLocation);
        startService(intent);
    }
    public void fetchAddressButtonHandler(View view) {
        // Only start the service to fetch the address if GoogleApiClient is
        // connected.
        if (googleAPIClient.isConnected() && currentLocation != null) {
            startIntentService();
        }
        // If GoogleApiClient isn't connected, process the user's request by
        // setting mAddressRequested to true. Later, when GoogleApiClient connects,
        // launch the service to fetch the address. As far as the user is
        // concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.

    }
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i("TEST", "entered onRecievedResult");
            // Display the address string
            // or an error message sent from the intent service.
            decodedAddress = resultData.getString(Constants.RESULT_DATA_KEY);
            locationTextView.setText(decodedAddress);


            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(PostCreator.this, R.string.address_found, Toast.LENGTH_LONG).show();

            }

        }
    }


>>>>>>> 331d8aa1abb2206c68d19270fb26085fd132d7e1

    public void myClickHandler(View view) {

        switch (view.getId()) {
            case R.id.edit_image:

                new AlertDialog.Builder(this)
                        .setTitle("Camera or Gallery")
                        .setMessage("Would you like to take a photo from the Gallery or the Camera?")
                        .setPositiveButton(getResources().getString(R.string.choose_gallery), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent galleryIntent;
                                galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(galleryIntent, RESULT_FROM_GALLERY);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.choose_camera), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivityForResult(takePictureIntent, RESULT_FROM_CAMERA);
                                }
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;
            case R.id.post_button:
                if(!photoChosen) {
                    Toast.makeText(this,"Pick a photo!",Toast.LENGTH_LONG).show();
                }
                else {


                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
                    String strDate = "Current Date : " + mdformat.format(calendar.getTime());

                    caption = (String)((EditText)findViewById(R.id.caption_text)).toString();
                    String imagePath = pictureUri.toString();

                    Post post = new Post(uId,displayName, imagePath, strDate, decodedAddress, caption);
                    
                }
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            pictureUri = data.getData();


            //Bitmap photo = (Bitmap) data.getExtras().get("data");
           // photoImageView.setImageBitmap(photo);
            photoImageView.setImageURI(pictureUri);
            photoChosen = true;
        }
        if (requestCode == RESULT_FROM_GALLERY && resultCode == RESULT_OK && null != data) {
            pictureUri = data.getData();
            //String[] filePathColumn = {MediaStore.Images.Media.DATA};
            //Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            //cursor.moveToFirst();
           // int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //String picturePath = cursor.getString(columnIndex);
            //cursor.close();
            ImageView imageView = (ImageView) findViewById(R.id.edit_image);
            imageView.setImageURI(pictureUri);
            //imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            photoChosen = true;
        }
    }



}
