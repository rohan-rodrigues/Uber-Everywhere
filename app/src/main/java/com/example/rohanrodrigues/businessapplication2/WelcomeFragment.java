package com.example.rohanrodrigues.businessapplication2;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by rohanrodrigues on 1/21/17.
 */

public class WelcomeFragment extends Fragment {
    View rootview;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview =  inflater.inflate(R.layout.welcome_fragment, container, false);
        return rootview;
    }
}
