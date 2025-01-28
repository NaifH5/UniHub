package com.tongteacrew.unihub;

import static java.lang.Math.max;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class HomeFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    Spinner sessionSpinner;
    Button btnCourses;
    RecyclerView scheduleRecyclerView;
    ScheduleAdapter scheduleAdapter;
    ArrayList<ArrayList<String>> schedules;
    ArrayList<String> sessions = new ArrayList<>();
    long departmentId=1;
    String selectedSession="";

    public HomeFragment() {
        generateTestSchedule();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        scheduleRecyclerView = view.findViewById(R.id.schedule_recycler_view);
        btnCourses = view.findViewById(R.id.btn_courses);
        sessionSpinner = view.findViewById(R.id.spinner_session);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        scheduleRecyclerView.setLayoutManager(layoutManager);
        scheduleAdapter = new ScheduleAdapter(getContext(), schedules);
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        btnCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AllCoursesActivity.class);
                getContext().startActivity(intent);
            }
        });

        getDepartmentId(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                if(data!=null) {
                    departmentId = (long) data;
                    getSessionList(new CompletionCallback() {
                        @Override
                        public void onCallback(Object data) {
                            getSelectedSession(new CompletionCallback() {
                                @Override
                                public void onCallback(Object data) {
                                    if(data!=null) {
                                        System.out.println("Done!");
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSelectedSession(sessions.get(position), new CompletionCallback() {
                    @Override
                    public void onCallback(Object data) {
                        System.out.println("Session updated!");
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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

    void getSessionList(CompletionCallback callback) {

        Query sessionReference = rootReference.child("sessions").child(String.valueOf(departmentId)).limitToLast(3);
        sessionReference.keepSynced(true);

        sessionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                sessionReference.removeEventListener(this);

                if(snapshot.exists()) {

                    if(sessions.size()>0) {
                        sessions.clear();
                    }

                    for(DataSnapshot s : snapshot.getChildren()) {
                        sessions.add(String.valueOf(s.getValue()));
                    }

                    Collections.reverse(sessions);
                    ArrayAdapter sessionsArrayAdapter = new ArrayAdapter(getContext(), R.layout.spinner_sessions, sessions);
                    sessionsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sessionSpinner.setAdapter(sessionsArrayAdapter);
                }

                callback.onCallback(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                sessionReference.removeEventListener(this);
                callback.onCallback(true);
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
                    sessionSpinner.setSelection(max(0, sessions.indexOf(selectedSession)));
                    callback.onCallback(true);
                }
                else if(sessions.size()>0) {
                    setSelectedSession(sessions.get(0), callback);
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

    void setSelectedSession(String session, CompletionCallback callback) {

        DatabaseReference sessionReference = rootReference.child("selectedSessions").child(user.getUid());

        sessionReference.setValue(session).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                selectedSession = session;
                callback.onCallback(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                selectedSession = session;
                callback.onCallback(true);
            }
        });
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