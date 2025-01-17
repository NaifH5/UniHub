package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
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
import android.widget.TextView;
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

public class RegisterFacultyDepInfoActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user;
    ProgressBar progressBar;
    Spinner depSpinner;
    TextView designationText;
    EditText acronymInRegister, idInRegister;
    Button btnRegisterInRegister;
    ImageButton btnBack;
    String accountType, name, number, email, password, department, acronym, id;
    int departmentID=1;

    String[] departments = {"Architecture", "Bangla", "Business Administration", "Civil Engineering",
            "Computer Science & Engineering", "Electrical and Electronics Engineering", "English",
            "Islamic Studies", "Law", "Public Health", "Tourism & Hospitality Management"};
    String[] designations = {"Adjunct Lecturer", "Assistant Proctor", "Assistant Professor",
            "Associate Professor", "Head", "Lecturer"};
    boolean[] selectedDesignations = new boolean[designations.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_faculty_dep_info);

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
        selectedDesignations[0] = true;

        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);
        depSpinner = findViewById(R.id.department_spinner);
        designationText = findViewById(R.id.designation_text);
        acronymInRegister = findViewById(R.id.acronym_in_register);
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

        ArrayAdapter depArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, departments);
        depArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        depSpinner.setAdapter(depArrayAdapter);

        designationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDesignationChoice();
            }
        });

        btnRegisterInRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                acronym = String.valueOf(acronymInRegister.getText());
                id = String.valueOf(idInRegister.getText());

                if(!validateAcronym(acronym)) {
                    Toast.makeText(RegisterFacultyDepInfoActivity.this, "Invalid acronym!", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerEmailAndPassword();
            }
        });
    }

    void showDesignationChoice() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Designations");

        builder.setMultiChoiceItems(designations, selectedDesignations, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedDesignations[which] = isChecked;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                StringBuilder selectedOptions = new StringBuilder();

                for(int i=0; i<selectedDesignations.length; i++) {

                    if(selectedDesignations[i]) {
                        selectedOptions.append(designations[i]).append(", ");
                    }
                }

                // To remove trailing comma and space
                if(selectedOptions.length()>0) {
                    selectedOptions.setLength(selectedOptions.length()-2);
                }

                designationText.setText(selectedOptions);
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    boolean validateAcronym(String acronym) {
        return acronym.length()==3;
    }

    void registerEmailAndPassword() {

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
                    Toast.makeText(RegisterFacultyDepInfoActivity.this, "Failed to register!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnRegisterInRegister.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    void registerPersonalDetails(String uId) {

        Map<String, Object> facultyMember = new HashMap<>();
        facultyMember.put("fullName", name);
        facultyMember.put("phoneNumber", number);
        facultyMember.put("email", email);
        facultyMember.put("departmentId", departmentID);
        facultyMember.put("acronym", acronym);
        facultyMember.put("id", id);

        rootReference.child("facultyMember").child(uId).setValue(facultyMember).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                registerDesignations(uId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(RegisterFacultyDepInfoActivity.this, "Failed to register!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnRegisterInRegister.setVisibility(View.VISIBLE);
            }
        });
    }

    void registerDesignations(String uId) {

        Map<String, Object> designation = new HashMap<>();

        for(int i=0; i<selectedDesignations.length; i++) {
            if(selectedDesignations[i]) {
                designation.put(designations[i], true);
            }
        }

        rootReference.child("designations").child(uId).setValue(designation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Intent intent = new Intent(RegisterFacultyDepInfoActivity.this, AddProfilePictureActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("account_type", accountType);
                RegisterFacultyDepInfoActivity.this.startActivity(intent);

                progressBar.setVisibility(View.GONE);
                btnRegisterInRegister.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterFacultyDepInfoActivity.this, "Failed to register!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnRegisterInRegister.setVisibility(View.VISIBLE);
            }
        });
    }
}