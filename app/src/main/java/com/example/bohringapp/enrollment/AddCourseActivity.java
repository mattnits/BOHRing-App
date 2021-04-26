package com.example.bohringapp.enrollment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bohringapp.R;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AddCourseActivity extends AppCompatActivity {

    protected static final String ACTIVITY_NAME = "AddCourseActivity";

    private ArrayList<CourseItem> courseList;
    private ArrayList<CourseItem> addCourseList;
    private CourseAdapter courseAdapter;
    SQLiteDatabase database;

    int USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        USER_ID = getIntent().getIntExtra("USER_ID", 1);

        ListView courseListView = (ListView) findViewById(R.id.available_classes);
        this.courseList = new ArrayList<CourseItem>();
        this.addCourseList = new ArrayList<CourseItem>();
        this.courseAdapter = new CourseAdapter(this);
        courseListView.setAdapter(this.courseAdapter);

        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                CourseItem course = AddCourseActivity.this.courseList.get(position);
                if (course.isSelected()) {
                    addCourseList.remove(course);
                    course.deselect();
                }
                else {
                    course.select();
                    addCourseList.add(course);
                }
                AddCourseActivity.this.courseList.remove(position);
                AddCourseActivity.this.courseList.add(position, course);

                AddCourseActivity.this.courseAdapter.notifyDataSetChanged();
            }
        });

        // retrieve from DB
        EnrollmentDatabaseHelper databaseHelper = new EnrollmentDatabaseHelper(this);
        database = databaseHelper.getWritableDatabase();
        String select = "SELECT * FROM " + EnrollmentDatabaseHelper.COURSE_TABLE
                + " WHERE `Course`.`courseCode` NOT IN ("
                + "SELECT `Enrollment`.`_courseCode` FROM `Enrollment` "
                + " WHERE `Enrollment`.`_userID` =" + USER_ID + ");";

        Cursor cursor = database.rawQuery(select, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String courseCode = cursor.getString( cursor.getColumnIndex( EnrollmentDatabaseHelper.COURSE_CODE_COLUMN) );
            String courseDescription = cursor.getString( cursor.getColumnIndex( EnrollmentDatabaseHelper.COURSE_DESCRIPTION_COLUMN) );
            CourseItem item = new CourseItem(courseCode, courseDescription);
            courseList.add(item);
            cursor.moveToNext();
        }
        cursor.close();

    }

    public void addClicked(View view) {
        Intent addCourseIntent = new Intent();
        String addCourseListJSON = new Gson().toJson(AddCourseActivity.this.addCourseList);
        addCourseIntent.putExtra("courseList", addCourseListJSON);
        setResult(RESULT_OK, addCourseIntent);
        finish();
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
            LayoutInflater inflater = AddCourseActivity.this.getLayoutInflater();
            View result = inflater.inflate(R.layout.activity_enrollment_course_item, null);

            TextView courseCodeName = (TextView) result.findViewById((R.id.course_code_name));
            String codeName = getItem(position).getCode() + " - " + getItem(position).getName();
            courseCodeName.setText(codeName);

            View courseColor = (View) result.findViewById((R.id.course_color));

            if (getItem(position).isSelected()) {
                courseColor.setBackgroundColor(Color.BLACK);
            }
            else {
                courseColor.setBackgroundColor(Color.WHITE);
            }

            return result;
        }
    }
}