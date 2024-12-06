package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class CheckSubmissionsActivity extends AppCompatActivity {

    ImageButton btnBack;
    RecyclerView submissionsRecyclerView;
    SubmissionsAdapter submissionsAdapter;
    ArrayList<ArrayList<String>> submissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_submissions);

        generateTestSubmissions();

        btnBack = findViewById(R.id.btn_back);
        submissionsRecyclerView = findViewById(R.id.submissions_recycler_view);

        submissionsRecyclerView.setLayoutManager(new LinearLayoutManager(CheckSubmissionsActivity.this));
        submissionsRecyclerView.setHasFixedSize(true);
        submissionsAdapter = new SubmissionsAdapter(CheckSubmissionsActivity.this, submissions);
        submissionsRecyclerView.setAdapter(submissionsAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void generateTestSubmissions() {

        submissions = new ArrayList<>();

        ArrayList arrayList = new ArrayList();
        arrayList.add("https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg");
        arrayList.add("Lorem Ipsum Ahmed");
        arrayList.add("2132020000");
        arrayList.add("06:00 pm  05 Dec 2024");
        submissions.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg");
        arrayList.add("Lorem Ipsum Ahmed");
        arrayList.add("2132020000");
        arrayList.add("06:00 pm  05 Dec 2024");
        submissions.add(arrayList);
    }
}