package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Objects;

public class RegisterAuthInfoActivity extends AppCompatActivity {

    Button btnNextInRegister;
    ImageButton btnBack;
    ImageView btnPasswordVisibilityInRegister, bgImage;
    EditText fullNameInRegister, phoneNumberInRegister, emailInRegister, passwordInRegister;
    private String accountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_auth_info);

        // To make light status bar background
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        btnBack = findViewById(R.id.btn_back);
        bgImage = findViewById(R.id.bg_image);
        fullNameInRegister = findViewById(R.id.full_name_in_register);
        phoneNumberInRegister = findViewById(R.id.phone_number_in_register);
        emailInRegister = findViewById(R.id.email_in_register);
        passwordInRegister = findViewById(R.id.password_in_register);
        btnPasswordVisibilityInRegister = findViewById(R.id.btn_password_visibility_in_register);
        btnNextInRegister = findViewById(R.id.btn_next_in_register);

        // To fetch data from previous activity
        accountType = (String) getIntent().getSerializableExtra("account_type");

        if(Objects.equals(accountType, "student")) {
            bgImage.setImageResource(R.drawable.illustration_1);
        }
        else {
            bgImage.setImageResource(R.drawable.illustration_2);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // To change visibility of password
        btnPasswordVisibilityInRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(passwordInRegister.getTransformationMethod().getClass().getSimpleName().equals("PasswordTransformationMethod")) {
                    // To show password
                    passwordInRegister.setTransformationMethod(new SingleLineTransformationMethod());
                    btnPasswordVisibilityInRegister.setImageResource(R.drawable.icon_password_visibility_not_visible);
                }
                else {
                    // To hide password
                    passwordInRegister.setTransformationMethod(new PasswordTransformationMethod());
                    btnPasswordVisibilityInRegister.setImageResource(R.drawable.icon_password_visibility_visible);
                }

                // To move cursor to the end of the text
                passwordInRegister.setSelection(passwordInRegister.getText().length());
            }
        });

        // To go to next activity
        btnNextInRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = String.valueOf(fullNameInRegister.getText());
                String number = String.valueOf(phoneNumberInRegister.getText());
                String email = String.valueOf(emailInRegister.getText());
                String password = String.valueOf(passwordInRegister.getText());

                if(!validateName(name)) {
                    Toast.makeText(RegisterAuthInfoActivity.this, "Invalid name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!validateNumber(number)) {
                    Toast.makeText(RegisterAuthInfoActivity.this, "Invalid phone number!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!validateEmail(email)) {
                    Toast.makeText(RegisterAuthInfoActivity.this, "Invalid email!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!validatePassword(password)) {
                    Toast.makeText(RegisterAuthInfoActivity.this, "Invalid password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent;

                if(Objects.equals(accountType, "student")) {
                    intent = new Intent(RegisterAuthInfoActivity.this, RegisterStudentDepInfoActivity.class);
                }
                else {
                    intent = new Intent(RegisterAuthInfoActivity.this, RegisterFacultyDepInfoActivity.class);
                }

                intent.putExtra("account_type", accountType);
                intent.putExtra("full_name", name);
                intent.putExtra("phone_number", number);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                RegisterAuthInfoActivity.this.startActivity(intent);
            }
        });
    }

    // Regex for Name: Non-empty string with no special characters or digitsa
    private boolean validateName(String name) {
        String regex = "^[A-Za-z\\s]{2,50}$";  // Only letters and spaces
        return !name.isEmpty() && name.matches(regex);
    }

    // Regex for Phone Number: Simple phone number pattern (US-style)
    private boolean validateNumber(String number) {
        String regex = "^(?:\\+8801|01)[3-9]\\d{8}$"; // Matches numbers with optional + sign, and 10-13 digits
        return !number.isEmpty() && number.matches(regex);
    }

    // Regex for Email: Using Patterns.EMAIL_ADDRESS for basic email validation
    private boolean validateEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"; // Custom regex for email validation
        return !email.isEmpty() && email.matches(regex);
    }

    // Regex for Password: At least 6 characters, including one uppercase, one lowercase, one digit, and one special character
    private boolean validatePassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"; // Password strength
        return !password.isEmpty() && password.matches(regex);
    }
}