package com.example.bohringapp.enrollment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bohringapp.loginHome.HelpActivity;
import com.example.bohringapp.R;
import com.example.bohringapp.calendar.CalendarActivity;
import com.example.bohringapp.courseContent.CourseActivity;
import com.example.bohringapp.courseContent.CourseContent;
import com.example.bohringapp.loginHome.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected static final String ACTIVITY_NAME = "EnrollmentActivity";

    private ArrayList<CourseItem> courseList;
    private CourseAdapter courseAdapter;
    SQLiteDatabase database;
    ProgressBar progressBar;
    Button createButton;
    EditText courseNameEditText;

    int USER_ID;
    int IS_ADMIN;

    int colour = -1;

    List<String> colourList;

    int[] colourValues = {
                        0xFF000000,
                        0xFF444444,
                        0xFF888888,
                        0xFFCCCCCC,
                        0xFFFF0000,
                        0xFF00FF00,
                        0xFF0000FF,
                        0xFFFFFF00,
                        0xFF00FFFF,
                        0xFFFF00FF
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        setUpNavigationDrawer();

        EnrollmentDatabaseHelper dbHelper = new EnrollmentDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        Bundle bundle = getIntent().getExtras();
        USER_ID = Integer.parseInt(bundle.getString("USER_ID"));
        Log.i("UserID", "" + USER_ID);


        String select = "SELECT is_admin FROM User WHERE userID = " + USER_ID + ";";
        Cursor cursor = database.rawQuery(select, null);

        if (cursor.getCount() < 1) {
            Log.i("Admin Check", "Error");
        }
        else {
            cursor.moveToFirst();

            IS_ADMIN = cursor.getInt(cursor.getColumnIndex(EnrollmentDatabaseHelper.IS_ADMIN_COLUMN));

            cursor.close();
        }

        Log.i("Admin Check", "Admin is "+ IS_ADMIN + " and user is " + USER_ID);


        progressBar = findViewById(R.id.progressBarEnrollment);
        progressBar.setVisibility(View.VISIBLE);

        createButton = findViewById(R.id.create_course);
        if (IS_ADMIN == 1){
            createButton.setVisibility(View.VISIBLE);
        }

        ListView courseListView = (ListView) findViewById(R.id.enrolled_classes);
        this.courseList = new ArrayList<CourseItem>();
        this.courseAdapter = new CourseAdapter(this);
        courseListView.setAdapter(this.courseAdapter);

        courseListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {
                final CourseItem course = EnrollmentActivity.this.courseList.get(pos);
                int index = ArrayUtils.indexOf(EnrollmentActivity.this.colourValues, course.getColour());

                AlertDialog.Builder builder = new AlertDialog.Builder(EnrollmentActivity.this);
                LayoutInflater inflater = EnrollmentActivity.this.getLayoutInflater();

                final View view = (IS_ADMIN == 1) ?
                        inflater.inflate(R.layout.course_info_admin_layout, null) :
                        inflater.inflate(R.layout.course_info_layout, null);

                builder.setTitle(course.getCode() + "-" + course.getName());
                builder.setView(view);

                final Spinner colourSpinner = (IS_ADMIN == 1) ?
                        (Spinner) view.findViewById(R.id.colourSpinner2):
                        (Spinner) view.findViewById(R.id.colourSpinner);

//                colourList = Arrays.asList(getResources().getStringArray(R.array.colours));

                ArrayAdapter<CharSequence> adapter =
                        ArrayAdapter.createFromResource(
                                EnrollmentActivity.this, R.array.colours, android.R.layout.simple_spinner_dropdown_item);
                colourSpinner.setAdapter(adapter);
                colourSpinner.setSelection(index);
                colourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView <?> adapterView, View view, int i, long l) {
                        EnrollmentActivity.this.colour = EnrollmentActivity.this.colourValues[i];
                    }

                    @Override
                    public void onNothingSelected(AdapterView <?> adapterView) { }
                });

                // Add the buttons
                builder.setPositiveButton(getString(R.string.OkButton), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (EnrollmentActivity.this.colour != -1) {
                            changeColor(pos, EnrollmentActivity.this.colour, course.getCode());
                            EnrollmentActivity.this.colour = -1;
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.CancelButton), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton(getString(R.string.DropCourseButton), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String code = course.getCode();
                        dropCourse(pos, course.getCode());
                        Toast toast = Toast.makeText(EnrollmentActivity.this , getString(R.string.DroppedMsg) + code , Toast.LENGTH_LONG); //this is the ListActivity
                        toast.show();
                    }
                });

                // Create the AlertDialog
                final AlertDialog dialog2 = builder.create();

                if (IS_ADMIN == 1) {
                    courseNameEditText = (EditText) view.findViewById(R.id.editTextChangeName);
                    createButton = (Button) view.findViewById(R.id.changeName_CourseInfo);
                    createButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String courseName = courseNameEditText.getText().toString();

                            if (courseName.matches("") ) {
                                Toast.makeText(EnrollmentActivity.this, getString(R.string.MissingCourseNameMsg) , Toast.LENGTH_SHORT).show();
                            }
                            else {
                                ContentValues cv = new ContentValues();
                                cv.put(EnrollmentDatabaseHelper.COURSE_DESCRIPTION_COLUMN, courseName);

                                String whereClause = EnrollmentDatabaseHelper.COURSE_CODE_COLUMN + "=?";
                                String[] whereArgs = new String[] { course.getCode() };

                                try
                                {
                                    database.update(EnrollmentDatabaseHelper.COURSE_TABLE, cv, whereClause, whereArgs);
                                }
                                catch (SQLException e)
                                {
                                    Toast toast = Toast.makeText(EnrollmentActivity.this , getString(R.string.UpdateFailure), Toast.LENGTH_SHORT); //this is the ListActivity
                                    toast.show();
                                }

                                dialog2.dismiss();

                                new EnrollmentQuery().execute("this will go to background");
                            }
                        }
                    });

                    Button deleteCourseButton = view.findViewById(R.id.deleteButton_CourseInfo);
                    deleteCourseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteCourse(course.getCode());
                            dialog2.dismiss();
                        }
                    });
                }

                dialog2.show();

                return true;
            }
        });

        // connect with db


        //query data
        new EnrollmentQuery().execute("this will go to background");

    }

    public void addCourseClicked(View view) {
        Intent addCourseIntent = new Intent(EnrollmentActivity.this, AddCourseActivity.class);
        addCourseIntent.putExtra("USER_ID", USER_ID);
        startActivityForResult(addCourseIntent, 1);
    }

    public void createCourseClicked(View view) {
        Intent intent = new Intent(EnrollmentActivity.this, CreateCourseActivity.class);
        startActivity(intent);
    }


    public void dropCourse (int pos, String courseCode) {
        String whereClause = EnrollmentDatabaseHelper._COURSE_CODE_COLUMN + "=? and " + EnrollmentDatabaseHelper._USER_ID_COLUMN + "=?";
        String[] whereArgs = new String[] { courseCode, Integer.toString(USER_ID) };

        try
        {
            database.delete(EnrollmentDatabaseHelper.ENROLLMENT_TABLE, whereClause, whereArgs);
        }
        catch (SQLException e)
        {
            Toast toast = Toast.makeText(EnrollmentActivity.this , getString(R.string.DeleteFailure), Toast.LENGTH_SHORT); //this is the ListActivity
            toast.show();
        }

        new EnrollmentQuery().execute("this will go to background");
    }

    public void deleteCourse (String courseCode) {
        String whereClause = EnrollmentDatabaseHelper._COURSE_CODE_COLUMN + "=?";
        String[] whereArgs = new String[] { courseCode };


        try
        {
            database.delete(EnrollmentDatabaseHelper.ENROLLMENT_TABLE, whereClause, whereArgs);
            whereClause = EnrollmentDatabaseHelper.COURSE_CODE_COLUMN + "=?";
            database.delete(EnrollmentDatabaseHelper.COURSE_TABLE, whereClause, whereArgs);
        }
        catch (SQLException e)
        {
            Toast toast = Toast.makeText(EnrollmentActivity.this , getString(R.string.DeleteFailure), Toast.LENGTH_SHORT); //this is the ListActivity
            toast.show();
        }

        new EnrollmentQuery().execute("this will go to background");
    }

    public void changeColor(int pos, int colour, String courseCode){
        ContentValues cv = new ContentValues();
        cv.put(EnrollmentDatabaseHelper.COLOR_COLUMN, colour);

        String whereClause = EnrollmentDatabaseHelper._COURSE_CODE_COLUMN + "=? and " + EnrollmentDatabaseHelper._USER_ID_COLUMN + "=?";
        String[] whereArgs = new String[] { courseCode, Integer.toString(USER_ID) };
        try
        {
            database.update(EnrollmentDatabaseHelper.ENROLLMENT_TABLE, cv, whereClause, whereArgs);
        }
        catch (SQLException e)
        {
            Toast toast = Toast.makeText(EnrollmentActivity.this , getString(R.string.UpdateFailure), Toast.LENGTH_SHORT); //this is the ListActivity
            toast.show();
        }


        new EnrollmentQuery().execute("this will go to background");
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        if (requestCode == 1 && responseCode == Activity.RESULT_OK) {
            String courseListJSON =  data.getStringExtra("courseList");
            Type listType = new TypeToken<ArrayList<CourseItem>>(){}.getType();
            ArrayList<CourseItem> addedCourses = new Gson().fromJson(courseListJSON, listType);

            String msg = getString(R.string.AddedMsg1) + addedCourses.size() + getString(R.string.AddedMsg2);

            for (CourseItem course : addedCourses) {
                ContentValues insertValues = new ContentValues();
                insertValues.put(EnrollmentDatabaseHelper._USER_ID_COLUMN, USER_ID);
                insertValues.put(EnrollmentDatabaseHelper._COURSE_CODE_COLUMN, course.getCode());
                insertValues.put(EnrollmentDatabaseHelper.COLOR_COLUMN, Color.BLUE);

                try
                {
                    database.insertOrThrow(EnrollmentDatabaseHelper.ENROLLMENT_TABLE, null, insertValues);
                    msg += "\t" + course.getCode() + " ,";
                }
                catch (SQLException e)
                {
                    Toast toast = Toast.makeText(EnrollmentActivity.this , getString(R.string.InsertFailure), Toast.LENGTH_SHORT); //this is the ListActivity
                    toast.show();
                }

            }

            new EnrollmentQuery().execute("this will go to background");
            msg = msg.substring(0, msg.length()-1);
            Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }


    private class CourseAdapter extends ArrayAdapter<CourseItem> {
        public CourseAdapter (Context ctx) {
            super(ctx, 0);
        }

        public int getCount() {
            return courseList.size();
        }

        public CourseItem getItem(int position) {
            return courseList.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = EnrollmentActivity.this.getLayoutInflater();
            View result = inflater.inflate(R.layout.activity_enrollment_course_item, null);

            TextView courseCodeName = (TextView) result.findViewById((R.id.course_code_name));
            String codeName = getItem(position).getCode() + " - " + getItem(position).getName();
            courseCodeName.setText(codeName);

            View courseColor = (View) result.findViewById((R.id.course_color));

            courseColor.setBackgroundColor(getItem(position).getColour());
            return result;
        }
    }

    private class EnrollmentQuery extends AsyncTask<String, Integer, String> {

        private String currentTemp;
        private String minTemp;
        protected String city;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            Log.i(ACTIVITY_NAME, "onPreExecute is called");
        }

        @Override
        protected String doInBackground(String... strings){

            try {
                publishProgress(25);
                Thread.sleep(50); // used to show progressbar update

                String select = "SELECT * FROM " + EnrollmentDatabaseHelper.ENROLLMENT_TABLE
                        + " INNER JOIN `Course` ON `Enrollment`.`_courseCode` = `Course`.`courseCode`"
                        + " WHERE `Enrollment`.`_userID` =" + USER_ID
                        + " ORDER BY `Enrollment`.`_courseCode`;";
                Cursor cursor = database.rawQuery(select, null);
                cursor.moveToFirst();
                courseList.clear();

                while (!cursor.isAfterLast()) {
                    String courseCode = cursor.getString( cursor.getColumnIndex( EnrollmentDatabaseHelper.COURSE_CODE_COLUMN) );
                    String courseDescription = cursor.getString( cursor.getColumnIndex( EnrollmentDatabaseHelper.COURSE_DESCRIPTION_COLUMN) );
                    int courseColor = cursor.getInt( cursor.getColumnIndex( EnrollmentDatabaseHelper.COLOR_COLUMN) );
                    CourseItem item = new CourseItem(courseCode, courseDescription, courseColor);
                    courseList.add(item);
                    cursor.moveToNext();
                }
                cursor.close();

                publishProgress(50);
                Thread.sleep(50); // used to show progressbar update

                publishProgress(75);
                Thread.sleep(50); // used to show progressbar update

                publishProgress(100);
                Thread.sleep(50); // used to show progressbar update


            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String a) {
            Log.i ("post", a + "------------------");
            progressBar.setVisibility(View.INVISIBLE);
            EnrollmentActivity.this.courseAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.i ("values",  values[0] +"-------------------------------") ;
            progressBar.setProgress(values[0]);
        }

    }

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView nView;
    int pagecnt = 0;
    public SQLiteDatabase db;
    public ArrayList<String> links = new ArrayList<String>();
    public ArrayList<String> courseExtras = new ArrayList<String>();
    String selectedCourse = "";

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuCalendar) {
            Log.i("test", "testing");
            Intent intent = new Intent(EnrollmentActivity.this, CalendarActivity.class);
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
            db = dbHelper.getWritableDatabase();

            String select = "SELECT * FROM " + EnrollmentDatabaseHelper.ENROLLMENT_TABLE
                    + " INNER JOIN `Course` ON `Enrollment`.`_courseCode` = `Course`.`courseCode`"
                    + " WHERE `Enrollment`.`_userID` =" + USER_ID
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
                            Intent intent = new Intent(EnrollmentActivity.this, CalendarActivity.class);
                            intent.putExtra("USER_ID", Integer.toString(USER_ID));
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                            finish();
                        }
                        else if (menuItem.getItemId() == 2131362061) {
                            Log.i("DrawerClick", "Clicked Course Content");
                            Intent intent = new Intent(EnrollmentActivity.this, CourseContent.class);
                            intent.putExtra("USER_ID", Integer.toString(USER_ID));
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                            finish();
                        }
                        else if (menuItem.getItemId() == 2131362063) {
                            Log.i("DrawerClick", "Clicked Enrollment");
                            Intent intent = new Intent(EnrollmentActivity.this, EnrollmentActivity.class);
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
                            Intent intent = new Intent(EnrollmentActivity.this, HelpActivity.class);
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

                            Intent intent = new Intent(EnrollmentActivity.this, CourseActivity.class);
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

                            Intent intent = new Intent(EnrollmentActivity.this, LoginActivity.class);
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
                            Intent intent = new Intent(EnrollmentActivity.this, CourseActivity.class);
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