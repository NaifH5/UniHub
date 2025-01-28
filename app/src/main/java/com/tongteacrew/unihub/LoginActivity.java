package com.tongteacrew.unihub;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    RelativeLayout loginAndRegisterPanel;
    ProgressBar loginProgressBar;
    ImageView btnPasswordVisibilityInLogin;
    EditText emailInLogin, passwordInLogin;
    Button btnRegisterInLogin, btnLoginInLogin;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(isGranted) {
                    System.out.println("FCM SDK (and the app) can post notifications.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // To make light status bar background
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        loginAndRegisterPanel = findViewById(R.id.login_and_register_panel);
        loginProgressBar = findViewById(R.id.login_progress_bar);
        btnPasswordVisibilityInLogin = findViewById(R.id.btn_password_visibility_in_login);
        emailInLogin = findViewById(R.id.email_in_login);
        passwordInLogin = findViewById(R.id.password_in_login);
        btnLoginInLogin = findViewById(R.id.btn_add_to_my_clubs);
        btnRegisterInLogin = findViewById(R.id.btn_register_in_login);

        askNotificationPermission();

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
                login();
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

    private void askNotificationPermission() {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) {

            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)== PackageManager.PERMISSION_GRANTED) {
                System.out.println("FCM SDK (and your app) can post notifications.");
            }
            else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    void login() {

        String email = String.valueOf(emailInLogin.getText());
        String password = String.valueOf(passwordInLogin.getText());

        if(email.equals("") || password.equals("")) {
            Toast.makeText(LoginActivity.this, "Incorrect credentials!", Toast.LENGTH_SHORT).show();
            return;
        }

        loginAndRegisterPanel.setVisibility(View.GONE);
        loginProgressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                loginAndRegisterPanel.setVisibility(View.VISIBLE);
                loginProgressBar.setVisibility(View.GONE);

                if(task.isSuccessful()) {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    LoginActivity.this.startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Incorrect credentials!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Failed to login!", Toast.LENGTH_SHORT).show();
                loginAndRegisterPanel.setVisibility(View.VISIBLE);
                loginProgressBar.setVisibility(View.GONE);
            }
        });
    }
}