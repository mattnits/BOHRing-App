package com.example.bohringapp.calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;

import com.example.bohringapp.R;

public class TodoActivity extends AppCompatActivity {

    public static int NEW_TODO_REQUEST = 2;
    public static int NEW_TODO_RESPONSE = 2;
    public static int EDIT_TODO_REQUEST = 1;
    public static int EDIT_TODO_RESPONSE = 1;
    public static int DELETE_TODO_RESPONSE = 3;
    private int USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        Bundle bundle = getIntent().getExtras();

        USER_ID = bundle.getInt("USER_ID");


        TodoFragment todoFragment = new TodoFragment();
        todoFragment.setArguments(getIntent().getExtras());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.todo_fragment_frame, todoFragment);
        ft.commit();
    }
}