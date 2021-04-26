package com.example.bohringapp.loginHome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bohringapp.R;
import com.example.bohringapp.enrollment.EnrollmentDatabaseHelper;

public class LoginActivity extends AppCompatActivity {


    //Shared Preferences to save user logged in
    //Log out button

    EditText editText;
    public SQLiteDatabase db;
    String val = "";
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


         sharedPref = getApplicationContext()
                .getSharedPreferences("BohringApp", MODE_PRIVATE);
        String name = sharedPref.getString("User", "-1");

        Log.i("NameTest", name);
        if (Integer.parseInt(name) != -1) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("Username", name);
            startActivity(intent);
            finish();
        }
        editText = findViewById(R.id.username);
    }

    public void loginClick(View view) {

        SharedPreferences.Editor editor = sharedPref.edit();

        if (isNumeric(editText.getText().toString())) {
            String username = editText.getText().toString();
            EnrollmentDatabaseHelper dbHelper = new EnrollmentDatabaseHelper(this);
            db = dbHelper.getWritableDatabase();

            String select = "SELECT * FROM User WHERE userID = " + username + ";";
            Cursor cursor = db.rawQuery(select, null);
            if (cursor.getCount() < 1) {
                ContentValues values = new ContentValues();
                values.put(EnrollmentDatabaseHelper.USER_ID_COLUMN, username);
                values.put(EnrollmentDatabaseHelper.IS_ADMIN_COLUMN, 0);

                long rowID = db.insert(EnrollmentDatabaseHelper.USER_TABLE, null, values);
                Log.i("RowID", "RowID" + rowID);
            }
            else {
                ContentValues cv = new ContentValues();
                cv.put("userID", username);
                cv.put("is_admin", "0");

                db.update("User", cv, "userID = ?", new String[]{username});
            }

            editor.putString("User", username);
            editor.apply();


            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("Username", username);
            startActivity(intent);
            finish();
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.errText), Toast.LENGTH_LONG);
            toast.show();

        }
    }

    public void loginClickAdmin(View view) {

        SharedPreferences.Editor editor = sharedPref.edit();
        if (isNumeric(editText.getText().toString())) {
            String username = editText.getText().toString();
            EnrollmentDatabaseHelper dbHelper = new EnrollmentDatabaseHelper(this);
            db = dbHelper.getWritableDatabase();

            String select = "SELECT * FROM User WHERE userID = " + username + ";";
            Cursor cursor = db.rawQuery(select, null);
            if (cursor.getCount() < 1) {
                ContentValues values = new ContentValues();
                values.put(EnrollmentDatabaseHelper.USER_ID_COLUMN, username);
                values.put(EnrollmentDatabaseHelper.IS_ADMIN_COLUMN, 1);

                long rowID = db.insert(EnrollmentDatabaseHelper.USER_TABLE, null, values);
                Log.i("RowID", "RowID" + rowID);
            }
            else {
                ContentValues cv = new ContentValues();
                cv.put("userID", username);
                cv.put("is_admin", "1");

                db.update("User", cv, "userID = ?", new String[]{username});
            }

            editor.putString("User", username);
            editor.apply();


            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("Username", username);
            startActivity(intent);
            finish();
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.errText), Toast.LENGTH_LONG);
            toast.show();

        }
    }

    private boolean isNumeric(String s) {
        if (s == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}