package com.example.bohringapp.enrollment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.bohringapp.calendar.CalendarDatabaseHelper;

public class EnrollmentDatabaseHelper extends SQLiteOpenHelper {
    protected static final String ACTIVITY_NAME = "EnrollmentDatabaseHelper";

    public static SQLiteDatabase database;

    public static final String ENROLLMENT_TABLE = "Enrollment";
    public static final String _USER_ID_COLUMN = "_userID";
    public static final String _COURSE_CODE_COLUMN = "_courseCode";
    public static final String COLOR_COLUMN = "color";

    public static final String COURSE_TABLE = "Course";
    public static final String COURSE_CODE_COLUMN = "courseCode";
    public static final String COURSE_DESCRIPTION_COLUMN = "courseDescription";
    public static final String HAS_CONTENT_COLUMN = "hasContent";
    public static final String COURSE_LINK_COLUMN = "courseLink";

    public static final String USER_TABLE = "User";
    public static final String USER_ID_COLUMN = "userID";
    public static final String IS_ADMIN_COLUMN = "is_admin";

    public static final String DATABASE_NAME = "bohringApp.db";
    private static final int DATABASE_VERSION = 14;

    public static final String COURSE_LINK_TABLE = "CourseLinksTable";

    public static final String COURSE_COLUMN = "courseCode";

    public static final String LINK_NAME_COLUMN = "LINK_NAME";

    public static final String LINK_COLUMN = "LINKS";

    // Table/Database creation statement
    private static final String ENROLLMENT_TABLE_CREATE = "CREATE TABLE "
            + ENROLLMENT_TABLE + " ("
            + _USER_ID_COLUMN + " INTEGER NOT NULL, "
            + _COURSE_CODE_COLUMN + " TEXT NOT NULL, "
            + COLOR_COLUMN + " INTEGER NOT NULL, "
            + "PRIMARY KEY (" + _USER_ID_COLUMN + "," + _COURSE_CODE_COLUMN + "));";

    private static final String COURSE_TABLE_CREATE = "CREATE TABLE "
            + COURSE_TABLE + " ("
            + COURSE_CODE_COLUMN + " TEXT NOT NULL PRIMARY KEY, "
            + COURSE_DESCRIPTION_COLUMN + " INTEGER NOT NULL, "
            + HAS_CONTENT_COLUMN + "  INTEGER NOT NULL, "
            + COURSE_LINK_COLUMN + " TEXT);";

    private static final String USER_TABLE_CREATE = "CREATE TABLE "
            + USER_TABLE + " ("
            + USER_ID_COLUMN + " INTEGER NOT NULL PRIMARY KEY, "
            + IS_ADMIN_COLUMN + "  INTEGER NOT NULL);";

    private static final String POPULATE_COURSE_TABLE = "INSERT INTO " + COURSE_TABLE + " (" + COURSE_CODE_COLUMN + "," + COURSE_DESCRIPTION_COLUMN + "," + HAS_CONTENT_COLUMN + "," + COURSE_LINK_COLUMN + ") " +
            "VALUES " +
            "   ('CP104','Introduction to programming', 1, 'https://bohr.wlu.ca/cp104/')," +
            "   ('CP164','Data Structures I', 1, 'https://bohr.wlu.ca/cp164/')," +
            "   ('CP264','Data Structures II', 1, 'https://bohr.wlu.ca/cp264/')," +
            "   ('CP212','Windows Application Programming', 1, 'https://bohr.wlu.ca/cp212/');";

    private static final String POPULATE_USER_TABLE = "INSERT INTO " + USER_TABLE + " (" + USER_ID_COLUMN + "," + IS_ADMIN_COLUMN +")" +
            "VALUES " +
            "   (1,1)," +
            "   (2,0);";


    public static final String CREATE_COURSE_LINK_TABLE =
            "CREATE TABLE " + COURSE_LINK_TABLE + " ("
            + COURSE_COLUMN + " TEXT NOT NULL, "
            + LINK_COLUMN + " TEXT NOT NULL, "
            + LINK_NAME_COLUMN + " TEXT NOT NULL, "
            + "FOREIGN KEY (" + COURSE_COLUMN + ") REFERENCES `Enrollment`(`_courseCode`));";

