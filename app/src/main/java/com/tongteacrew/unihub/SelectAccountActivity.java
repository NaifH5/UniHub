package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

public class SelectAccountActivity extends AppCompatActivity {

    Button btn_student, btn_faculty_member;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_account);

        // To make dark status bar background
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkShade4));
        window.getDecorView().setSystemUiVisibility(0);

        btnBack = findViewById(R.id.btn_back);
        btn_student = findViewById(R.id.btn_student);
        btn_faculty_member = findViewById(R.id.btn_faculty_member);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Moves to the registration activity for students.
        btn_student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectAccountActivity.this, RegisterAuthInfoActivity.class);
                intent.putExtra("account_type", "student");
                SelectAccountActivity.this.startActivity(intent);
            }
        });

        // Moves to the registration activity for faculty members.
        btn_faculty_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectAccountActivity.this, RegisterAuthInfoActivity.class);
                intent.putExtra("account_type", "faculty_member");
                SelectAccountActivity.this.startActivity(intent);
            }
        });
    }
}