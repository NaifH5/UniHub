package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class LoginActivity extends AppCompatActivity {

    ImageView btnPasswordVisibilityInLogin;
    EditText passwordInLogin;
    Button btnRegisterInLogin, btnLoginInLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // To make light status bar background
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        btnPasswordVisibilityInLogin = findViewById(R.id.btn_password_visibility_in_login);
        passwordInLogin = findViewById(R.id.password_in_login);
        btnLoginInLogin = findViewById(R.id.btn_add_to_my_clubs);
        btnRegisterInLogin = findViewById(R.id.btn_register_in_login);

        btnPasswordVisibilityInLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(passwordInLogin.getTransformationMethod().getClass().getSimpleName().equals("PasswordTransformationMethod")) {
                    // To show password
                    passwordInLogin.setTransformationMethod(new SingleLineTransformationMethod());
                    btnPasswordVisibilityInLogin.setImageResource(R.drawable.icon_password_visibility_not_visible);
                }
                else {
                    // To hide password
                    passwordInLogin.setTransformationMethod(new PasswordTransformationMethod());
                    btnPasswordVisibilityInLogin.setImageResource(R.drawable.icon_password_visibility_visible);
                }

                // To move cursor to the end of the text
                passwordInLogin.setSelection(passwordInLogin.getText().length());
            }
        });

        btnLoginInLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });

        btnRegisterInLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, SelectAccountActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });
    }
}