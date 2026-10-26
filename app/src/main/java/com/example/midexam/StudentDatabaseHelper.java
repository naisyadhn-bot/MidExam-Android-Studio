package com.example.midexam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StudentDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mid_exam.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "students";
    public static final String COL_ID = "_id";
    public static final String COL_STUDENT_ID = "student_id";
    public static final String COL_NAME = "name";
    public static final String COL_MAJOR = "major";

    public StudentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_STUDENT_ID + " TEXT UNIQUE NOT NULL, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_MAJOR + " TEXT NOT NULL)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addStudent(String studentId,
                              String name, String major) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_NAME, name);
        values.put(COL_MAJOR, major);

        try {
            return db.insertOrThrow(TABLE_NAME, null, values) != -1;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean updateStudent(long id, String studentId,
                                 String name, String major) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_NAME, name);
        values.put(COL_MAJOR, major);

        try {
            return db.update(TABLE_NAME, values,
                    COL_ID + "=?",
                    new String[]{String.valueOf(id)}) > 0;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean deleteStudent(long id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(TABLE_NAME, COL_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAllStudents() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(TABLE_NAME, null, null, null,
                null, null, COL_ID + " DESC");
    }
}
