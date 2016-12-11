package com.carolinagold.thesauce;

import android.content.Context;
import android.media.ImageWriter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;



public class ProfileFragment extends Fragment {

    RecyclerView recyclerView;

    ImageView imageView;
    TextView textView;

    FirebaseUser user;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor

    }


    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.profile_fragment_profile_image);
        textView = (TextView) rootView.findViewById(R.id.profile_fragment_profile_name);
        setUpTopView();

        recyclerView = (RecyclerView) rootView.findViewById(R.id.profile_fragment_recycler_grid);

        List<Post> theList = new ArrayList<Post>();

        for (int i = 0; i < 10; i++) {
            theList.add(new Post("n " + i, " HelloWorld", " HelloWorld"," HelloWorld"," HelloWorld"," HelloWorld"));
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3, GridLayout.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        ProfileAdapter adapter = new ProfileAdapter(getContext(), theList);
        recyclerView.setAdapter(adapter);

        user = ((MainActivity) getActivity()).user;
        getAllProfilePost();

        return rootView;
    }


    private void getAllProfilePost() {
        FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
        DatabaseReference myRef = dbRef.getReference("Post").child(user.getUid());

        ((MainActivity) getActivity()).showProgress(true);
        final ProfileAdapter adaptor = (ProfileAdapter) recyclerView.getAdapter();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Post> listOfPosts = new ArrayList<Post>();

                for (DataSnapshot postByUser : dataSnapshot.getChildren()) {
                    System.out.println(postByUser);

                    listOfPosts.add(postByUser.getValue(Post.class));

                }
                ((MainActivity) getActivity()).showProgress(false);
                adaptor.updateProfileGallery(listOfPosts);
                adaptor.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(Logs.POINT_OF_INTEREST, "Failed retrieving data from firebase from method: getLatestPost");
                ((MainActivity) getActivity()).showProgress(false);
            }
        });
    }

    private void setUpTopView() {
        textView.setText(user.getDisplayName());
        //Picasso.with(getActivity()).load(user.getPhotoUrl())
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
