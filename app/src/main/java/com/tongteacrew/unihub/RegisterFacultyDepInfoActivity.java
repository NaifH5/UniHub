package com.tongteacrew.unihub;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterFacultyDepInfoActivity extends AppCompatActivity {

    Spinner depSpinner;
    TextView designationText;
    EditText acronymInRegister;
    Button btnRegisterInRegister;
    ImageButton btnBack;
    String accountType, name, number, email, password, department, acronym;

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

        // To keep navigation bar, but make status bar transparent
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        // To fetch data from previous activity
        accountType = (String) getIntent().getSerializableExtra("account_type");
        name = (String) getIntent().getSerializableExtra("full_name");
        number = (String) getIntent().getSerializableExtra("phone_number");
        email = (String) getIntent().getSerializableExtra("email");
        password = (String) getIntent().getSerializableExtra("password");

        // To set default values
        department = "Business Administration";
        selectedDesignations[0] = true;

        btnBack = findViewById(R.id.btn_back);
        depSpinner = findViewById(R.id.department_spinner);
        designationText = findViewById(R.id.designation_text);
        acronymInRegister = findViewById(R.id.acronym_in_register);
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

                if(!validateID(acronym)) {
                    Toast.makeText(RegisterFacultyDepInfoActivity.this, "Invalid acronym!", Toast.LENGTH_SHORT).show();
                    return;
                }

                register();
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

    boolean validateID(String acronym) {
        return acronym.length()==3;
    }

    void register() {

        System.out.println(accountType);
        System.out.println(department);
        System.out.println(acronym);
        System.out.println(name);
        System.out.println(number);
        System.out.println(email);
        System.out.println(password);

        Intent intent = new Intent(RegisterFacultyDepInfoActivity.this, AddProfilePictureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("account_type", accountType);
        RegisterFacultyDepInfoActivity.this.startActivity(intent);
    }
}