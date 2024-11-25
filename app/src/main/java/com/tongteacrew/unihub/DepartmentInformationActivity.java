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
        as.add("Lorem Ipsum Khan");
        as.add("Faculty Member");
        as.add("https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg");
        departmentMembers.add(as);

        as = new ArrayList<>();
        as.add("Lorem Ipsum Ahmed");
        as.add("Student");
        as.add("https://i.pinimg.com/736x/5a/ab/f8/5aabf84d67477f77d3bb8f0fe4cfcd17.jpg");
        departmentMembers.add(as);
    }
}