package com.example.bohringapp.courseContent;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.bohringapp.R;
import com.example.bohringapp.enrollment.CourseItem;
import com.example.bohringapp.enrollment.EnrollmentDatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class CourseFragment extends Fragment {
    WebView myWebView;
    String htmlData;
    SQLiteDatabase courseDatabase;
    LinearLayout ll;
    HorizontalScrollView sv;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);

        ArrayList<CourseItem> subheadings = new ArrayList<CourseItem>();
        //ll = view.findViewById(R.id.nav_bar);
        sv = view.findViewById(R.id.nav_bar);
        myWebView = view.findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(true);

        myWebView.loadDataWithBaseURL("file///android_asset/.",htmlData,"text/html","UTF-8",null);

        //Intent intent = getIntent();


        EnrollmentDatabaseHelper dbHelper = new EnrollmentDatabaseHelper(getContext());
        courseDatabase = dbHelper.getWritableDatabase();

        String[] columns = {EnrollmentDatabaseHelper.LINK_NAME_COLUMN, EnrollmentDatabaseHelper.LINK_COLUMN, EnrollmentDatabaseHelper.COURSE_COLUMN};
        Cursor cursor =  courseDatabase.query(EnrollmentDatabaseHelper.COURSE_LINK_TABLE, columns, null, null, null, null,null);
        cursor.moveToFirst();

        Bundle passedBund = getArguments();
        String courseCode;
        String course = passedBund.getString("class").toUpperCase();
        String courseLink = null;
        String linkName;
        String passedLink = passedBund.getString("link");
        CourseItem ci;

        while (!cursor.isAfterLast()){
            courseCode = cursor.getString(2).toUpperCase();
            linkName = cursor.getString(0);
            if (courseCode.equals(course) && linkName.equals(passedLink)){
                courseLink = cursor.getString(1);
                Log.i("CourseTest", courseLink);
            }
            else if (courseCode.equals(course)){
                ci = new CourseItem(cursor.getString(2), cursor.getString(1),cursor.getString(0));

                subheadings.add(ci);
            }
            cursor.moveToNext();
        }
        cursor.close();


        int height = getResources().getDisplayMetrics().heightPixels;
        int width = getResources().getDisplayMetrics().widthPixels;



        // Working version of that modifies CSS
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest (final WebView view, String url) {
                if (url.contains(".css")) {
                    return getCssWebResourceResponseFromAsset();
                } else {
                    return super.shouldInterceptRequest(view, url);
                }
            }


            // * Return WebResourceResponse with CSS markup from an asset (e.g. "assets/style.css").

            private WebResourceResponse getCssWebResourceResponseFromAsset() {
                try {
                    return getUtf8EncodedCssWebResourceResponse(getActivity().getAssets().open("custom.css"));
                } catch (IOException e) {
                    return null;
                }
            }

            private WebResourceResponse getUtf8EncodedCssWebResourceResponse(InputStream data) {
                return new WebResourceResponse("text/css", "UTF-8", data);
            }

        });

        myWebView.loadUrl(courseLink);

        ViewGroup.LayoutParams params = myWebView.getLayoutParams();
        params.height = height - 350;

        myWebView.setLayoutParams(params);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.nav_bar_linear);

        for (final CourseItem x : subheadings){
            Button subBtn = new Button(getContext());
            subBtn.setText(x.getURL_desc());
            subBtn.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Bundle bund = new Bundle();
                    bund.putString("class",x.getCode());
                    bund.putString("link",x.getURL_desc());
                    Intent intent = new Intent(getContext(), CourseActivity.class);
                    intent.putExtras(bund);
                    startActivity(intent);
                    getActivity().finish();

                }
            });

            ll.addView(subBtn);


        }


        // Inflate the layout for this fragment
        return view;
    }



}