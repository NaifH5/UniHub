package com.tongteacrew.unihub;

import static java.util.Collections.swap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UnallocatedRoomsActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    ImageButton btnBack;
    GridView sundayGridview, mondayGridview, tuesdayGridview, wednesdayGridview, thursdayGridview, fridayGridview, saturdayGridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unallocated_rooms);

        btnBack = findViewById(R.id.btn_back);
        sundayGridview = findViewById(R.id.sunday_gridview);
        mondayGridview = findViewById(R.id.monday_gridview);
        tuesdayGridview = findViewById(R.id.tuesday_gridview);
        wednesdayGridview = findViewById(R.id.wednesday_gridview);
        thursdayGridview = findViewById(R.id.thursday_gridview);
        fridayGridview = findViewById(R.id.friday_gridview);
        saturdayGridview = findViewById(R.id.saturday_gridview);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSelectedSession();
    }

    public void getSelectedSession() {

        DatabaseReference sessionRef = rootReference.child("selectedSessions").child(user.getUid());
        sessionRef.keepSynced(true);

        sessionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    getAllRooms(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void getAllRooms(String selectedSession) {

        DatabaseReference allRoomsReference = rootReference.child("allRooms").child(selectedSession);
        allRoomsReference.keepSynced(true);

        allRoomsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    ArrayList<String> allRooms = new ArrayList<>();
                    for(DataSnapshot s : snapshot.getChildren()) {
                        allRooms.add(s.getKey());
                    }
                    getAllocatedRooms(selectedSession, allRooms);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void getAllocatedRooms(String selectedSession, ArrayList<String> allRooms) {

        DatabaseReference allocateedRoomsReference = rootReference.child("allocatedRooms").child(selectedSession);
        allocateedRoomsReference.keepSynced(true);

        allocateedRoomsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    HashMap<String, ArrayList<Map<String, Object>>> unallocatedRooms = new HashMap<>();

                    for(DataSnapshot daySnapshot : snapshot.getChildren()) {

                        ArrayList<Map<String, Object>> timesList = new ArrayList();

                        for(DataSnapshot timeSnapshot : daySnapshot.getChildren()) {

                            ArrayList<String> allocatedRoomsList = new ArrayList<>();
                            ArrayList<String> unallocatedRoomsList = new ArrayList<>();

                            for(DataSnapshot roomSnapshot : timeSnapshot.getChildren()) {
                                allocatedRoomsList.add(roomSnapshot.getKey());
                            }

                            for(int i=0; i<allRooms.size(); i++) {
                                if(!allocatedRoomsList.contains(allRooms.get(i))) {
                                    unallocatedRoomsList.add(allRooms.get(i));
                                }
                            }

                            Map<String, Object> time = new HashMap<>();
                            time.put(timeSnapshot.getKey(), unallocatedRoomsList);
                            timesList.add(time);
                        }

                        unallocatedRooms.put(daySnapshot.getKey(), timesList);
                    }

                    sortAndSetAdapter(sundayGridview, unallocatedRooms.get("Sunday"));
                    sortAndSetAdapter(mondayGridview, unallocatedRooms.get("Monday"));
                    sortAndSetAdapter(tuesdayGridview, unallocatedRooms.get("Tuesday"));
                    sortAndSetAdapter(wednesdayGridview, unallocatedRooms.get("Wednesday"));
                    sortAndSetAdapter(thursdayGridview, unallocatedRooms.get("Thursday"));
                    sortAndSetAdapter(fridayGridview, unallocatedRooms.get("Friday"));
                    sortAndSetAdapter(saturdayGridview, unallocatedRooms.get("Saturday"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void sortAndSetAdapter(GridView gridView, ArrayList<Map<String, Object>> list) {

        list.sort((o1, o2) -> {
            return Long.compare(parseTime(o1.keySet().iterator().next()), parseTime(o2.keySet().iterator().next()));
        });

        gridView.setAdapter(new UnallocatedRoomsGridAdapter(UnallocatedRoomsActivity.this, list, gridView));
    }

    public long parseTime(String timeRange) {

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