package com.example.bohringapp.calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.example.bohringapp.enrollment.EnrollmentDatabaseHelper;
import com.google.gson.Gson;

import java.util.ArrayList;

import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.COURSE_CODE_COLUMN;
import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.COURSE_DESCRIPTION_COLUMN;
import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.COURSE_TABLE;
import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper.ENROLLMENT_TABLE;
import static com.example.bohringapp.enrollment.EnrollmentDatabaseHelper._USER_ID_COLUMN;

public class CalendarDatabaseHelper {

    public static final String TODO_TABLE = "todoTable";
    static final String TODO_KEY = "KeyID";
    static final String TODO_VAL = "TodoVal";
    static final String USER_ID_COLUMN = "todoUserID";


    public static final String CREATE_TODO_TABLE =
            "CREATE TABLE " + TODO_TABLE + " (" +
                TODO_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TODO_VAL + " TEXT NOT NULL," +
                COURSE_CODE_COLUMN + " TEXT NOT NULL," +
                USER_ID_COLUMN + " TEXT NOT NULL)";

    public static final String SELECT_ENROLLED_COURSES =
            "SELECT * FROM " + EnrollmentDatabaseHelper.ENROLLMENT_TABLE
            + " INNER JOIN `Course` ON `Enrollment`.`_courseCode` = `Course`.`courseCode`"
            + " WHERE `Enrollment`.`_userID` = ?"
            + " ORDER BY `Enrollment`.`_courseCode`;";

    public static final String SELECT_TODO_ITEMS =
            "SELECT * FROM " + TODO_TABLE +
                    " WHERE EXISTS (" +
                    "SELECT * FROM " + ENROLLMENT_TABLE + " WHERE " +
                    TODO_TABLE + "." + USER_ID_COLUMN + "=" + ENROLLMENT_TABLE + "._userID)" +
                    " AND " + USER_ID_COLUMN + "=?;";

    public static ArrayList<String> getCourseCodes(SQLiteDatabase db) {
        ArrayList<String> courseCodes = new ArrayList();
        Cursor cursor = db.query(
                COURSE_TABLE,
                new String[] {COURSE_CODE_COLUMN},
                null,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            courseCodes.add(cursor.getString(cursor.getColumnIndex(COURSE_CODE_COLUMN)));
            cursor.moveToNext();
        }

        return courseCodes;
    }

    public static ArrayList<String> getCourseNames(SQLiteDatabase db) {
        ArrayList<String> courseCodes = new ArrayList();
        Cursor cursor = db.query(
                COURSE_TABLE,
                new String[] {COURSE_DESCRIPTION_COLUMN},
                null,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            courseCodes.add(cursor.getString(cursor.getColumnIndex(COURSE_DESCRIPTION_COLUMN)));
            cursor.moveToNext();
        }

        return courseCodes;
    }

    public static int getNumEnrolledCourses(int userId, SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM Enrollment WHERE _userID = " + userId, null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static void insertTodoItem(TodoItem todoItem, int userId, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(TODO_VAL, new Gson().toJson(todoItem));
        values.put(COURSE_CODE_COLUMN, todoItem.getCourseCode());
        values.put(USER_ID_COLUMN, userId);
        db.insert(TODO_TABLE, null, values);
    }

    public static void replaceTodoItem(TodoItem todoItem, int userID, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(TODO_VAL, new Gson().toJson(todoItem));
        values.put(COURSE_CODE_COLUMN, todoItem.getCourseCode());
        values.put(TODO_KEY, todoItem.getDbKey());
        values.put(USER_ID_COLUMN, userID);
        db.replace(TODO_TABLE, null, values);
    }

    public static void deleteTodoItem(TodoItem todoItem, int userID, SQLiteDatabase db) {
        String[] whereArgs = {String.valueOf(todoItem.getDbKey())};
        db.delete(TODO_TABLE, CalendarDatabaseHelper.TODO_KEY + "=?", whereArgs);
    }

    public static ArrayList<TodoItem> getTodoItems(SQLiteDatabase db) {
        ArrayList<TodoItem> todoItems = new ArrayList();
        Cursor cursor = db.query(
                TODO_TABLE,
                new String[] {TODO_VAL},
                null,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        Gson gson = new Gson();
        while (!cursor.isAfterLast()) {
            todoItems.add(gson.fromJson(
                    cursor.getString(cursor.getColumnIndex(TODO_VAL)), TodoItem.class));
            cursor.moveToNext();
        }

        return todoItems;
    }
}
