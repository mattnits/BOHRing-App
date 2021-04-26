package com.example.bohringapp.loginHome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.bohringapp.R;
import com.example.bohringapp.courseContent.CourseActivity;
import com.example.bohringapp.enrollment.EnrollmentDatabaseHelper;
import com.google.android.material.navigation.NavigationView;

import android.widget.Toast;

import com.example.bohringapp.courseContent.CourseContent;
import com.example.bohringapp.enrollment.EnrollmentActivity;
import com.example.bohringapp.calendar.CalendarActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<String> list = new ArrayList<String>();
    HomeListViewAdapter homeAdapter;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView nView;
    int pagecnt = 0;
    String username;
    public SQLiteDatabase db;
    public ArrayList<String> links = new ArrayList<String>();
    public ArrayList<String> courseExtras = new ArrayList<String>();
    String selectedCourse = "";

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuCalendar) {
            Log.i("test", "testing");
            Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private class HomeListViewAdapter extends ArrayAdapter<String> {


        public HomeListViewAdapter(Context context) {
            super(context,0);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int pos) {
            return list.get(pos);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = HomeActivity.this.getLayoutInflater();
            View result = null;

            result = inflater.inflate(R.layout.home_listview, null);

            TextView tView = (TextView)findViewById(R.id.homeText);
            tView.setText("test");

            Button btnTest = (Button)findViewById(R.id.homeCalendar);
            btnTest.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //do something
                    Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
                    startActivity(intent);
                }
            });

            return result;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /*ListView view = (ListView)findViewById(R.id.homeListView);
        homeAdapter = new HomeListViewAdapter(this);
        view.setAdapter (homeAdapter);
        homeAdapter.notifyDataSetChanged();*/

        Log.i("Home", "Created Home");

        Bundle bundle = getIntent().getExtras();
        username = bundle.getString("Username");
        Toast toast = Toast.makeText(getApplicationContext(), "Welcome " + username, Toast.LENGTH_LONG);
        toast.show();

        setUpNavigationDrawer();
    }

    public void clickCalendar(View view) {
        Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
        intent.putExtra("USER_ID", username);
        startActivity(intent);
    }

    public void clickCourseContent(View view) {
        //Intent intent = new Intent(HomeActivity.this, CourseContentActivity.class);
        Intent intent = new Intent(HomeActivity.this, CourseContent.class);
        intent.putExtra("USER_ID", username);
        startActivity(intent);
    }

    public void clickEnrollment(View view) {
        Intent intent = new Intent(HomeActivity.this, EnrollmentActivity.class);
        intent.putExtra("USER_ID", username);
        startActivity(intent);
    }

    public void clickHelp(View view) {
        Intent intent = new Intent(HomeActivity.this, HelpActivity.class);
        intent.putExtra("HelpUsername", username);
        startActivity(intent);
    }

    public void clickLogout(View view) {
        SharedPreferences sharedPref = sharedPref = getApplicationContext()
                .getSharedPreferences("BohringApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("User");
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.CoursesMenuItem);
        MenuItem inCourse = menu.findItem(R.id.InCourseMenu);
        if (item == null) {
            Log.i("NullTest", "Item is null");
        }
        else {
            Log.i("NullTest", "Item is something");
            SubMenu sub = item.getSubMenu();
            sub.clear();

            EnrollmentDatabaseHelper dbHelper = new EnrollmentDatabaseHelper(this);
            db = dbHelper.getWritableDatabase();

            String select = "SELECT * FROM " + EnrollmentDatabaseHelper.ENROLLMENT_TABLE
                    + " INNER JOIN `Course` ON `Enrollment`.`_courseCode` = `Course`.`courseCode`"
                    + " WHERE `Enrollment`.`_userID` =" + username
                    + " ORDER BY `Enrollment`.`_courseCode`;";
            Cursor cursor = db.rawQuery(select, null);

            if (cursor.getCount() < 1) {
                sub.add(0,0,0, "No Courses Available");
            }

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String courseCode = cursor.getString(cursor.getColumnIndex(EnrollmentDatabaseHelper.COURSE_CODE_COLUMN));
                sub.add(0,0,0, courseCode).setIcon(R.drawable.square);
                sub.add(0,0,0, courseCode+ " Content").setIcon(R.drawable.arrow);
                cursor.moveToNext();
            }
            cursor.close();

        }

        if (inCourse != null) {
            //Log.i("Jake Menu", "Item is something");
            Log.i("Course", selectedCourse);
            SubMenu sub = inCourse.getSubMenu();
            sub.clear();
            links.clear();

            EnrollmentDatabaseHelper dbHelper = new EnrollmentDatabaseHelper(this);
            db = dbHelper.getWritableDatabase();

            String select = "SELECT * FROM CourseLinksTable WHERE courseCode = '" + selectedCourse.toLowerCase() + "';";
            Cursor cursor = db.rawQuery(select, null);

            if (cursor.getCount() < 1) {
                sub.add(0,0,0, "No Content Available");
            }

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String courseCode = cursor.getString(cursor.getColumnIndex(EnrollmentDatabaseHelper.LINK_NAME_COLUMN));
                String courseLink = cursor.getString(cursor.getColumnIndex(EnrollmentDatabaseHelper.LINK_COLUMN));
                sub.add(0,0,0, courseCode).setIcon(R.drawable.square);
                links.add(courseLink);
                courseExtras.add(courseCode);
                cursor.moveToNext();
            }
            cursor.close();
        }


        super.onPrepareOptionsMenu(menu);
        return true;
    }


    // Make sure to include this
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START) && pagecnt == 1) {
            nView.getMenu().clear();
            nView.inflateMenu(R.menu.drawer_menu);
            pagecnt--;
        }
        else if  (drawerLayout.isDrawerOpen(GravityCompat.START) && pagecnt == 0) {
            drawerLayout.closeDrawers();
        }
        else if (drawerLayout.isDrawerOpen(GravityCompat.START) && pagecnt == 2) {
            nView.getMenu().clear();
            nView.inflateMenu(R.menu.course_list_menu);
            onPrepareOptionsMenu(nView.getMenu());
            pagecnt--;
        }
        else {
            super.onBackPressed();
        }

    }

    // This function sets up the drawer
    private void setUpNavigationDrawer() {

        drawerLayout = findViewById(R.id.dl_drawer_layout);
        //toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,  R.string.open, R.string.close){


            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                nView.getMenu().clear();
                nView.inflateMenu(R.menu.drawer_menu);
                pagecnt = 0;
            }


            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        //actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        nView = findViewById(R.id.nv_navView);



        nView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {

                    if (menuItem.getItemId() == 2131362060) {
                        Log.i("DrawerClick", "Clicked Calendar");
                        Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
                        intent.putExtra("USER_ID", username);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                    }
                    else if (menuItem.getItemId() == 2131362061) {
                        Log.i("DrawerClick", "Clicked Course Content");
                        Intent intent = new Intent(HomeActivity.this, CourseContent.class);
                        intent.putExtra("USER_ID", username);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                    }
                    else if (menuItem.getItemId() == 2131362063) {
                        Log.i("DrawerClick", "Clicked Enrollment");
                        Intent intent = new Intent(HomeActivity.this, EnrollmentActivity.class);
                        intent.putExtra("USER_ID", username);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                    }
                    else if (menuItem.getItemId() == 2131362077) {
                        Log.i("DrawerClick", "More");
                        nView.getMenu().clear();
                        nView.inflateMenu(R.menu.course_list_menu);
                        onPrepareOptionsMenu(nView.getMenu());
                        pagecnt++;
                    }
                    else if (menuItem.getItemId() == 2131362064) {
                        Intent intent = new Intent(HomeActivity.this, HelpActivity.class);
                        intent.putExtra("HelpUsername", username);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                        Log.i("DrawerClick", "Help");
                        drawerLayout.closeDrawers();
                    }
                    else if (menuItem.toString().charAt(0) == 'C' && menuItem.toString().charAt(1) == 'P' && menuItem.toString().length() == 5) {
                        Log.i("DrawerClick", menuItem.toString());
                        Bundle bund = new Bundle();
                        bund.putString("class",menuItem.toString());
                        bund.putString("link","home");

                        Intent intent = new Intent(HomeActivity.this, CourseActivity.class);
                        intent.putExtras(bund);
                        startActivity(intent);
                    }
                    else if (menuItem.toString().charAt(0) == 'C' && menuItem.toString().charAt(1) == 'P' && menuItem.toString().length() > 5) {
                        Log.i("DrawerClick", "Course Extras");
                        selectedCourse = menuItem.toString().substring(0, 5);
                        nView.getMenu().clear();
                        nView.inflateMenu(R.menu.course_menu);
                        onPrepareOptionsMenu(nView.getMenu());

                        pagecnt++;
                    }
                    else if (menuItem.getItemId() == 2131362065) {
                        SharedPreferences sharedPref = sharedPref = getApplicationContext()
                                .getSharedPreferences("BohringApp", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.remove("User");
                        editor.apply();

                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finishAffinity();
                        Log.i("DrawerClick", "Help");
                    }
                    else {

                        Bundle bundle = new Bundle();
                        bundle.putString("class",selectedCourse);
                        for (int i=0; i < courseExtras.size(); i++) {
                            if (menuItem.toString().equals(courseExtras.get(i))) {
                                bundle.putString("link",menuItem.toString());
                            }
                        }
                        Intent intent = new Intent(HomeActivity.this, CourseActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        Log.i("DrawerClick", menuItem.toString());
                    }


                    //

                    return true;
                }
            }
        );

    }

}