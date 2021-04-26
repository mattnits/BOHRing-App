package com.example.bohringapp.calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bohringapp.R;
import com.example.bohringapp.enrollment.EnrollmentDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.COURSE_CODE_COLUMN;
import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.COURSE_DESCRIPTION_COLUMN;
import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.COURSE_TABLE;
import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.ENROLLMENT_TABLE;
import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.USER_ID_COLUMN;

public class TodoFragment extends Fragment {
    private SQLiteDatabase db;
    private int requestCode;
    private TodoItem todoItem;
    private ProgressBar progressBar;
    private Spinner courseCodeSpinner;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView dueDateTextView;
    private Button saveButton;
    private Button deleteButton;
    private long dueDate = 0L;
    ArrayList<String> courseCodes = new ArrayList();
    ArrayList<String> courseNames = new ArrayList();
    private ArrayList<String> courses = new ArrayList();
    private int USER_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        USER_ID = bundle.getInt("USER_ID");
        Log.i("ID", "" + USER_ID);
        View view = inflater.inflate(R.layout.fragment_todo, container, false);
        courseCodeSpinner = view.findViewById(R.id.todo_fragment_courseCode);
        progressBar = view.findViewById(R.id.todo_fragment_progress);
        db = new EnrollmentDatabaseHelper(getContext()).getReadableDatabase();
        new CourseQuery().execute();


        titleEditText = view.findViewById(R.id.todo_fragment_title);
        descriptionEditText = view.findViewById(R.id.todo_fragment_description);
        dueDateTextView = view.findViewById(R.id.todo_fragment_dueDate);
        dueDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater()
                        .inflate(R.layout.todo_date_time_dialog, null);
                ((CalendarView) view.findViewById((R.id.todo_date_calendarView)))
                        .setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                            @Override
                            public void onSelectedDayChange(@NonNull CalendarView view, int year,
                                                            int month, int dayOfMonth) {
                                Log.i("OLD DATE", String.valueOf(dueDate));
                                dueDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
                                Log.i("SELECTED CHANGE", "Year: " + year);
                                Log.i("SELECTED CHANGE", "Month: " + month);
                                Log.i("SELECTED CHANGE", "Day: " + dayOfMonth);
                                Log.i("NEW DATE", String.valueOf(dueDate));
                            }
                        });
                new AlertDialog.Builder(getContext())
                        .setView(view)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dueDateTextView.setText(new SimpleDateFormat(
                                        "MM/dd/yyyy").format(new Date(dueDate)));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}})
                        .create()
                        .show();
            }
        });

        saveButton = view.findViewById(R.id.todo_fragment_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                todoItem.setCourseCode(courseCodes.get(courseCodeSpinner.getSelectedItemPosition()));
                if (titleEditText.getText().toString().length() == 0 || dueDate == 0L) {
                    Toast invalid_input_toast = new Toast(getContext());
                    invalid_input_toast.setText(R.string.invalid_input_todo);
                    invalid_input_toast.setDuration(Toast.LENGTH_SHORT);
                    invalid_input_toast.show();
                } else {
                    todoItem.setTitle(titleEditText.getText().toString());
                    todoItem.setDescription(descriptionEditText.getText().toString());
                    todoItem.setDate(dueDate);

                    Intent intent = new Intent();
                    intent.putExtra("todoItem", todoItem);
                    if (requestCode == TodoActivity.EDIT_TODO_REQUEST) {
                        getActivity().setResult(TodoActivity.EDIT_TODO_RESPONSE, intent);
                    } else if (requestCode == TodoActivity.NEW_TODO_REQUEST){
                        getActivity().setResult(TodoActivity.NEW_TODO_RESPONSE, intent);
                    }
                    getActivity().finish();
                }
            }
        });

        deleteButton = view.findViewById(R.id.todo_fragment_delete);

        requestCode = bundle.getInt("requestCode");
        if (requestCode == TodoActivity.EDIT_TODO_REQUEST) {
            todoItem = bundle.getParcelable("todoItem");
            setEditViews();
        } else if (requestCode == TodoActivity.NEW_TODO_REQUEST){
            todoItem = new TodoItem();
        }

        return view;
    }

    private void setEditViews() {
        courseCodeSpinner.setSelection(courseCodes.indexOf(todoItem.getCourseCode()));
        titleEditText.setText(todoItem.getTitle());
        descriptionEditText.setText(todoItem.getDescription());
        dueDateTextView.setText(todoItem.getPrettyDueDate());
        dueDate = todoItem.getDueDate();
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.todo_delete_todo_item);
                builder.setPositiveButton(R.string.todo_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.putExtra("todoItem", todoItem);
                        getActivity().setResult(TodoActivity.DELETE_TODO_RESPONSE, intent);
                        getActivity().finish();
                    }
                });
                builder.setNegativeButton(R.string.todo_decline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private class CourseQuery extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            courseCodes.clear();
            long totalCourses = DatabaseUtils.queryNumEntries(db, COURSE_TABLE);
            long curCourses = 0;
            publishProgress(0);
            Log.i("USERS ASYNC", "" + USER_ID);
            Cursor cursor = db.rawQuery(CalendarDatabaseHelper.SELECT_ENROLLED_COURSES,
                    new String[] {String.valueOf(USER_ID)});
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                courseCodes.add(cursor.getString(cursor.getColumnIndex(COURSE_CODE_COLUMN)));
                courseNames.add(cursor.getString(cursor.getColumnIndex(COURSE_DESCRIPTION_COLUMN)));
                cursor.moveToNext();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress((int) (curCourses / totalCourses));
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
            for (int i = 0; i < courseNames.size(); i ++) {
                courses.add(courseCodes.get(i) + ": " + courseNames.get(i));
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(
                    getContext(), android.R.layout.simple_spinner_dropdown_item, courses);
            courseCodeSpinner.setAdapter(spinnerAdapter);
            progressBar.setVisibility(View.INVISIBLE);
            ((ViewGroup) progressBar.getParent()).removeView(progressBar);
        }
    }
}