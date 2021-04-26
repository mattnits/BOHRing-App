package com.example.bohringapp.courseContent;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bohringapp.loginHome.HelpActivity;
import com.example.bohringapp.R;
import com.example.bohringapp.calendar.CalendarActivity;
import com.example.bohringapp.enrollment.CourseItem;
import com.example.bohringapp.enrollment.EnrollmentActivity;
import com.example.bohringapp.enrollment.EnrollmentDatabaseHelper;
import com.example.bohringapp.loginHome.LoginActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class CourseContent extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ListView lv;
    EditText searchTxt;
    ProgressBar pb;
    String ACTIVITY_NAME = "NEW COURSE CONTENT";
    String cur_text;
    String snackbar;
    String CourseCode;
    ArrayList<CourseItem> COURSE_LIST = new ArrayList<CourseItem>();
    public static SQLiteDatabase db;

    //I dont think this is supposed to be course item... will keep it for not but might just want the code
    public static ArrayList<CourseItem> courseList; //changed from private no static
    public CourseAdapter courseAdapter; // I made courseAdapater public so i wouldn't have to copy the object over but then i had to make it static
    // when i got it to work with my class cuz it was static it didn't work on the inner class so i gave up
    public ArrayList<CourseItem>  URLList = new ArrayList<CourseItem>();


    int USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_content);

        setUpNavigationDrawer();

        Bundle bundle = getIntent().getExtras();
        USER_ID = Integer.parseInt(bundle.getString("USER_ID"));

        pb = findViewById(R.id.progBar);
        pb.setVisibility(View.VISIBLE);

        lv = (ListView) findViewById(R.id.enrolled_courses);
        searchTxt = (EditText) findViewById(R.id.search_bar);

        this.courseList = new ArrayList<CourseItem>();
        //need a second list to store values of course names (which will never be modified)
        this.courseAdapter = new CourseAdapter(this);
        lv.setAdapter(this.courseAdapter);

        searchTxt.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                cur_text = s.toString().toLowerCase();
                if (cur_text.isEmpty()){
                    for (CourseItem ci : COURSE_LIST){
                        if (!courseList.contains(ci)){
                            courseList.add(ci);;
                        }
                    }
                    courseAdapter.notifyDataSetChanged();
                }else{
                    searchClasses(cur_text);
                }

            }
        });

        EnrollmentDatabaseHelper dbHelper = new EnrollmentDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        new EnrollmentQuery().execute("this will go to background");

    }

    private void searchClasses(String cur_text) {
        //this might break the code if its a pass by ref but the idea is to refill the options every time the user edits the text
        cur_text = cur_text.toUpperCase();
        for (CourseItem c : COURSE_LIST){
            if (!c.getCode().contains(cur_text)){
                courseList.remove(c); //suposed to be removing from the array that gets passed to adapter that gets set for the list view
                //might need to modify TODO array adapter to take an array and populate the list view with the array vals
                //curently am removing from courseList which courseAdapter has a copy of but i dont think it's passed by reference
            }else{
                if (courseList.contains(c) == false){
                    courseList.add(c);
                }
            }
        }
        courseAdapter.notifyDataSetChanged();
    }


    private class CourseAdapter extends ArrayAdapter<CourseItem>{
        String courseCode;
        public CourseAdapter(Context ctx){
            super(ctx,0);
        }
        public int getCount() {
            return courseList.size();
        }

        public CourseItem getItem(int position) {
            return courseList.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = CourseContent.this.getLayoutInflater();
            View result = inflater.inflate(R.layout.button,null);
            Button courseBtn = (Button) result.findViewById(R.id.courseBtn);
            Button editBtn = result.findViewById(R.id.editButton);
            if (USER_ID == 0){
                editBtn.setVisibility(convertView.GONE);
            }else{
                editBtn.setVisibility(convertView.VISIBLE);
            }
            //TextView courseTxt = (TextView) result.findViewById(R.id.courseTxt);
            editBtn.setTag(position);
            String codeName = getItem(position).getCode() + " - " + getItem(position).getName();
            courseBtn.setText(codeName);

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    CourseCode = getItem(position).getCode().toLowerCase();
                    AlertDialog.Builder customBuilder = new AlertDialog.Builder(CourseContent.this);
                    customBuilder.setTitle(R.string.edit_course);
                    LayoutInflater inflater = CourseContent.this.getLayoutInflater();
                    final View view = inflater.inflate(R.layout.custom_dialog, null);

                    customBuilder.setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText text= view.findViewById(R.id.NewLink);
                            String courseLink = text.getText().toString();
                            snackbar = courseLink;
                            String home = "home";

                            String SQLupdateLink = "UPDATE " + EnrollmentDatabaseHelper.COURSE_LINK_TABLE
                                    + " SET " + EnrollmentDatabaseHelper.LINK_COLUMN + " ='" +courseLink+"'"
                                    + " WHERE " + EnrollmentDatabaseHelper.COURSE_COLUMN + " = '"+CourseCode+"'"
                                    + " AND " + EnrollmentDatabaseHelper.LINK_NAME_COLUMN + " ='" +home+"';";

                            db.execSQL(SQLupdateLink);

                            Snackbar.make(findViewById(R.id.editButton), R.string.course_link + snackbar , Snackbar.LENGTH_LONG).show();


                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Snackbar.make(findViewById(R.id.editButton), R.string.cancelled , Snackbar.LENGTH_LONG).show();
                        }
                    });

                    AlertDialog dialog2 = customBuilder.create();
                    dialog2.show();
                }
            });


            //The play might be to set an onclick function in the button and then when the buttons pressed goes to that function where it extracts the text from the button
            courseBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    courseCode = b.getText().toString().substring(0,5);
                    Bundle bund = new Bundle();
                    bund.putString("class",courseCode);
                    bund.putString("link","home");

                    Intent intent = new Intent(CourseContent.this, CourseActivity.class);
                    intent.putExtras(bund);
                    startActivity(intent);
                }
            });
            return result;

        }

    }


    public class EnrollmentQuery extends AsyncTask<String,Integer,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            Log.i(ACTIVITY_NAME, "onPreExecute is called");
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                publishProgress(25);
                Thread.sleep(250); // used to show progressbar update

                String select = "SELECT * FROM " + EnrollmentDatabaseHelper.ENROLLMENT_TABLE
                        + " INNER JOIN `Course` ON `Enrollment`.`_courseCode` = `Course`.`courseCode`"
                        + " WHERE `Enrollment`.`_userID` =" + USER_ID
                        + " ORDER BY `Enrollment`.`_courseCode`;";
                Cursor cursor = db.rawQuery(select, null);
                cursor.moveToFirst();
                courseList.clear();

                //Populates course List with courses student is enrolled in
                while (!cursor.isAfterLast()) {
                    String courseCode = cursor.getString(cursor.getColumnIndex(EnrollmentDatabaseHelper.COURSE_CODE_COLUMN));
                    String courseDescription = cursor.getString(cursor.getColumnIndex(EnrollmentDatabaseHelper.COURSE_DESCRIPTION_COLUMN));
                    CourseItem item = new CourseItem(courseCode, courseDescription, 0);
                    courseList.add(item);
                    COURSE_LIST.add(item);
                    cursor.moveToNext();
                }
                cursor.close();

                publishProgress(50);
                Thread.sleep(100); // used to show progressbar update

                publishProgress(75);
                Thread.sleep(100); // used to show progressbar update

                publishProgress(100);
                Thread.sleep(100); // used to show progressbar update

                String URL_select = "SELECT * FROM " + EnrollmentDatabaseHelper.COURSE_LINK_TABLE + " INNER JOIN `Enrollment` ON " +
                        "`CourseLinksTable`.`courseCode` = `Enrollment`.`_courseCode`;";
                Cursor urlCursor = db.rawQuery(URL_select,null);
                urlCursor.moveToFirst();
                URLList.clear();
                //make a map that holds the course code name and object
                while (!urlCursor.isAfterLast()) {
                    String courseCode = urlCursor.getString(urlCursor.getColumnIndex(EnrollmentDatabaseHelper.COURSE_CODE_COLUMN));
                    String URL = urlCursor.getString(urlCursor.getColumnIndex(EnrollmentDatabaseHelper.LINK_NAME_COLUMN));
                    String URL_desc = urlCursor.getString(urlCursor.getColumnIndex(EnrollmentDatabaseHelper.LINK_COLUMN));
                    CourseItem item = new CourseItem(courseCode, URL_desc, URL);
                    URLList.add(item);

                    urlCursor.moveToNext();
                }
                urlCursor.close();




            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }

            @Override
        protected void onPostExecute(String s) {
            Log.i ("post", s + "------------------");
            pb.setVisibility(View.INVISIBLE);
            CourseContent.this.courseAdapter.notifyDataSetChanged();
            Toast toast;
            if (COURSE_LIST.size() != 0){
                toast = Toast.makeText(CourseContent.this , R.string.success , Toast.LENGTH_SHORT); //this is the ListActivity
            }else{
                toast = Toast.makeText(CourseContent.this,R.string.no_courses, Toast.LENGTH_SHORT);
            }
            toast.show();
            }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.i ("values",  values[0] +"-------------------------------") ;
            pb.setProgress(values[0]);
        }
    }

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView nView;
    int pagecnt = 0;
    public SQLiteDatabase db1;
    public ArrayList<String> links = new ArrayList<String>();
    public ArrayList<String> courseExtras = new ArrayList<String>();
    String selectedCourse = "";

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuCalendar) {
            Log.i("test", "testing");
            Intent intent = new Intent(CourseContent.this, CalendarActivity.class);
            startActivity(intent);
        }
        return true;
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
            db1 = dbHelper.getWritableDatabase();

            String select = "SELECT * FROM " + EnrollmentDatabaseHelper.ENROLLMENT_TABLE
                    + " INNER JOIN `Course` ON `Enrollment`.`_courseCode` = `Course`.`courseCode`"
                    + " WHERE `Enrollment`.`_userID` =" + USER_ID
                    + " ORDER BY `Enrollment`.`_courseCode`;";
            Cursor cursor = db1.rawQuery(select, null);

            if (cursor.getCount() < 1) {
                sub.add(0,0,0, R.string.no_c_available);
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
            db1 = dbHelper.getWritableDatabase();

            String select = "SELECT * FROM CourseLinksTable WHERE courseCode = '" + selectedCourse.toLowerCase() + "';";
            Cursor cursor = db1.rawQuery(select, null);

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
                            Intent intent = new Intent(CourseContent.this, CalendarActivity.class);
                            intent.putExtra("USER_ID", Integer.toString(USER_ID));
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                            finish();
                        }
                        else if (menuItem.getItemId() == 2131362061) {
                            Log.i("DrawerClick", "Clicked Course Content");
                            Intent intent = new Intent(CourseContent.this, CourseContent.class);
                            intent.putExtra("USER_ID", Integer.toString(USER_ID));
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                            finish();
                        }
                        else if (menuItem.getItemId() == 2131362063) {
                            Log.i("DrawerClick", "Clicked Enrollment");
                            Intent intent = new Intent(CourseContent.this, EnrollmentActivity.class);
                            intent.putExtra("USER_ID", Integer.toString(USER_ID));
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                            finish();
                        }
                        else if (menuItem.getItemId() == 2131362077) {
                            Log.i("DrawerClick", "More");
                            nView.getMenu().clear();
                            nView.inflateMenu(R.menu.course_list_menu);
                            onPrepareOptionsMenu(nView.getMenu());
                            pagecnt++;
                        }
                        else if (menuItem.getItemId() == 2131362064) {
                            Intent intent = new Intent(CourseContent.this, HelpActivity.class);
                            intent.putExtra("HelpUsername", Integer.toString(USER_ID));
                            startActivity(intent);
                            Log.i("DrawerClick", "Help");
                            finish();
                        }
                        else if (menuItem.toString().charAt(0) == 'C' && menuItem.toString().charAt(1) == 'P' && menuItem.toString().length() == 5) {
                            Log.i("DrawerClick", menuItem.toString());
                            Bundle bund = new Bundle();
                            bund.putString("class",menuItem.toString());
                            bund.putString("link","home");

                            Intent intent = new Intent(CourseContent.this, CourseActivity.class);
                            intent.putExtras(bund);
                            startActivity(intent);
                            finish();
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

                            Intent intent = new Intent(CourseContent.this, LoginActivity.class);
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
                            Intent intent = new Intent(CourseContent.this, CourseActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                            Log.i("DrawerClick", menuItem.toString());
                        }

                        //Log.i("MenuOnClick", menuItem.toString());

                        return true;
                    }
                }
        );

    }
}