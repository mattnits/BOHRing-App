package com.example.bohringapp.loginHome;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.bohringapp.R;

public class HelpFragment extends Fragment {

    public HelpFragment() {
        //Things and stuff
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("Test", "Creates View");
        return inflater.inflate(R.layout.activity_help, container, false);
    }

}
