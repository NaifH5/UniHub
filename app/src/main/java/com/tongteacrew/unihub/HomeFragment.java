package com.tongteacrew.unihub;

import static java.lang.Math.ceil;
import static java.lang.Math.max;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    LinearLayout scheduleLayout;
    RelativeLayout routineLayout;
    Spinner sessionSpinner;
    Button btnCourses, btnUnallocatedRooms;
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
    String selectedSession="";
    boolean expandedRoutine = false;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        scheduleRecyclerView = view.findViewById(R.id.schedule_recycler_view);
        btnCourses = view.findViewById(R.id.btn_courses);
        btnUnallocatedRooms = view.findViewById(R.id.btn_unallocated_rooms);
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

        btnExpandRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!expandedRoutine) {
                    routineLayout.post(() -> {
                        animateHeight(dpToPx(100), max(dpToPx(100), dpToPx(getLayoutHeight())));
                    });
                }
                else {
                    routineLayout.post(() -> {
                        animateHeight(max(dpToPx(100), dpToPx(getLayoutHeight())), dpToPx(100));
                    });
                }

                expandedRoutine = !expandedRoutine;
            }
        });

        btnUnallocatedRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UnallocatedRoomsActivity.class);
                getContext().startActivity(intent);
            }
        });

        scheduleRecyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                rv.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        getMyAccountType();
        return view;
    }

    void getMyAccountType() {

        DatabaseReference studentRef = rootReference.child("student").child(user.getUid());
        DatabaseReference facultyRef = rootReference.child("facultyMember").child(user.getUid());

        studentRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()) {
                    getMyDetails(task.getResult(), "student");
                }
                else {
                    facultyRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task2) {
                            if(task2.isSuccessful() && task2.getResult().exists()) {
                                getMyDetails(task2.getResult(), "facultyMember");
                            }
                        }
                    });
                }
            }
        });
    }

    void getMyDetails(DataSnapshot snapshot, String accountType) {

        if(snapshot.exists()) {
            myDetails = (Map<String, Object>) snapshot.getValue();
            myDetails.put("accountType", accountType);
            observeSessions();
        }
    }

    void observeSessions() {

        Query sessionReference = rootReference.child("sessions").child(String.valueOf(myDetails.get("departmentId"))).limitToLast(3);
        sessionReference.keepSynced(true);

        sessionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                sessions.clear();

                for(DataSnapshot s : snapshot.getChildren()) {
                    sessions.add(String.valueOf(s.getValue()));
                }

                Collections.reverse(sessions);
                updateSessionSpinner();
                observeSelectedSession();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void setSpinnerListener() {

        sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSelectedSession(sessions.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    void observeSelectedSession() {

        DatabaseReference sessionRef = rootReference.child("selectedSessions").child(user.getUid());
        sessionRef.keepSynced(true);

        sessionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    selectedSession = snapshot.getValue(String.class);
                    setSpinnerListener();
                }
                else if(!sessions.isEmpty()) {
                    selectedSession = sessions.get(0);
                    setSelectedSession(sessions.get(0));
                }

                updateSessionSpinnerSelection();
                getSchedule();

                if(String.valueOf(myDetails.get("accountType")).equals("student")) {
                    getStudentRoutine();
                }
                else {
                    getFacultyRoutine();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void setSelectedSession(String session) {
        rootReference.child("selectedSessions").child(user.getUid()).setValue(session).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                selectedSession = session;
                setSpinnerListener();
            }
        });
    }

    void updateSessionSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_sessions, sessions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessionSpinner.setAdapter(adapter);
    }

    void updateSessionSpinnerSelection() {
        if(selectedSession!=null && sessions.contains(selectedSession)) {
            sessionSpinner.setSelection(sessions.indexOf(selectedSession));
        }
    }

    void animateHeight(int startHeight, int endHeight) {

        if(startHeight<=endHeight) {
            btnExpandRoutine.setRotation(90);
        }
        else {
            btnExpandRoutine.setRotation(270);
        }

        ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight);
        valueAnimator.setDuration(100);

        valueAnimator.addUpdateListener(animation -> {

            int animatedValue = (int) animation.getAnimatedValue();

            if(startHeight<=endHeight) {
                gradientView.setAlpha(1-((float)(animatedValue-startHeight)/(endHeight-startHeight)));
            }
            else {
                gradientView.setAlpha(1-((float)(animatedValue-endHeight)/(startHeight-endHeight)));
            }

            ViewGroup.LayoutParams layoutParams = routineLayout.getLayoutParams();
            layoutParams.height = animatedValue;
            routineLayout.setLayoutParams(layoutParams);
        });

        valueAnimator.start();
    }

    int getLayoutHeight() {

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        int gridViewItemHeight=0, verticalSpacing=5, totalHeight=0;

        Map<String, GridView> gridViewMap = new HashMap<>();
        gridViewMap.put("Sunday", sundayGridview);
        gridViewMap.put("Monday", mondayGridview);
        gridViewMap.put("Tuesday", tuesdayGridview);
        gridViewMap.put("Wednesday", wednesdayGridview);
        gridViewMap.put("Thursday", thursdayGridview);
        gridViewMap.put("Friday", fridayGridview);
        gridViewMap.put("Saturday", saturdayGridview);

        for(String day : gridViewMap.keySet()) {
            if(routineList.containsKey(day)) {
                float density = gridViewMap.get(day).getContext().getResources().getDisplayMetrics().density;
                gridViewItemHeight = (int) (gridViewMap.get(day).getChildAt(0).getHeight()/density);
                break;
            }
        }

        for(int i=0; i<7; i++) {
            if(routineList.containsKey(days[i])) {
                int rowCount = (int)ceil((double)routineList.get(days[i]).size()/3.0);
                totalHeight += 50+rowCount*gridViewItemHeight+(rowCount-1)*verticalSpacing;
            }
        }

        return totalHeight+routineList.size();
    }

    int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp*density);
    }

    void getSchedule() {

        DatabaseReference scheduleReference = rootReference.child("myCourses").child(user.getUid()).child(selectedSession);
        scheduleReference.keepSynced(true);

        scheduleReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                schedules.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot courseIdSnapshot : snapshot.getChildren()) {
                        String courseCode = courseIdSnapshot.getKey();
                        for(DataSnapshot batchSnapshot : courseIdSnapshot.getChildren()) {
                            String batch = batchSnapshot.getKey();
                            for(DataSnapshot sectionSnapshot : batchSnapshot.getChildren()) {
                                String section = sectionSnapshot.getKey();
                                String courseGroupId = selectedSession+"_"+batch+"_"+section+"_"+courseCode;
                                fetchCourseAnnouncements(courseGroupId, courseCode);
                            }
                        }
                    }
                }

                updateScheduleVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void fetchCourseAnnouncements(String courseGroupId, String courseCode) {

        DatabaseReference announcementReference = rootReference.child("courseAnnouncements").child(courseGroupId);
        announcementReference.keepSynced(true);

        announcementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    parseAnnouncements(snapshot, courseGroupId, courseCode);
                }
                updateScheduleVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void parseAnnouncements(DataSnapshot snapshot, String courseGroupId, String courseCode) {

        for(DataSnapshot announcement : snapshot.getChildren()) {

            Map<String, Object> data = (Map<String, Object>) announcement.getValue();

            if(data!=null && data.containsKey("schedule")) {

                long schedule = (long) data.get("schedule");

                if(schedule>System.currentTimeMillis()) {

                    Map<String, Object> data2 = new HashMap<>();
                    data2.put("courseGroupId", courseGroupId);
                    data2.put("courseCode", courseCode);
                    data2.put("date", getDate(schedule));
                    data2.put("schedule", schedule);

                    if(data.containsKey("text")) {
                        data2.put("text", data.get("text"));
                    }

                    schedules.add(data2);

                    schedules.sort((o1, o2) -> {
                        return Long.compare((Long) o1.get("schedule"), (Long) o2.get("schedule"));
                    });

                }
            }
        }
        scheduleAdapter.notifyDataSetChanged();
    }

    void updateScheduleVisibility() {
        boolean hasSchedules = !schedules.isEmpty();
        emptySchedule.setVisibility(hasSchedules ? View.GONE : View.VISIBLE);
        scheduleLayout.setVisibility(hasSchedules ? View.VISIBLE : View.GONE);
    }

    void getFacultyRoutine() {

        DatabaseReference routineReference = rootReference.child("facultyRoutine").child(selectedSession).child(String.valueOf(myDetails.get("acronym")));
        routineReference.keepSynced(true);

        routineReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                routineReference.removeEventListener(this);

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
            public void onCancelled(@NonNull DatabaseError error) {
                routineReference.removeEventListener(this);
            }
        });
    }

    void getStudentRoutine() {

        DatabaseReference routineReference = rootReference.child("routine").child(String.valueOf(myDetails.get("departmentId")))
                .child(selectedSession).child(String.valueOf(myDetails.get("batchId"))).child(String.valueOf(myDetails.get("sectionId")));
        routineReference.keepSynced(true);

        routineReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                routineReference.removeEventListener(this);

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
            public void onCancelled(@NonNull DatabaseError error) {
                routineReference.removeEventListener(this);
            }
        });
    }

    void changeGridviewSize(ArrayList<Map<String, Object>> list, GridView gridView, String day) {

        if(list!=null && !list.isEmpty()) {

            list.sort((o1, o2) -> {
                return Long.compare(parseTime(String.valueOf(o1.get("title1"))), parseTime(String.valueOf(o2.get("title1"))));
            });

            gridView.setAdapter(new RoutineGridAdapter(getContext(), list));
        }

        Map<String, Pair<TextView, GridView>> dayViews = new HashMap<>();
        dayViews.put("Sunday", new Pair<>(sundayTextView, sundayGridview));
        dayViews.put("Monday", new Pair<>(mondayTextView, mondayGridview));
        dayViews.put("Tuesday", new Pair<>(tuesdayTextView, tuesdayGridview));
        dayViews.put("Wednesday", new Pair<>(wednesdayTextView, wednesdayGridview));
        dayViews.put("Thursday", new Pair<>(thursdayTextView, thursdayGridview));
        dayViews.put("Friday", new Pair<>(fridayTextView, fridayGridview));
        dayViews.put("Saturday", new Pair<>(saturdayTextView, saturdayGridview));

        Pair<TextView, GridView> views = dayViews.getOrDefault(day, null);

        if(views!=null) {
            int visibility = (list!=null && !list.isEmpty()) ? View.VISIBLE : View.GONE;
            views.first.setVisibility(visibility);
            views.second.setVisibility(visibility);
        }

        if(expandedRoutine && "Saturday".equals(day)) {
            routineLayout.post(() -> {
                animateHeight(dpToPx(100), Math.max(dpToPx(100), dpToPx(getLayoutHeight())));
            });
        }
    }

    String getDate(long time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(Instant.ofEpochMilli(time));
        return formattedDate;
    }

    long parseTime(String timeRange) {

        String startTime = timeRange.split("-")[0].trim().replaceAll("(?i)pm|am", "").trim();

        try {

            if(Pattern.compile("^\\d{1,2}:\\d{2}pm-\\d{1,2}:\\d{2}pm$").matcher(timeRange).find()) {
                startTime = startTime+" PM";
            }
            else if(Pattern.compile("^(0?8:[0-5][0-9]|0?9:[0-5][0-9]|10:[0-5][0-9]|11:[0-5][0-9])$").matcher(startTime).find()) {
                startTime = startTime+" AM";
            }
            else {
                startTime = startTime+" PM";
            }

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a"); // 12-hour format with AM/PM
            Date date = sdf.parse(startTime);
            return date.getTime()%(24*60*60*1000);
        }
        catch(Exception e) {
            return 0;
        }
    }
}