package com.tongteacrew.unihub;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class CoursesFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    RecyclerView courseGroupRecyclerView;
    CourseGroupAdapter courseGroupAdapter;
    ArrayList<Map<String, String>> courseGroups = new ArrayList<>();
    long departmentId=1;
    String selectedSession="";

    public CoursesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        courseGroupRecyclerView = view.findViewById(R.id.course_group_recycler_view);

        courseGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        courseGroupAdapter = new CourseGroupAdapter(getContext(), courseGroups);
        courseGroupRecyclerView.setAdapter(courseGroupAdapter);

        getDepartmentId(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                if(data!=null) {
                    departmentId = (long) data;
                    getSelectedSession(new CompletionCallback() {
                        @Override
                        public void onCallback(Object data) {
                            if(data!=null) {
                                getMyCourses();
                            }
                        }
                    });
                }
            }
        });

        return view;
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

    void getMyCourses() {

        DatabaseReference coursesReference = rootReference.child("myCourses").child(user.getUid()).child(selectedSession);
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

                                courseGroups.add(courseData);
                                courseGroupAdapter = new CourseGroupAdapter(getContext(), courseGroups);
                                courseGroupRecyclerView.setAdapter(courseGroupAdapter);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}