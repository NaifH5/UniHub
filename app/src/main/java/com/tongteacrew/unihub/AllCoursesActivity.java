package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class AllCoursesActivity extends AppCompatActivity {

    RecyclerView courseGroupRecyclerView;
    AllCoursesGroupAdapter allCourseGroupAdapter;
    ImageButton btnBack;
    ArrayList<ArrayList<String>> courseGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_courses);

        generateTestCourseGroups();

        btnBack = findViewById(R.id.btn_back);
        courseGroupRecyclerView = findViewById(R.id.course_group_recycler_view);

        courseGroupRecyclerView.setLayoutManager(new LinearLayoutManager(AllCoursesActivity.this));
        courseGroupRecyclerView.setHasFixedSize(true);
        allCourseGroupAdapter = new AllCoursesGroupAdapter(AllCoursesActivity.this, courseGroups);
        courseGroupRecyclerView.setAdapter(allCourseGroupAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void generateTestCourseGroups() {

        courseGroups = new ArrayList<>();

        ArrayList arrayList = new ArrayList();
        arrayList.add("CSE-3213");
        arrayList.add("Digital Signal Processing");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-3214");
        arrayList.add("Digital Signal Processing Sessional");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-3227");
        arrayList.add("Theory of Computation");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-3315");
        arrayList.add("Compiler Design and Construction");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-3316");
        arrayList.add("Compiler Design and Construction Sessional");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-3319");
        arrayList.add("Software Engineering and Information System Design");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-3320");
        arrayList.add("Software Engineering and Information System Design Sessional");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-4211");
        arrayList.add("Web Technologies");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-4212");
        arrayList.add("Web Technologies Sessional");
        arrayList.add("Batch 58");
        arrayList.add("Section A");
        courseGroups.add(arrayList);
    }
}