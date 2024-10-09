package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class DepartmentInformationActivity extends AppCompatActivity {

    ImageButton btnBack;
    RecyclerView departmentMemberRecyclerView;
    DepartmentMembersAdapter departmentMemberAdapter;
    ArrayList<ArrayList<String>> departmentMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_information);

        generateTestMemberList();

        btnBack = findViewById(R.id.btn_back);
        departmentMemberRecyclerView = findViewById(R.id.users_recycler_view);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        departmentMemberRecyclerView.setLayoutManager(new LinearLayoutManager(DepartmentInformationActivity.this));
        departmentMemberRecyclerView.setHasFixedSize(true);
        departmentMemberAdapter = new DepartmentMembersAdapter(DepartmentInformationActivity.this, departmentMembers);
        departmentMemberRecyclerView.setAdapter(departmentMemberAdapter);
    }

    void generateTestMemberList() {

        ArrayList<String> as = new ArrayList<>();
        as.add("Naif Haider Chowdhury");
        as.add("Student");
        departmentMembers.add(as);

        as = new ArrayList<>();
        as.add("Shahriar Jahan Sunny");
        as.add("Student");
        departmentMembers.add(as);

        as = new ArrayList<>();
        as.add("Md. Ali Hossain Sagor");
        as.add("Student");
        departmentMembers.add(as);
    }
}