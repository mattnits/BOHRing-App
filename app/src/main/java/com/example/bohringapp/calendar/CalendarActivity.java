package com.example.bohringapp.calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bohringapp.loginHome.HelpActivity;
import com.example.bohringapp.R;
import com.example.bohringapp.courseContent.CourseActivity;
import com.example.bohringapp.courseContent.CourseContent;
import com.example.bohringapp.enrollment.EnrollmentActivity;
import com.example.bohringapp.enrollment.EnrollmentDatabaseHelper;
import com.example.bohringapp.loginHome.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

public class CalendarActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private int USER_ID;
    private ProgressBar progressBar;
    private ListView todoListView;
    private ArrayList<TodoItem> todoList = new ArrayList();
    private TodoAdapter todoAdapter;
    private SQLiteDatabase db;
    private ViewGroup parentViewGroup;
    private boolean showCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        setUpNavigationDrawer();

        Bundle bundle = getIntent().getExtras();
        USER_ID = Integer.parseInt(bundle.getString("USER_ID"));

        this.db = new EnrollmentDatabaseHelper(this).getWritableDatabase();
        this.progressBar = findViewById(R.id.calendar_progress);
        this.progressBar = findViewById(R.id.calendar_progress);
        this.todoListView = findViewById(R.id.master_todo_list);
        this.showCompleted = ((CheckBox) findViewById(R.id.show_completed)).isChecked();

        this.todoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                 LayoutInflater inflater = CalendarActivity.this.getLayoutInflater();
                 final View view = inflater.inflate(R.layout.todo_details, null);

                 TodoItem todoItem = todoList.get(position);
                 ((TextView) view.findViewById(R.id.todo_detail_title))
                         .setText(todoItem.getTitle());
                 ((TextView) view.findViewById(R.id.todo_detail_description))
                         .setText(todoItem.getDescription());
                 ((TextView) view.findViewById(R.id.todo_detail_duedate))
                         .setText(todoItem.getPrettyDueDate());

                 new AlertDialog.Builder(CalendarActivity.this)
                         .setView(view)
                         .create()
                         .show();
             }
         });

        this.todoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Bundle bundle = new Bundle();

                bundle.putParcelable("todoItem", todoAdapter.getItem(pos));
                bundle.putInt("requestCode", TodoActivity.EDIT_TODO_REQUEST);
                bundle.putInt("position", pos);
                bundle.putInt("USER_ID", USER_ID);
                Intent intent = new Intent(CalendarActivity.this, TodoActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, TodoActivity.EDIT_TODO_REQUEST);
                return true;
            }
        });
        this.todoAdapter = new TodoAdapter(this);
        this.todoListView.setAdapter(this.todoAdapter);
        this.parentViewGroup = ((ViewGroup) todoListView.getParent());
        new TodoQuery().execute();
    }

    public void addTodoItemClicked(View view) {
        if (CalendarDatabaseHelper.getNumEnrolledCourses(USER_ID, db) > 0) {
            Intent intent = new Intent(CalendarActivity.this, TodoActivity.class);
            intent.putExtra("requestCode", TodoActivity.NEW_TODO_REQUEST);
            intent.putExtra("USER_ID", USER_ID);
            startActivityForResult(intent, TodoActivity.NEW_TODO_REQUEST);
        } else {
            Toast no_courses_toast = new Toast(this);
            no_courses_toast.setText(R.string.todo_no_courses);
            no_courses_toast.setDuration(Toast.LENGTH_SHORT);
            no_courses_toast.show();
        }
    }

    public void showCompletedClicked(View view) {
        this.showCompleted = ((CheckBox) view).isChecked();
        new TodoQuery().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);

        if (responseCode == TodoActivity.EDIT_TODO_RESPONSE) {
            TodoItem todoItem = data.getExtras().getParcelable("todoItem");
            CalendarDatabaseHelper.replaceTodoItem(todoItem, USER_ID, db);
        } else if (responseCode == TodoActivity.NEW_TODO_RESPONSE) {
            TodoItem todoItem = data.getExtras().getParcelable("todoItem");
            CalendarDatabaseHelper.insertTodoItem(todoItem, USER_ID, db);
        } else if (responseCode == TodoActivity.DELETE_TODO_RESPONSE) {
            TodoItem todoItem = data.getExtras().getParcelable("todoItem");
            CalendarDatabaseHelper.deleteTodoItem(todoItem, USER_ID, db);
        }
        new TodoQuery().execute();
    }

    private class TodoAdapter extends ArrayAdapter<TodoItem> {
        public TodoAdapter (Context ctx) {
            super(ctx, 0);
        }

        public int getCount() {
            return todoList.size();
        }

        public TodoItem getItem(int position) {
            return todoList.get(position);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = CalendarActivity.this.getLayoutInflater();
            View result = inflater.inflate(R.layout.activity_calendar_todo_item, null);

            TodoItem todoItem = getItem(position);
            TextView courseCode = result.findViewById((R.id.todo_list_courseCode));
            courseCode.setText(CalendarActivity.this.getResources().getString(R.string.course) + " " + todoItem.getCourseCode());

            TextView title = result.findViewById((R.id.todo_list_title));
            title.setText(todoItem.getTitle());

            TextView dueDate = result.findViewById((R.id.todo_list_dueDate));
            dueDate.setText(CalendarActivity.this.getResources().getString(R.string.due_date) + " " + todoItem.getPrettyDueDate());


            CheckBox completed = result.findViewById(R.id.todo_list_completed);
            completed.setChecked(todoItem.getCompleted());
            completed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getItem(position).setCompleted(!getItem(position).getCompleted());
                    Toast.makeText(getBaseContext(), R.string.markedAsCompleted, Toast.LENGTH_SHORT)
                            .show();
                    CalendarDatabaseHelper.replaceTodoItem(getItem(position), USER_ID, db);
                    new TodoQuery().execute();
                }
            });

            result.setBackgroundColor(getItem(position).getColour());
            return result;
        }
    }

    private class TodoQuery extends AsyncTask<String, Integer, String> {
        private ArrayList<TodoItem> newTodoList;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (parentViewGroup.findViewById(R.id.calendar_progress) == null) {
                parentViewGroup.addView(progressBar);
            }
            newTodoList = new ArrayList();
        }

        @Override
        protected String doInBackground(String... strings) {
            long totalTodos = DatabaseUtils.queryNumEntries(db, CalendarDatabaseHelper.TODO_TABLE);
            long curTodos = 0;
            publishProgress(0);
            Cursor cursor = db.rawQuery(CalendarDatabaseHelper.SELECT_TODO_ITEMS,
                    new String[] {String.valueOf(USER_ID)});
            cursor.moveToFirst();
            Gson gson = new Gson();
            while (!cursor.isAfterLast()) {
                TodoItem todoItem = gson.fromJson(cursor.getString(cursor.getColumnIndex(
                        CalendarDatabaseHelper.TODO_VAL)), TodoItem.class);
                todoItem.setDbKey(cursor.getInt(cursor.getColumnIndex(
                        CalendarDatabaseHelper.TODO_KEY)));
                if (showCompleted || !todoItem.getCompleted()) {
                    newTodoList.add(todoItem);
                }
                cursor.moveToNext();
                publishProgress((int) (curTodos / totalTodos));
            }
            publishProgress(100);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            todoList = newTodoList;
            Collections.sort(todoList);
            todoAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
            parentViewGroup.removeView(progressBar);
            Snackbar.make(parentViewGroup.getRootView(),
                    R.string.loadDbSnack, Snackbar.LENGTH_LONG).show();
        }
    }

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
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
            Intent intent = new Intent(CalendarActivity.this, CalendarActivity.class);
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
                            Intent intent = new Intent(CalendarActivity.this, CalendarActivity.class);
                            intent.putExtra("USER_ID", Integer.toString(USER_ID));
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                            finish();
                        }
                        else if (menuItem.getItemId() == 2131362061) {
                            Log.i("DrawerClick", "Clicked Course Content");
                            Intent intent = new Intent(CalendarActivity.this, CourseContent.class);
                            intent.putExtra("USER_ID", Integer.toString(USER_ID));
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                            finish();
                        }
                        else if (menuItem.getItemId() == 2131362063) {
                            Log.i("DrawerClick", "Clicked Enrollment");
                            Intent intent = new Intent(CalendarActivity.this, EnrollmentActivity.class);
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
                            Intent intent = new Intent(CalendarActivity.this, HelpActivity.class);
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

                            Intent intent = new Intent(CalendarActivity.this, CourseActivity.class);
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

                            Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
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
                            Intent intent = new Intent(CalendarActivity.this, CourseActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            Log.i("DrawerClick", menuItem.toString());
                            finish();
                        }

                        //Log.i("MenuOnClick", menuItem.toString());

                        return true;
                    }
                }
        );

    }
}