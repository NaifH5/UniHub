package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class GroupInfoActivity extends AppCompatActivity {

    ImageButton btnBack;
    RecyclerView teacherRecyclerView, crRecyclerView, studentsRecyclerView;
    GroupMembersAdapter groupMembersAdapter;
    ArrayList<ArrayList<String>> courseTeacher, classRepresentative, students;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        generateTestMembers();

        btnBack = findViewById(R.id.btn_back);
        teacherRecyclerView = findViewById(R.id.teacher_recycler_view);
        crRecyclerView = findViewById(R.id.cr_recycler_view);
        studentsRecyclerView = findViewById(R.id.students_recycler_view);

        teacherRecyclerView.setLayoutManager(new LinearLayoutManager(GroupInfoActivity.this));
        //teacherRecyclerView.setHasFixedSize(true);
        groupMembersAdapter = new GroupMembersAdapter(GroupInfoActivity.this, courseTeacher);
        teacherRecyclerView.setAdapter(groupMembersAdapter);

        crRecyclerView.setLayoutManager(new LinearLayoutManager(GroupInfoActivity.this));
        //crRecyclerView.setHasFixedSize(true);
        groupMembersAdapter = new GroupMembersAdapter(GroupInfoActivity.this, classRepresentative);
        crRecyclerView.setAdapter(groupMembersAdapter);

        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(GroupInfoActivity.this));
        //studentsRecyclerView.setHasFixedSize(true);
        groupMembersAdapter = new GroupMembersAdapter(GroupInfoActivity.this, students);
        studentsRecyclerView.setAdapter(groupMembersAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void generateTestMembers() {

        courseTeacher = new ArrayList<>();
        classRepresentative = new ArrayList<>();
        students = new ArrayList<>();

        ArrayList arrayList = new ArrayList();
        arrayList.add("https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg");
        arrayList.add("Lorem Ipsum Khan");
        arrayList.add("2132020000");
        courseTeacher.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("https://i.pinimg.com/736x/5a/ab/f8/5aabf84d67477f77d3bb8f0fe4cfcd17.jpg");
        arrayList.add("Lorem Ipsum Ahmed");
        arrayList.add("2132020000");
        classRepresentative.add(arrayList);

        for(int i=0; i<20; i++) {

            arrayList = new ArrayList();
            arrayList.add("https://i.pinimg.com/736x/5a/ab/f8/5aabf84d67477f77d3bb8f0fe4cfcd17.jpg");
            arrayList.add("Lorem Ipsum Ahmed");
            arrayList.add("2132020000");
            students.add(arrayList);
        }
    }
}