package com.carolinagold.thesauce;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private String TAG = "MainActivity";
    //google database objects
    private FirebaseAuth mAuth;
    //keeps track of user logging in or out
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        mAuth = FirebaseAuth.getInstance();
        //tracks when user signs in or out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if(user != null) {
                    /*
                    user is signed in, stay in the main activity
                     */
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }
                else {
                    /*
                    user is signed out, take them to the login screen
                     */
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }

        };

        //sign out button
        Button signInButton = (Button) findViewById(R.id.email_logout_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user = mAuth.getCurrentUser();
                if(user != null) {
                    mAuth.signOut();
                }
            }

        });

    }

    //assigns state listener to our Authenticator
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
}
