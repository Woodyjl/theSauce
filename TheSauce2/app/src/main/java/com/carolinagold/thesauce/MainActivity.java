package com.carolinagold.thesauce;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    public static final int LOGIN_ACTIVITY = 0;

    private SectionsPagerAdapter mSectionsPagerAdapter;


    private static final int POST_CREATE_ACTIVITY = 1;

    private ViewPager mViewPager;

    private Toolbar toolbar;
    private TabLayout tabLayout;


    private String TAG = "MainActivity";
    //google database objects
    private FirebaseAuth mAuth;
    //keeps track of user logging in or out
    private FirebaseAuth.AuthStateListener mAuthListener;
    public FirebaseUser user;
    private ProgressBar progressBar;

    NewFeedFragment newFeedFragment;
    ProfileFragment profileFragment;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(Logs.POINT_OF_INTEREST, "checkPoint");
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
                    finishActivity(LOGIN_ACTIVITY);
                }
                else {
                    /*
                    user is signed out, take them to the login screen
                     */
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOGIN_ACTIVITY);
                }
            }

        };

        progressBar = (ProgressBar) findViewById(R.id.main_activity_progress_bar);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setOnTabSelectedListener(tabSelectedListener);
        //tabLayout.setOnTabSelectedListener();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Create a new post
                String uId = user.getUid();
                String displayName = user.getDisplayName();



                Intent postCreateActivity;
                postCreateActivity = new Intent(MainActivity.this,PostCreator.class);
                postCreateActivity.putExtra("uId", uId);
                postCreateActivity.putExtra("displayName", displayName);
                startActivityForResult(postCreateActivity, POST_CREATE_ACTIVITY);

            }
        });



//        Snackbar.make(view, "Cool! you want to create a new post", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();

    }

    public FirebaseUser getUser() {
       return mAuth.getCurrentUser();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case POST_CREATE_ACTIVITY:
                if(resultCode == RESULT_OK) {
                    if(newFeedFragment != null) {
                        newFeedFragment.getLatestPost();
                    }

                }
                break;
            case LOGIN_ACTIVITY:
                if(resultCode == RESULT_CANCELED)
                    finish();
                else
                    recreate();

                break;
        }

    }

    boolean onProfileFrag = false;

    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            Log.i(Logs.POINT_OF_INTEREST,  "In tab listener");
            mViewPager.setCurrentItem(tab.getPosition());
            if(tab.getPosition() == 1 ) {
                onProfileFrag = true;
            } else {
                onProfileFrag = false;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    //assigns state listener to our Authenticator
    @Override
    protected void onStart() {
        super.onStart();
       mAuth.addAuthStateListener(mAuthListener);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());


        newFeedFragment = new NewFeedFragment();
        adapter.addFragment(newFeedFragment, getString(R.string.news_feed_fragment));

        profileFragment = new ProfileFragment();
        adapter.addFragment(profileFragment,getString(R.string.profile_fragment));
        viewPager.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Toast.makeText(this, "Works", Toast.LENGTH_LONG);
                Log.i(Logs.POINT_OF_INTEREST, "This actually works");
                return true;
            case R.id.log_out:

                user = mAuth.getCurrentUser();
                if(user != null)
                    mAuth.signOut();

                return true;
            case R.id.refresh:
                if (onProfileFrag) {
                    profileFragment.getAllProfilePost();
                } else {
                    newFeedFragment.getLatestPost();
                }


                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
            mViewPager.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}