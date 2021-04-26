package com.example.bohringapp.courseContent;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.bohringapp.R;
import com.example.bohringapp.calendar.TodoFragment;
import com.example.bohringapp.enrollment.EnrollmentDatabaseHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


// Levi's suggestion is add a seocnd CSS page which takes precedent over original CSS
public class CourseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        CourseFragment courseFragment = new CourseFragment();
        Intent intent = getIntent();
        String course = intent.getStringExtra("class");
        String url = intent.getStringExtra("link");
        Bundle bundle = new Bundle();
        bundle.putString("class", course);
        bundle.putString("link", url);
        courseFragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.Course_Fragment, courseFragment);
        ft.commit();

    }

}