package com.carolinagold.thesauce;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.List;

public class SignUpActivity extends AppCompatActivity {


        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private static final int REQUEST_READ_CONTACTS = 0;

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private static final String[] DUMMY_CREDENTIALS = new String[]{
                "foo@example.com:hello", "bar@example.com:world"
        };
        /**
         * Keep track of the login task to ensure we can cancel it if requested.
         */
        private com.carolinagold.thesauce.SignUpActivity.UserAccountCreationTask mAuthTask = null;

        // UI references.
        private AutoCompleteTextView mNameView;
        private AutoCompleteTextView mEmailView;
        private EditText mPasswordView;
        private EditText mPasswordConfirmView;


        private View mProgressView;
        private View mSignUpFormView;

        //google database object
        private FirebaseAuth mAuth;
        //tracks when user signs in or out
        private FirebaseAuth.AuthStateListener mAuthListener;
        //tag for Log statements for debugging
        private String TAG = "SignUpActivity";
        private String displayName;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sign_up);

            prepare();
        }


        private void prepare() {
            mAuth = FirebaseAuth.getInstance();


            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    //gets the user to see if he is signed in or not
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {

                        //User account has been created and they are now signed in.

                        //set new user's display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });

                        //go to main activity now that user has account
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    } else {
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                    }
                    // ...
                }
            };



            // Set up the login form.
            mEmailView = (AutoCompleteTextView) findViewById(R.id.sign_up_email);
            populateAutoComplete();
            mNameView = (AutoCompleteTextView) findViewById(R.id.sign_up_name);


            mPasswordView = (EditText) findViewById(R.id.sign_up_password);
            //user must type password twice to confirm password
            mPasswordConfirmView = (EditText) findViewById(R.id.sign_up_confirm_password);


            Button signUpButton = (Button) findViewById(R.id.email_create_account_button);
            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    attemptAccountCreation();
                }
            });

            mSignUpFormView = findViewById(R.id.sign_up_form);
            mProgressView = findViewById(R.id.sign_up_progress);
        }

        @Override
        protected void onStart() {
            super.onStart();
        }
        protected void onStop() {
            super.onStop();

        }

        private void populateAutoComplete() {
            if (!mayRequestContacts()) {
                return;
            }
        }

        private boolean mayRequestContacts() {

            return true;
        }


        /**
         * Attempts to sign in or register the account specified by the login form.
         * If there are form errors (invalid email, missing fields, etc.), the
         * errors are presented and no actual login attempt is made.
         */
        private void attemptAccountCreation() {
            if (mAuthTask != null) {
                return;
            }

            // Reset errors.
            mEmailView.setError(null);
            mPasswordView.setError(null);

            // Store values at the time of the login attempt.
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();
            String passwordConfirm = mPasswordConfirmView.getText().toString();
            displayName = mNameView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check for a valid password, if the user entered one.
            //also check if both passwords are equal
            if (TextUtils.isEmpty(password) || !isPasswordValid(password) || !password.equals(passwordConfirm)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }
            //check if display name was inputted
            if(TextUtils.isEmpty(displayName)) {
                mNameView.setError(getString(R.string.error_field_required));
                focusView = mNameView;
                cancel = true;
            }


            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);
                mAuthTask = new com.carolinagold.thesauce.SignUpActivity.UserAccountCreationTask(email, password);
                mAuthTask.execute((Void) null);
            }
        }

        private boolean isEmailValid(String email) {
            //TODO: Replace this with your own logic
            //Login may require username instead of email
            return email.contains("@");
        }

        private boolean isPasswordValid(String password) {
            //TODO: Replace this with your own logic
            return password.length() > 4;
        }

        /**
         * Shows the progress UI and hides the login form.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        private void showProgress(final boolean show) {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
            // for very easy animations. If available, use these APIs to fade-in
            // the progress spinner.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                mSignUpFormView.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressView.animate().setDuration(shortAnimTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }

        private void addToAutoComplete(List<String> suggestions) {
            //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(com.carolinagold.thesauce.SignUpActivity.this,
                            android.R.layout.simple_dropdown_item_1line, suggestions);

            mEmailView.setAdapter(adapter);
        }

        /**
         * Represents an asynchronous login/registration task used to authenticate
         * the user.
         */
        public class UserAccountCreationTask extends AsyncTask<Void, Void, Boolean> {

            private final String mEmail;
            private final String mPassword;

            UserAccountCreationTask(String email, String password) {
                mEmail = email;
                mPassword = password;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                // TODO: attempt authentication against a network service.



                //create new user account
                mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                        .addOnCompleteListener(com.carolinagold.thesauce.SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail", task.getException());
                                    Toast.makeText(com.carolinagold.thesauce.SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                try {

                    Thread.sleep(2000);
                }
                //simulate network access
                catch (InterruptedException e) {
                    return false;
                }

                for (String credential : DUMMY_CREDENTIALS) {
                    String[] pieces = credential.split(":");
                    if (pieces[0].equals(mEmail)) {
                        // Account exists, return true if the password matches.
                        return pieces[1].equals(mPassword);
                    }
                }

                // TODO: register the new account here.
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                mAuthTask = null;
                showProgress(false);

                if (success) {
                    finish();
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }

            @Override
            protected void onCancelled() {
                mAuthTask = null;
                showProgress(false);
            }
        }
    }




