package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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

public class GroupInfoActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    ImageButton btnBack;
    RecyclerView teacherRecyclerView, crRecyclerView, studentsRecyclerView;
    TextView courseCode, batch, section;
    GroupMembersAdapter teacherAdapter, studentsAdapter, crAdapter;
    ArrayList<Map<String, Object>> courseTeacher = new ArrayList<>();
    ArrayList<Map<String, Object>> classRepresentative = new ArrayList<>();
    ArrayList<Map<String, Object>> students = new ArrayList<>();
    private String courseGroupId;
    String myAccountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        courseGroupId = (String) getIntent().getSerializableExtra("courseGroupId");
        myAccountType = (String) getIntent().getSerializableExtra("myAccountType");

        btnBack = findViewById(R.id.btn_back);
        courseCode = findViewById(R.id.course_code);
        batch = findViewById(R.id.batch);
        section = findViewById(R.id.section);
        teacherRecyclerView = findViewById(R.id.teacher_recycler_view);
        crRecyclerView = findViewById(R.id.cr_recycler_view);
        studentsRecyclerView = findViewById(R.id.students_recycler_view);

        String[] parts = courseGroupId.split("_");
        courseCode.setText(parts[3]);
        batch.setText(String.format("Batch %s", parts[1]));
        section.setText(String.format("Section %s", parts[2]));

        teacherRecyclerView.setLayoutManager(new LinearLayoutManager(GroupInfoActivity.this));
        teacherAdapter = new GroupMembersAdapter(GroupInfoActivity.this, courseTeacher, courseGroupId, myAccountType, new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                finish();
            }
        });
        teacherRecyclerView.setAdapter(teacherAdapter);

        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(GroupInfoActivity.this));
        studentsAdapter = new GroupMembersAdapter(GroupInfoActivity.this, students, courseGroupId, myAccountType, new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                finish();
            }
        });
        studentsRecyclerView.setAdapter(studentsAdapter);

        crRecyclerView.setLayoutManager(new LinearLayoutManager(GroupInfoActivity.this));
        crAdapter = new GroupMembersAdapter(GroupInfoActivity.this, classRepresentative, courseGroupId, myAccountType, new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                finish();
            }
        });
        crRecyclerView.setAdapter(crAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getCourseTeacher();
        getStudents();
        getCR();
    }

    public void getCourseTeacher() {

        DatabaseReference facultyReference = rootReference.child("courseGroupMembers").child(courseGroupId).child("facultyMember");
        facultyReference.keepSynced(true);

        facultyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(courseTeacher.size()>0) {
                    courseTeacher.clear();
                }

                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()) {

                        getProfile(s.getKey(), "facultyMember", new CompletionCallback() {
                            @Override
                            public void onCallback(Object data) {
                                Map<String, Object> userData = (Map<String, Object>) data;
                                userData.put("isCr", false);
                                courseTeacher.add(userData);
                                teacherAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
                else {
                    teacherAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void getStudents() {

        DatabaseReference studentReference = rootReference.child("courseGroupMembers").child(courseGroupId).child("student");
        studentReference.keepSynced(true);

        studentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(students.size()>0) {
                    students.clear();
                }

                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()) {

                        getProfile(s.getKey(), "student", new CompletionCallback() {
                            @Override
                            public void onCallback(Object data) {
                                Map<String, Object> userData = (Map<String, Object>) data;
                                userData.put("isCr", false);
                                students.add(userData);
                                studentsAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
                else {
                    studentsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void getCR() {

        DatabaseReference crReference = rootReference.child("courseGroupMembers").child(courseGroupId).child("classRepresentative");
        crReference.keepSynced(true);

        crReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(classRepresentative.size()>0) {
                    classRepresentative.clear();
                }

                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()) {

                        getProfile(s.getKey(), "student", new CompletionCallback() {
                            @Override
                            public void onCallback(Object data) {
                                Map<String, Object> userData = (Map<String, Object>) data;
                                userData.put("isCr", true);
                                classRepresentative.add(userData);
                                crAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
                else {
                    crAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getProfile(String id, String accountType, CompletionCallback callback) {

        DatabaseReference userReference = rootReference.child(accountType).child(id);
        userReference.keepSynced(true);

        userReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful() && task.getResult().exists()) {

                    Map<String, Object> userData = (Map<String, Object>) task.getResult().getValue();
                    Map<String, Object> filteredData = new HashMap<>();
                    filteredData.put("fullName", userData.get("fullName"));
                    filteredData.put("accountType", accountType);
                    filteredData.put("id", userData.get("id"));
                    filteredData.put("uid", id);

                    if(userData.containsKey("batchId")) {
                        filteredData.put("batch", userData.get("batchId"));
                    }

                    if(userData.containsKey("sectionId")) {
                        filteredData.put("section", userData.get("sectionId"));
                    }

                    if(userData.containsKey("profilePicture")) {
                        filteredData.put("profilePicture", userData.get("profilePicture"));
                    }

                    callback.onCallback(filteredData);
                }
            }
        });
    }
}