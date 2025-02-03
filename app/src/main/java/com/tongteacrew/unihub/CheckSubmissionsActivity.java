package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckSubmissionsActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    ImageButton btnBack;
    RecyclerView submissionsRecyclerView;
    SubmissionsAdapter submissionsAdapter;
    ArrayList<Map<String, Object>> submissions = new ArrayList<>();
    String courseGroupId, announcementId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_submissions);

        courseGroupId = (String) getIntent().getSerializableExtra("courseGroupId");
        announcementId = (String) getIntent().getSerializableExtra("announcementId");

        btnBack = findViewById(R.id.btn_back);
        submissionsRecyclerView = findViewById(R.id.submissions_recycler_view);

        submissionsRecyclerView.setLayoutManager(new LinearLayoutManager(CheckSubmissionsActivity.this));
        submissionsAdapter = new SubmissionsAdapter(CheckSubmissionsActivity.this, submissions);
        submissionsRecyclerView.setAdapter(submissionsAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSubmissions();
    }

    void getSubmissions() {

        DatabaseReference assignmentsReference = rootReference.child("assignments").child(courseGroupId).child(announcementId);
        assignmentsReference.keepSynced(true);

        assignmentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                assignmentsReference.removeEventListener(this);

                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()) {

                        Map<String, Object> data = (Map<String, Object>) s.getValue();
                        Map<String, Object> assignmentData = new HashMap<>();
                        assignmentData.put("time", data.get("time"));
                        assignmentData.put("posterId", s.getKey());

                        DatabaseReference userReference = rootReference.child("student").child(s.getKey());
                        userReference.keepSynced(true);

                        userReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {

                                if(task.isSuccessful() && task.getResult().exists()) {

                                    Map<String, Object> data = (Map<String, Object>) task.getResult().getValue();
                                    assignmentData.put("fullName", data.get("fullName"));
                                    assignmentData.put("id", data.get("id"));

                                    DatabaseReference assignmentsReference = rootReference.child("assignmentMedia")
                                            .child(courseGroupId).child(announcementId).child(task.getResult().getKey())
                                            .child("file");
                                    assignmentsReference.keepSynced(true);

                                    assignmentsReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {

                                            if(task.isSuccessful() && task.getResult().exists()) {
                                                assignmentData.put("url", task.getResult().getValue());
                                            }

                                            submissions.add(assignmentData);

                                            if(submissions.size()==snapshot.getChildrenCount()) {
                                                submissionsAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                assignmentsReference.removeEventListener(this);
            }
        });
    }
}