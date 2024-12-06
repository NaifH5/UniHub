package com.tongteacrew.unihub;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CoursesFragment extends Fragment {

    RecyclerView courseGroupRecyclerView;
    CourseGroupAdapter courseGroupAdapter;
    ArrayList<ArrayList<String>> courseGroups;

    public CoursesFragment() {
        generateTestCourseGroups();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        courseGroupRecyclerView = view.findViewById(R.id.course_group_recycler_view);

        courseGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        courseGroupRecyclerView.setHasFixedSize(true);
        courseGroupAdapter = new CourseGroupAdapter(getContext(), courseGroups);
        courseGroupRecyclerView.setAdapter(courseGroupAdapter);

        return view;
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