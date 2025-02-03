package com.tongteacrew.unihub;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    LinearLayout scheduleLayout;
    RelativeLayout routineLayout;
    Spinner sessionSpinner;
    Button btnCourses;
    ImageButton btnExpandRoutine;
    TextView emptySchedule, sundayTextView, mondayTextView, tuesdayTextView, wednesdayTextView, thursdayTextView, fridayTextView, saturdayTextView;
    View gradientView;
    RecyclerView scheduleRecyclerView;
    ScheduleAdapter scheduleAdapter;
    GridView sundayGridview, mondayGridview, tuesdayGridview, wednesdayGridview, thursdayGridview, fridayGridview, saturdayGridview;
    ArrayList<Map<String, Object>> schedules = new ArrayList<>();
    Map<String, ArrayList<Map<String, Object>>> routineList = new HashMap<>();
    ArrayList<String> sessions = new ArrayList<>();
    Map<String, Object> myDetails = new HashMap<>();
    long departmentId=1;
    String selectedSession="";
    boolean expandedRoutine = false;
    int maxRoutineHeight=0;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        scheduleRecyclerView = view.findViewById(R.id.schedule_recycler_view);
        btnCourses = view.findViewById(R.id.btn_courses);
        sessionSpinner = view.findViewById(R.id.spinner_session);
        emptySchedule = view.findViewById(R.id.empty_schedule);
        scheduleLayout = view.findViewById(R.id.linearLayout10);
        sundayGridview = view.findViewById(R.id.sunday_gridview);
        mondayGridview = view.findViewById(R.id.monday_gridview);
        tuesdayGridview = view.findViewById(R.id.tuesday_gridview);
        wednesdayGridview = view.findViewById(R.id.wednesday_gridview);
        thursdayGridview = view.findViewById(R.id.thursday_gridview);
        fridayGridview = view.findViewById(R.id.friday_gridview);
        saturdayGridview = view.findViewById(R.id.saturday_gridview);
        btnExpandRoutine = view.findViewById(R.id.btn_expand_routine);
        routineLayout = view.findViewById(R.id.relativeLayout6);
        sundayTextView = view.findViewById(R.id.sunday_text_view);
        mondayTextView = view.findViewById(R.id.monday_text_view);
        tuesdayTextView = view.findViewById(R.id.tuesday_text_view);
        wednesdayTextView = view.findViewById(R.id.wednesday_text_view);
        thursdayTextView = view.findViewById(R.id.thursday_text_view);
        fridayTextView = view.findViewById(R.id.friday_text_view);
        saturdayTextView = view.findViewById(R.id.saturday_text_view);
        gradientView = view.findViewById(R.id.view2);

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

        btnExpandRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!expandedRoutine) {
                    ExpandRoutine(300);
                }
                else {
                    collapseRoutine(300);
                }

                expandedRoutine = !expandedRoutine;
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

                                        getSchedule();

                                        if(String.valueOf(myDetails.get("accountType")).equals("student")) {
                                            getStudentRoutine();
                                        }
                                        else {
                                            getFacultyRoutine();
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        return view;
    }

    void ExpandRoutine(int animDuration) {
        int startingHeight = routineLayout.getHeight();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) routineLayout.getLayoutParams();
        params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        routineLayout.setLayoutParams(params);
        btnExpandRoutine.setRotation(90);
        animateHeightChange(routineLayout, startingHeight, maxRoutineHeight, animDuration);
    }

    void collapseRoutine(int animDuration) {
        int startingHeight = routineLayout.getHeight();
        int heightInPixels = (int) (100*getResources().getDisplayMetrics().density);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) routineLayout.getLayoutParams();
        params.height = heightInPixels;
        routineLayout.setLayoutParams(params);
        btnExpandRoutine.setRotation(270);
        animateHeightChange(routineLayout, startingHeight, heightInPixels, animDuration);
    }

    private void animateHeightChange(final View view, int startHeight, int endHeight, int animDuration) {

        ValueAnimator anim = ValueAnimator.ofInt(startHeight, endHeight);
        anim.setDuration(animDuration);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {

                int animatedValue = (int) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                params.height = animatedValue;
                view.setLayoutParams(params);
                float percentage = (float) ((animatedValue-startHeight))/(endHeight-startHeight);

                if(endHeight>=startHeight) {
                    float alpha = (float) max(0.5, 1.0-percentage);
                    gradientView.setAlpha(alpha);
                }
                else {
                    gradientView.setAlpha(percentage);
                }
            }
        });

        anim.start();
    }

    void getSchedule() {

        DatabaseReference scheduleReference = rootReference.child("myCourses").child(user.getUid()).child(selectedSession);
        scheduleReference.keepSynced(true);

        scheduleReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    emptySchedule.setVisibility(View.GONE);
                    scheduleLayout.setVisibility(View.VISIBLE);

                    for(DataSnapshot courseIdSnapshot : snapshot.getChildren()) {
                        String courseCode = courseIdSnapshot.getKey();
                        for(DataSnapshot batchSnapshot : courseIdSnapshot.getChildren()) {
                            String batch = batchSnapshot.getKey();
                            for(DataSnapshot sectionSnapshot : batchSnapshot.getChildren()) {
                                String section = sectionSnapshot.getKey();
                                String courseGroupId = selectedSession + "_" + batch + "_" + section + "_" + courseCode;
                                fetchCourseAnnouncements(courseGroupId, courseCode);
                            }
                        }
                    }
                }
                else {
                    emptySchedule.setVisibility(View.VISIBLE);
                    scheduleLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchCourseAnnouncements(String courseGroupId, String courseCode) {

        DatabaseReference announcementReference = rootReference.child("courseAnnouncements").child(courseGroupId);
        announcementReference.keepSynced(true);

        announcementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    emptySchedule.setVisibility(View.GONE);
                    scheduleLayout.setVisibility(View.VISIBLE);
                    parseAnnouncements(snapshot, courseGroupId, courseCode);
                    scheduleAdapter.notifyDataSetChanged();
                }
                else if(schedules.size()==0) {

                    emptySchedule.setVisibility(View.VISIBLE);
                    scheduleLayout.setVisibility(View.GONE);

                    if(schedules.size()>0) {
                        schedules.clear();
                    }

                    scheduleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void parseAnnouncements(DataSnapshot snapshot, String courseGroupId, String courseCode) {

        if(schedules.size()>0) {
            schedules.clear();
        }

        for(DataSnapshot announcement : snapshot.getChildren()) {

            Map<String, Object> data = (Map<String, Object>) announcement.getValue();

            if(data!=null && data.containsKey("schedule")) {

                long schedule = (long) data.get("schedule");

                if(schedule>System.currentTimeMillis()) {

                    Map<String, Object> data2 = new HashMap<>();
                    data2.put("courseGroupId", courseGroupId);
                    data2.put("courseCode", courseCode);
                    data2.put("date", getDate(schedule));

                    if(data.containsKey("text")) {
                        data2.put("text", data.get("text"));
                    }

                    schedules.add(data2);
                }
            }
        }
    }

    void getFacultyRoutine() {

        String acronym = String.valueOf(myDetails.get("acronym"));
        DatabaseReference routineReference = rootReference.child("facultyRoutine").child(selectedSession).child(acronym);
        routineReference.keepSynced(true);

        routineReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    if(routineList.size()>0) {
                        routineList.clear();
                    }

                    for(DataSnapshot daySnapshot : snapshot.getChildren()) {

                        ArrayList<Map<String, Object>> list = new ArrayList<>();

                        for(DataSnapshot timeSnapshot : daySnapshot.getChildren()) {

                            Map<String, Object> result = (Map<String, Object>) timeSnapshot.getValue();
                            Map<String, Object> data = new HashMap<>();
                            data.put("title1", timeSnapshot.getKey());
                            data.put("title2", result.get("courseCode"));
                            data.put("title3", result.get("batch"));
                            data.put("title4", result.get("section"));
                            data.put("title5", result.get("room"));
                            list.add(data);
                        }

                        routineList.put(daySnapshot.getKey(), list);
                    }

                    changeGridviewSize(routineList.get("Sunday"), sundayGridview, "Sunday");
                    changeGridviewSize(routineList.get("Monday"), mondayGridview, "Monday");
                    changeGridviewSize(routineList.get("Tuesday"), tuesdayGridview, "Tuesday");
                    changeGridviewSize(routineList.get("Wednesday"), wednesdayGridview, "Wednesday");
                    changeGridviewSize(routineList.get("Thursday"), thursdayGridview, "Thursday");
                    changeGridviewSize(routineList.get("Friday"), fridayGridview, "Friday");
                    changeGridviewSize(routineList.get("Saturday"), saturdayGridview, "Saturday");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getStudentRoutine() {

        DatabaseReference routineReference = rootReference.child("routine").child(String.valueOf(departmentId))
                .child(selectedSession).child(String.valueOf(myDetails.get("batchId"))).child(String.valueOf(myDetails.get("sectionId")));
        routineReference.keepSynced(true);

        routineReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    if(routineList.size()>0) {
                        routineList.clear();
                    }

                    for(DataSnapshot daySnapshot : snapshot.getChildren()) {

                        ArrayList<Map<String, Object>> list = new ArrayList<>();

                        for(DataSnapshot timeSnapshot : daySnapshot.getChildren()) {

                            Map<String, Object> result = (Map<String, Object>) timeSnapshot.getValue();
                            Map<String, Object> data = new HashMap<>();
                            data.put("title1", timeSnapshot.getKey());
                            data.put("title2", result.get("courseCode"));
                            data.put("title3", result.get("acronym"));
                            data.put("title4", result.get("room"));
                            list.add(data);
                        }

                        routineList.put(daySnapshot.getKey(), list);
                    }

                    changeGridviewSize(routineList.get("Sunday"), sundayGridview, "Sunday");
                    changeGridviewSize(routineList.get("Monday"), mondayGridview, "Monday");
                    changeGridviewSize(routineList.get("Tuesday"), tuesdayGridview, "Tuesday");
                    changeGridviewSize(routineList.get("Wednesday"), wednesdayGridview, "Wednesday");
                    changeGridviewSize(routineList.get("Thursday"), thursdayGridview, "Thursday");
                    changeGridviewSize(routineList.get("Friday"), fridayGridview, "Friday");
                    changeGridviewSize(routineList.get("Saturday"), saturdayGridview, "Saturday");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void changeGridviewSize(ArrayList<Map<String, Object>> list, GridView gridview, String day) {

        if(list!=null) {

            RoutineGridAdapter adapter = new RoutineGridAdapter(getContext(), list);
            gridview.setAdapter(adapter);

            if(list.size()>0) {

                gridview.post(new Runnable() {
                    @Override
                    public void run() {

                        int numRows = list.size()/3+min(1, list.size()%3);
                        int firstItemHeight = gridview.getChildAt(0).getHeight();
                        int space = gridview.getVerticalSpacing();
                        int totalHeight = (numRows*firstItemHeight)+((numRows-1)*space);

                        ViewGroup.LayoutParams params = gridview.getLayoutParams();
                        params.height = totalHeight;
                        gridview.setLayoutParams(params);
                        gridview.requestLayout();

                        RoutineGridAdapter adapter = new RoutineGridAdapter(getContext(), list);
                        gridview.setAdapter(adapter);

                        gridview.post(new Runnable() {
                            @Override
                            public void run() {
                                maxRoutineHeight = routineLayout.getHeight();
                                collapseRoutine(0);
                            }
                        });
                    }
                });
            }

            if(day.equals("Sunday")) sundayTextView.setVisibility(View.VISIBLE);
            else if(day.equals("Monday")) mondayTextView.setVisibility(View.VISIBLE);
            else if(day.equals("Tuesday")) tuesdayTextView.setVisibility(View.VISIBLE);
            else if(day.equals("Wednesday")) wednesdayTextView.setVisibility(View.VISIBLE);
            else if(day.equals("Thursday")) thursdayTextView.setVisibility(View.VISIBLE);
            else if(day.equals("Friday")) fridayTextView.setVisibility(View.VISIBLE);
            else if(day.equals("Saturday")) saturdayTextView.setVisibility(View.VISIBLE);
        }
        else {
            if(day.equals("Sunday")) sundayTextView.setVisibility(View.GONE);
            else if(day.equals("Monday")) mondayTextView.setVisibility(View.GONE);
            else if(day.equals("Tuesday")) tuesdayTextView.setVisibility(View.GONE);
            else if(day.equals("Wednesday")) wednesdayTextView.setVisibility(View.GONE);
            else if(day.equals("Thursday")) thursdayTextView.setVisibility(View.GONE);
            else if(day.equals("Friday")) fridayTextView.setVisibility(View.GONE);
            else if(day.equals("Saturday")) saturdayTextView.setVisibility(View.GONE);
        }
    }

    void getDepartmentId(CompletionCallback callback) {

        DatabaseReference depIdReference = rootReference.child("student").child(user.getUid());
        depIdReference.keepSynced(true);

        depIdReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().getValue()!=null) {
                    myDetails = (Map<String, Object>) task.getResult().getValue();
                    myDetails.put("accountType", "student");
                    callback.onCallback(myDetails.get("departmentId"));
                }
                else {

                    DatabaseReference depIdReference = rootReference.child("facultyMember").child(user.getUid());
                    depIdReference.keepSynced(true);

                    depIdReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful() && task.getResult().getValue()!=null) {
                                myDetails = (Map<String, Object>) task.getResult().getValue();
                                myDetails.put("accountType", "facultyMember");
                                callback.onCallback(myDetails.get("departmentId"));
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

    String getDate(long time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(Instant.ofEpochMilli(time));
        return formattedDate;
    }
}