package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

public class AllCoursesActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    RecyclerView courseGroupRecyclerView;
    AllCoursesGroupAdapter allCourseGroupAdapter;
    ImageButton btnBack;
    ArrayList<Map<String, String>> courseGroups = new ArrayList<>();
    long departmentId=1;
    String selectedSession=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_courses);

        btnBack = findViewById(R.id.btn_back);
        courseGroupRecyclerView = findViewById(R.id.course_group_recycler_view);

        courseGroupRecyclerView.setLayoutManager(new LinearLayoutManager(AllCoursesActivity.this));
        allCourseGroupAdapter = new AllCoursesGroupAdapter(AllCoursesActivity.this, courseGroups, selectedSession);
        courseGroupRecyclerView.setAdapter(allCourseGroupAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getDepartmentId(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                if(data!=null) {
                    departmentId = (long) data;
                    getSelectedSession(new CompletionCallback() {
                        @Override
                        public void onCallback(Object data) {
                            if(data!=null) {
                                getAllCourses();
                            }
                        }
                    });
                }
            }
        });
    }

    void getAllCourses() {

        DatabaseReference coursesReference = rootReference.child("courses").child(String.valueOf(departmentId)).child(selectedSession);
        coursesReference.keepSynced(true);

        coursesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    if(courseGroups.size()>0) {
                        courseGroups.clear();
                    }

                    for(DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        for(DataSnapshot batchSnapshot : courseSnapshot.getChildren()) {
                            for(DataSnapshot keyValue : batchSnapshot.getChildren()) {

                                Map<String, String> courseData = new HashMap<>();
                                courseData.put("courseCode", courseSnapshot.getKey());
                                courseData.put("batch", batchSnapshot.getKey());
                                courseData.put("section", keyValue.getKey());

                                if(!String.valueOf(keyValue.getValue()).equals("")) {
                                    courseData.put("courseName", keyValue.getKey());
                                }

                                isGroupMember(courseSnapshot.getKey(), batchSnapshot.getKey(), keyValue.getKey(), new CompletionCallback() {
                                    @Override
                                    public void onCallback(Object data) {
                                        if(data!=null) {
                                            boolean isMember = (boolean) data;
                                            courseData.put("isMember", String.valueOf(isMember));
                                            courseGroups.add(courseData);
                                            allCourseGroupAdapter = new AllCoursesGroupAdapter(AllCoursesActivity.this, courseGroups, selectedSession);
                                            courseGroupRecyclerView.setAdapter(allCourseGroupAdapter);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getDepartmentId(CompletionCallback callback) {

        DatabaseReference depIdReference = rootReference.child("student").child(user.getUid()).child("departmentId");
        depIdReference.keepSynced(true);

        depIdReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().getValue()!=null) {
                    callback.onCallback(task.getResult().getValue());
                }
                else {

                    DatabaseReference depIdReference = rootReference.child("facultyMember").child(user.getUid()).child("departmentId");
                    depIdReference.keepSynced(true);

                    depIdReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful() && task.getResult().getValue()!=null) {
                                callback.onCallback(task.getResult().getValue());
                            }
                            else {
                                callback.onCallback(null);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onCallback(null);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(null);
            }
        });
    }

    void getSelectedSession(CompletionCallback callback) {

        DatabaseReference sessionReference = rootReference.child("selectedSessions").child(user.getUid());
        sessionReference.keepSynced(true);

        sessionReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()) {
                    selectedSession = String.valueOf(task.getResult().getValue());
                    callback.onCallback(true);
                }
                else {
                    callback.onCallback(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(null);
            }
        });
    }

    void isGroupMember(String courseCode, String batch, String section, CompletionCallback callback) {

        DatabaseReference memberReference = rootReference.child("myCourses").child(user.getUid())
                .child(selectedSession).child(courseCode).child(batch).child(section);
        memberReference.keepSynced(true);

        memberReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()) {
                    callback.onCallback(true);
                }
                else {
                    callback.onCallback(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(false);
            }
        });
    }
}