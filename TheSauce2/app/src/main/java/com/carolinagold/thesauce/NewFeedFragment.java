package com.carolinagold.thesauce;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewFeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewFeedFragment extends Fragment implements MainActivity.NewsFeedFetchCallBack{

    RecyclerView recyclerView;



    private OnFragmentInteractionListener mListener;

    public NewFeedFragment() {
        // Required empty public constructor

    }

    public static NewFeedFragment newInstance() {
        NewFeedFragment fragment = new NewFeedFragment();
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
        View view = inflater.inflate(R.layout.fragment_new_feed, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.news_feed_fragment_recycler_view);

        List<Post> theList = new ArrayList<Post>();

        for (int i = 0; i < 10; i++) {
            theList.add(new Post("n " + i, " HelloWorld", " HelloWorld"," HelloWorld"," HelloWorld"," HelloWorld"));
        }

        NewsFeedAdaptor adapter = new NewsFeedAdaptor(getContext(), theList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        ready = true;
        return view;
    }

    boolean ready = false;

    public void updateFeed(List<Post> list) {
        ((NewsFeedAdaptor) recyclerView.getAdapter()).updateFeedList(list);
    }

    @Override
    public boolean ready() {return ready;}

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
