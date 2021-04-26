package com.example.bohringapp.enrollment;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bohringapp.R;


public class CreateCourseFragment extends Fragment {

    View view;
    Button cancelButton;
    Button createButton;
    EditText courseCodeEditText;
    EditText courseNameEditText;
    SQLiteDatabase database;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_course, container, false);

        // connect with db
        EnrollmentDatabaseHelper databaseHelper = new EnrollmentDatabaseHelper(getActivity());
        database = databaseHelper.getWritableDatabase();

        courseCodeEditText = (EditText) view.findViewById(R.id.editTextCreateCourse);
        courseNameEditText = (EditText) view.findViewById(R.id.editText2CreateCourse);

        cancelButton = (Button) view.findViewById(R.id.button2CreateCourse);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        createButton = (Button) view.findViewById(R.id.buttonCreateCourse);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseCode = courseCodeEditText.getText().toString();
                String courseName = courseNameEditText.getText().toString();

                if (courseCode.matches("") || courseName.matches("") ) {
                    Toast.makeText(getActivity(), getString(R.string.MissingCourseCodeNameMsg), Toast.LENGTH_SHORT).show();
                }
                else {
                    ContentValues insertValues = new ContentValues();
                    insertValues.put(EnrollmentDatabaseHelper.COURSE_CODE_COLUMN, courseCode);
                    insertValues.put(EnrollmentDatabaseHelper.COURSE_DESCRIPTION_COLUMN, courseName);
                    insertValues.put(EnrollmentDatabaseHelper.HAS_CONTENT_COLUMN, 0);
                    try
                    {
                        database.insertOrThrow("Course", null, insertValues);
                    }
                    catch (SQLException e)
                    {
                        Toast toast = Toast.makeText(getActivity() , getString(R.string.InsertFailure), Toast.LENGTH_SHORT); //this is the ListActivity
                        toast.show();
                    }

                    getActivity().finish();
                }
            }
        });

        return view;
    }
}