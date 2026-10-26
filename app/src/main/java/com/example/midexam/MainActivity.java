package com.example.midexam;

import android.os.Bundle;
import android.database.Cursor;
import android.graphics.Color;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etStudentId;
    private Spinner spMajor;
    private LinearLayout studentList;
    private TextView tvHint;
    private ScrollView scrollView;
    private StudentDatabaseHelper dbHelper;

    private long selectedId = -1;

    private final String[] majors = {
            "Informatics",
            "Information Systems"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = findViewById(R.id.scrollView);

        etName = findViewById(R.id.etName);
        etStudentId = findViewById(R.id.etStudentId);
        spMajor = findViewById(R.id.spMajor);
        studentList = findViewById(R.id.studentList);
        tvHint = findViewById(R.id.tvHint);

        dbHelper = new StudentDatabaseHelper(this);

        // Handle mouse-wheel scrolling directly on the ScrollView.
        scrollView.setOnGenericMotionListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_SCROLL
                    && (event.getSource() & InputDevice.SOURCE_MOUSE)
                    == InputDevice.SOURCE_MOUSE) {

                float delta = event.getAxisValue(
                        MotionEvent.AXIS_VSCROLL
                );

                if (delta != 0) {
                    scrollView.smoothScrollBy(
                            0, (int) (-delta * 120)
                    );
                    return true;
                }
            }
            return false;
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                majors
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spMajor.setAdapter(adapter);

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnView = findViewById(R.id.btnView);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);

        btnAdd.setOnClickListener(v -> addStudent());
        btnView.setOnClickListener(v -> loadStudents());
        btnEdit.setOnClickListener(v -> editStudent());
        btnDelete.setOnClickListener(v -> deleteStudent());

        loadStudents();
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();

        if (name.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        return true;
    }

    private void addStudent() {
        if (!validateInput()) return;

        boolean success = dbHelper.addStudent(
                etStudentId.getText().toString().trim(),
                etName.getText().toString().trim(),
                spMajor.getSelectedItem().toString()
        );

        if (success) {
            Toast.makeText(
                    this,
                    "Student added successfully",
                    Toast.LENGTH_SHORT
            ).show();

            clearForm();
            loadStudents();
        } else {
            Toast.makeText(
                    this,
                    "Student ID already exists or data could not be saved",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void loadStudents() {
        studentList.removeAllViews();

        Cursor cursor = dbHelper.getAllStudents();
        int count = 0;

        try {
            while (cursor.moveToNext()) {
                count++;

                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow("_id")
                );

                String studentId = cursor.getString(
                        cursor.getColumnIndexOrThrow("student_id")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                String major = cursor.getString(
                        cursor.getColumnIndexOrThrow("major")
                );

                TextView item = new TextView(this);

                item.setText(
                        "Student Name: " + name +
                                "\nStudent ID: " + studentId +
                                "\nMajor: " + major +
                                "\nTap here to select"
                );

                item.setTextSize(16);
                item.setTextColor(Color.rgb(36, 59, 83));
                item.setPadding(24, 24, 24, 24);
                item.setBackgroundColor(Color.WHITE);
                item.setClickable(true);
                item.setFocusable(true);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.setMargins(0, 0, 0, 16);
                item.setLayoutParams(params);

                item.setOnClickListener(v -> {
                    selectedId = id;
                    etName.setText(name);
                    etStudentId.setText(studentId);

                    spMajor.setSelection(
                            major.equals("Information Systems") ? 1 : 0
                    );

                    tvHint.setText("Selected student: " + name);

                    Toast.makeText(
                            this,
                            "Student selected",
                            Toast.LENGTH_SHORT
                    ).show();
                });

                studentList.addView(item);
            }
        } finally {
            cursor.close();
        }

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("No student records yet.");
            empty.setTextSize(16);
            empty.setPadding(8, 16, 8, 16);
            studentList.addView(empty);
        }

        tvHint.setText(
                "Total students: " + count +
                        ". Tap a record to select it."
        );
    }

    private void editStudent() {
        if (selectedId == -1) {
            Toast.makeText(
                    this,
                    "Please select a student first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!validateInput()) return;

        boolean success = dbHelper.updateStudent(
                selectedId,
                etStudentId.getText().toString().trim(),
                etName.getText().toString().trim(),
                spMajor.getSelectedItem().toString()
        );

        if (success) {
            Toast.makeText(
                    this,
                    "Student updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

            clearForm();
            loadStudents();
        } else {
            Toast.makeText(
                    this,
                    "Student ID already exists or no changes were made",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void deleteStudent() {
        if (selectedId == -1) {
            Toast.makeText(
                    this,
                    "Please select a student first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean success = dbHelper.deleteStudent(selectedId);

        if (success) {
            Toast.makeText(
                    this,
                    "Student deleted successfully",
                    Toast.LENGTH_SHORT
            ).show();

            clearForm();
            loadStudents();
        } else {
            Toast.makeText(
                    this,
                    "Failed to delete student",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void clearForm() {
        etName.setText("");
        etStudentId.setText("");
        spMajor.setSelection(0);
        selectedId = -1;
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
