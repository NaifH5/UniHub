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
    String myAccountType="student";

    public CoursesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        courseGroupRecyclerView = view.findViewById(R.id.course_group_recycler_view);

        courseGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        courseGroupAdapter = new CourseGroupAdapter(getContext(), courseGroups, selectedSession);
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
                                courseGroupAdapter = new CourseGroupAdapter(getContext(), courseGroups, selectedSession);
                                courseGroupRecyclerView.setAdapter(courseGroupAdapter);
                            }
                        }
                    }
                }
                else {
                    addToMyCourses();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void addToMyCourses() {

        getMyAccountType(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {

                System.out.println(myAccountType);
                Map<String, String> map = (Map<String, String>) data;

                if(myAccountType.equals("student")) {

                    DatabaseReference accountTypeReference = rootReference.child("routine")
                            .child(String.valueOf(map.get("departmentId")))
                            .child(selectedSession)
                            .child(String.valueOf(map.get("batchId")));

                    accountTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(!snapshot.exists()) return;
                            HashMap<String, Object> updates = new HashMap<>();

                            for(DataSnapshot sectionSnapshot : snapshot.getChildren()) {

                                if(!sectionSnapshot.getKey().contains(String.valueOf(map.get("sectionId")))) continue;

                                for(DataSnapshot daySnapshot : sectionSnapshot.getChildren()) {
                                    for(DataSnapshot timeSnapshot : daySnapshot.getChildren()) {

                                        Map<String, String> map2 = (Map<String, String>) timeSnapshot.getValue();
                                        if(map2==null || !map2.containsKey("courseCode")) continue;

                                        String courseCode = map2.get("courseCode");
                                        String courseName = map2.getOrDefault("courseName", "");

                                        String routinePath = "myCourses/"+map.get("id")+"/"+selectedSession+"/"+courseCode+"/"+String.valueOf(map.get("batchId"))+"/"+sectionSnapshot.getKey();
                                        updates.put(routinePath, courseName);

                                        String groupId = selectedSession+"_"+String.valueOf(map.get("batchId"))+"_"+map.get("sectionId")+"_"+courseCode;
                                        String groupMemberPath = "courseGroupMembers/"+groupId+"/"+myAccountType+"/"+user.getUid();
                                        updates.put(groupMemberPath, true);
                                    }
                                }
                            }

                            rootReference.updateChildren(updates).addOnCompleteListener(task -> {
                                if(task.isSuccessful()) {
                                    System.out.println("All updates completed successfully!");
                                }
                                else {
                                    System.out.println("Failed! " + task.getException());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            System.out.println("Database error: " + error.getMessage());
                        }
                    });
                }
                else {

                    DatabaseReference accountTypeReference = rootReference.child("facultyRoutine")
                            .child(selectedSession).child(String.valueOf(map.get("acronym")));

                    accountTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(!snapshot.exists()) return;
                            Map<String, Object> updates = new HashMap<>();
                            HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> routineData = new HashMap<>();

                            for(DataSnapshot daySnapshot : snapshot.getChildren()) {
                                for(DataSnapshot timeSnapshot : daySnapshot.getChildren()) {

                                    Map<String, String> map2 = (Map<String, String>) timeSnapshot.getValue();
                                    if(map2 == null) continue;

                                    String courseCode = String.valueOf(map2.get("courseCode"));
                                    String batch = String.valueOf(map2.get("batch"));
                                    String section = String.valueOf(map2.get("section"));
                                    String courseName = map2.getOrDefault("courseName", "");

                                    routineData
                                            .computeIfAbsent(selectedSession, k -> new HashMap<>())
                                            .computeIfAbsent(courseCode, k -> new HashMap<>())
                                            .computeIfAbsent(batch, k -> new HashMap<>())
                                            .put(section, courseName);

                                    String groupId = selectedSession+"_"+batch+"_"+section+"_"+courseCode;
                                    updates.put("courseGroupMembers/"+groupId+"/"+myAccountType+"/"+user.getUid(), true);
                                }
                            }

                            updates.put("myCourses/"+map.get("id"), routineData);

                            rootReference.updateChildren(updates).addOnCompleteListener(task -> {
                                if(task.isSuccessful()) {
                                    System.out.println("All updates completed successfully!");
                                }
                                else {
                                    System.out.println("Failed! " + task.getException());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            System.out.println("Database Error: " + error.getMessage());
                        }
                    });

                }
            }
        });
    }

    void getMyAccountType(CompletionCallback callback) {

        DatabaseReference accountTypeReference = rootReference.child("student").child(user.getUid());

        accountTypeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful() && task.getResult().exists()) {

                    myAccountType="student";
                    Map<String, String> map = (Map<String, String>) task.getResult().getValue();
                    map.put("id", task.getResult().getKey());
                    callback.onCallback(map);
                }
                else {

                    DatabaseReference accountTypeReference = rootReference.child("facultyMember").child(user.getUid());

                    accountTypeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.getResult().exists()) {
                                myAccountType="facultyMember";
                                Map<String, String> map = (Map<String, String>) task.getResult().getValue();
                                map.put("id", task.getResult().getKey());
                                callback.onCallback(map);
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
}