    private static final String POPULATE_LINK_TABLE =
            "INSERT INTO " + COURSE_LINK_TABLE
                    + " (" + COURSE_COLUMN + "," + LINK_COLUMN + "," + LINK_NAME_COLUMN + ")"
                    + "VALUES "
                    + "  ('cp104','https://bohr.wlu.ca/cp104/','home'),"
                    + "  ('cp104','https://bohr.wlu.ca/cp104/mailing_list.php','Mailing List'),"
                    + "  ('cp104','https://bohr.wlu.ca/cp104/email.php','Email Etiquette'),"
                    + "  ('cp104','https://bohr.wlu.ca/cp104/tools.php','Tools'),"
                    + "  ('cp104','https://bohr.wlu.ca/cp104/standards.php','Programming Standards'),"
                    + "  ('cp104','https://bohr.wlu.ca/cp104/metaphors.php','Programming as a Sport'),"
                    + "  ('cp164','https://bohr.wlu.ca/cp164/','home'),"
                    + "  ('cp164','https://bohr.wlu.ca/cp164/syllabus.php','Syllabus'),"
                    + "  ('cp164','https://bohr.wlu.ca/cp164/mailing_list.php','Mailing List'),"
                    + "  ('cp164','https://bohr.wlu.ca/cp164/email.php','Email Etiquette'),"
                    + "  ('cp164','https://bohr.wlu.ca/cp164/notes/','Notes'),"
                    + "  ('cp164','https://bohr.wlu.ca/cp164/asgns/','Assignments'),"
                    + "  ('cp164','https://bohr.wlu.ca/cp164/tools.php','Tools'),"
                    + "  ('cp164','https://bohr.wlu.ca/cp164/therac/therac.html','Therac-25'),"
                    + "  ('cp264','https://bohr.wlu.ca/cp264/','home'),"
                    + "  ('cp264','https://bohr.wlu.ca/hfan/cp264/20/index.html','Syllabus'),"
                    + "  ('cp264','https://bohr.wlu.ca/cp264/mailing_list.php','Mailing List'),"
                    + "  ('cp264','https://bohr.wlu.ca/cp264/tools.php','Tools'),"
                    + "  ('cp212','https://bohr.wlu.ca/cp212/','home'),"
                    + "  ('cp212','https://bohr.wlu.ca/cp212/Schedule_Fall_2020.html','Dr. Ali Schedule'),"
                    + "  ('cp212','https://bohr.wlu.ca/cp212/mailing_list.php','Mailing List'),"
                    + "  ('cp212','https://bohr.wlu.ca/cp212/email.php','Email Etiquette'),"
                    + "  ('cp363','https://bohr.wlu.ca/cp363/','home'),"
                    + "  ('cp363','https://bohr.wlu.ca/cp363/mailing_list.php','Mailing List'),"
                    + "  ('cp363','https://bohr.wlu.ca/cp363/email.php','Email Etiquette'),"
                    + "  ('cp363','https://bohr.wlu.ca/cp363/tools.php','Tools'),"
                    + "  ('cp363','https://bohr.wlu.ca/cp363/notes/','Notes'),"
                    + "  ('cp363','https://bohr.wlu.ca/cp363/asgns/','Assignments'),"
                    + "  ('cp363','https://bohr.wlu.ca/cp363/practice/','Practice');";

    public static SQLiteDatabase databaseFactory () {
        return database ;
    }


    public EnrollmentDatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(ACTIVITY_NAME, "Calling onCreate");
        database.execSQL(ENROLLMENT_TABLE_CREATE);
        database.execSQL(COURSE_TABLE_CREATE);
        database.execSQL(USER_TABLE_CREATE);
        database.execSQL(POPULATE_COURSE_TABLE);
        database.execSQL(POPULATE_USER_TABLE);
        database.execSQL(CalendarDatabaseHelper.CREATE_TODO_TABLE);
        database.execSQL(CREATE_COURSE_LINK_TABLE);
        database.execSQL(POPULATE_LINK_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(ACTIVITY_NAME, "Calling onUpgrade, oldVersion=" + oldVersion + " newVersion=" + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + ENROLLMENT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COURSE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CalendarDatabaseHelper.TODO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COURSE_LINK_TABLE);

        onCreate(db);
    }
}

