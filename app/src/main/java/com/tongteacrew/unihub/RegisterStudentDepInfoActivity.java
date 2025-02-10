package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterStudentDepInfoActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user;
    Button btnRegisterInRegister;
    ImageButton btnBack;
    EditText idInRegister;
    ProgressBar progressBar;
    Spinner depSpinner, batchSpinner, sectionSpinner;
    private String accountType, department, batch, section, id, name, number, email, password, sectionId="A";
    private int departmentID=1, batchId=56;

    String[] departments = {"Architecture", "Bangla", "Business Administration", "Civil Engineering",
            "Computer Science & Engineering", "Electrical & Electronics Engineering", "English",
            "Islamic Studies", "Law", "Public Health", "Tourism & Hospitality Management"};
    String[] batches = {"Batch 56", "Batch 57", "Batch 58", "Batch 59", "Batch 60", "Batch 61",
            "Batch 62", "Batch 63", "Batch 64"};
    int[] batchIds = {56, 57, 58, 59, 60, 61, 62, 63, 64};
    String[] sections = {"Section A", "Section B", "Section C", "Section D", "Section E", "Section F",
            "Section G", "Section H", "Section I", "Section J"};
    String[] sectionIds = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student_dep_info);

        // To make light status bar background
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // To fetch data from previous activity
        accountType = (String) getIntent().getSerializableExtra("account_type");
        name = (String) getIntent().getSerializableExtra("full_name");
        number = (String) getIntent().getSerializableExtra("phone_number");
        email = (String) getIntent().getSerializableExtra("email");
        password = (String) getIntent().getSerializableExtra("password");

        // To set default values
        department = "Business Administration";
        batch = "Batch 56";
        section = "Section A";

        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);
        depSpinner = findViewById(R.id.department_spinner);
        batchSpinner = findViewById(R.id.batch_spinner);
        sectionSpinner = findViewById(R.id.section_spinner);
        idInRegister = findViewById(R.id.id_in_register);
        btnRegisterInRegister = findViewById(R.id.btn_register_in_register);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        depSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                department = departments[position];
                departmentID = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        batchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                batch = batches[position];
                batchId = batchIds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                section = sections[position];
                sectionId = sectionIds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter depArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, departments);
        depArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        depSpinner.setAdapter(depArrayAdapter);

        ArrayAdapter batchArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, batches);
        batchArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchSpinner.setAdapter(batchArrayAdapter);

        ArrayAdapter sectionArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sections);
        sectionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(sectionArrayAdapter);

        btnRegisterInRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                id = String.valueOf(idInRegister.getText());

                if(!validateID(id)) {
                    Toast.makeText(RegisterStudentDepInfoActivity.this, "Invalid ID!", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerEmailAndPassword();
            }
        });
    }

    boolean validateID(String id) {
        String regex = "^[0-9]{10}$";
        return !id.isEmpty() && id.matches(regex);
    }

    public void registerEmailAndPassword() {

        progressBar.setVisibility(View.VISIBLE);
        btnRegisterInRegister.setVisibility(View.GONE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {
                    user = mAuth.getCurrentUser();
                    registerPersonalDetails(user.getUid());
                }
                else {
                    Toast.makeText(RegisterStudentDepInfoActivity.this, "Failed to register!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void registerPersonalDetails(String uId) {

        Map<String, Object> student = new HashMap<>();
        student.put("fullName", name);
        student.put("phoneNumber", number);
        student.put("email", email);
        student.put("id", id);
        student.put("departmentId", departmentID);
        student.put("batchId", batchId);
        student.put("sectionId", sectionId);
        student.put("profilePicture", null);
        student.put("notes", null);

        rootReference.child("student").child(uId).setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Intent intent = new Intent(RegisterStudentDepInfoActivity.this, AddProfilePictureActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("account_type", accountType);
                RegisterStudentDepInfoActivity.this.startActivity(intent);

                progressBar.setVisibility(View.GONE);
                btnRegisterInRegister.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(RegisterStudentDepInfoActivity.this, "Failed to register!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnRegisterInRegister.setVisibility(View.VISIBLE);
            }
        });
    }
}