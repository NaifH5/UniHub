package com.tongteacrew.unihub;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    Button btnCourses;
    RecyclerView scheduleRecyclerView;
    ScheduleAdapter scheduleAdapter;
    ArrayList<ArrayList<String>> schedules;

    public HomeFragment() {
        generateTestSchedule();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        scheduleRecyclerView = view.findViewById(R.id.schedule_recycler_view);
        btnCourses = view.findViewById(R.id.btn_courses);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        scheduleRecyclerView.setLayoutManager(layoutManager);
        scheduleRecyclerView.setHasFixedSize(true);
        scheduleAdapter = new ScheduleAdapter(getContext(), schedules);
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        btnCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AllCoursesActivity.class);
                getContext().startActivity(intent);
            }
        });

        return view;
    }

    void generateTestSchedule() {

        schedules = new ArrayList<>();

        ArrayList arrayList = new ArrayList();
        arrayList.add("CSE-3319");
        arrayList.add("30 Nov 2024");
        arrayList.add("Lab report -2 of software engineering. Topic: DFD related everything . Also add 2 diagram with 0 and 1 level (diagram topic choose by yourself).");
        schedules.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-3213");
        arrayList.add("01 Dec 2024");
        arrayList.add("Dsp Tutorial:- 02. Topic:- upto last class.");
        schedules.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("CSE-4211");
        arrayList.add("04 Dec 2024");
        arrayList.add("Web technology tutorial- 2. Topic:- introduction to php & phd basic 1,2.");
        schedules.add(arrayList);
    }
}