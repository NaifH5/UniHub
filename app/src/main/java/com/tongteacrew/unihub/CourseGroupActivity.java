package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.Map;

public class CourseGroupActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    RecyclerView courseGroupAnnouncementRecyclerView;
    CourseGroupAnnouncementAdapter courseGroupAnnouncementAdapter;
    TextView courseCodeTextView, courseNameTextView, emptyText;
    ImageButton btnBack;
    Button btnAddAnnouncement;
    LinearLayout groupInfo;
    ArrayList<Map<String, Object>> announcements = new ArrayList<>();
    String courseGroupId;
    String myAccountType="student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_group);

        String selectedSession = (String) getIntent().getSerializableExtra("selectedSession");
        String batch = (String) getIntent().getSerializableExtra("batch");
        String section = (String) getIntent().getSerializableExtra("section");
        String courseCode = (String) getIntent().getSerializableExtra("courseCode");
        String courseName = (String) getIntent().getSerializableExtra("courseName");
        courseGroupId = selectedSession+"_"+batch+"_"+section+"_"+courseCode;

        courseGroupAnnouncementRecyclerView = findViewById(R.id.announcement_recycler_view);
        btnBack = findViewById(R.id.btn_back);
        btnAddAnnouncement = findViewById(R.id.btn_add_announcement);
        groupInfo = findViewById(R.id.group_info);
        courseCodeTextView = findViewById(R.id.course_code);
        courseNameTextView = findViewById(R.id.course_name);
        emptyText = findViewById(R.id.empty_text);

        courseGroupAnnouncementRecyclerView.setLayoutManager(new LinearLayoutManager(CourseGroupActivity.this));
        courseGroupAnnouncementRecyclerView.setHasFixedSize(true);

        courseCodeTextView.setText(courseCode);

        if(courseName!=null) {
            courseNameTextView.setText(courseName);
            courseNameTextView.setVisibility(View.VISIBLE);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(CourseGroupActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.announcement_type_overflow_menu, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId()==R.id.create_announcement) {
                            Intent intent = new Intent(CourseGroupActivity.this, CreateAnnouncementActivity.class);
                            intent.putExtra("announcement_type", "announcement");
                            intent.putExtra("courseGroupId", courseGroupId);
                            CourseGroupActivity.this.startActivity(intent);
                        }
                        else if(item.getItemId()==R.id.assign_assignment) {
                            Intent intent = new Intent(CourseGroupActivity.this, CreateAnnouncementActivity.class);
                            intent.putExtra("announcement_type", "assignment");
                            intent.putExtra("courseGroupId", courseGroupId);
                            CourseGroupActivity.this.startActivity(intent);
                        }

                        return true;
                    }
                });
            }
        });

        groupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseGroupActivity.this, GroupInfoActivity.class);
                intent.putExtra("courseGroupId", courseGroupId);
                intent.putExtra("myAccountType", myAccountType);
                CourseGroupActivity.this.startActivity(intent);
            }
        });

        getMyAccountType(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                courseGroupAnnouncementAdapter = new CourseGroupAnnouncementAdapter(CourseGroupActivity.this, announcements, myAccountType, courseGroupId);
                courseGroupAnnouncementRecyclerView.setAdapter(courseGroupAnnouncementAdapter);
                getAnnouncements();
            }
        });
    }

    void getMyAccountType(CompletionCallback callback) {

        DatabaseReference accountTypeReference = rootReference.child("student").child(user.getUid());
        accountTypeReference.keepSynced(true);

        accountTypeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().exists()) {
                        myAccountType = "student";
                    }
                    else {
                        myAccountType = "facultyMember";
                    }
                    callback.onCallback(true);
                }
            }
        });
    }

    void getAnnouncements() {

        DatabaseReference courseReference = rootReference.child("courseAnnouncements").child(courseGroupId);
        courseReference.keepSynced(true);

        courseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    courseGroupAnnouncementRecyclerView.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);

                    if(announcements.size()>0) {
                        announcements.clear();
                    }

                    for(DataSnapshot s : snapshot.getChildren()) {

                        Map<String, Object> announcementDetails = (Map<String, Object>) s.getValue();
                        announcementDetails.put("announcementId", s.getKey());
                        DatabaseReference queriesReference = rootReference.child("queries").child(courseGroupId).child(s.getKey());
                        queriesReference.keepSynced(true);

                        queriesReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                queriesReference.removeEventListener(this);

                                if(snapshot.exists()) {
                                    announcementDetails.put("queriesCount", snapshot.getChildrenCount());
                                }
                                else {
                                    announcementDetails.put("queriesCount", 0);
                                }

                                DatabaseReference profileReference = rootReference.child("student").child(String.valueOf(announcementDetails.get("posterId")));
                                profileReference.keepSynced(true);

                                profileReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {

                                        if(task.isSuccessful() && task.getResult().exists()) {

                                            Map<String, String> profileDetails = (Map<String, String>) task.getResult().getValue();
                                            announcementDetails.put("fullName", profileDetails.get("fullName"));

                                            if(profileDetails.containsKey("profilePicture")) {
                                                announcementDetails.put("profilePicture", profileDetails.get("profilePicture"));
                                            }

                                            announcements.add(announcementDetails);
                                            courseGroupAnnouncementAdapter.notifyDataSetChanged();
                                        }
                                        else {
                                            DatabaseReference profileReference = rootReference.child("facultyMember").child(String.valueOf(announcementDetails.get("posterId")));
                                            profileReference.keepSynced(true);

                                            profileReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    if(task.isSuccessful() && task.getResult().exists()) {

                                                        Map<String, Object> profileDetails = (Map<String, Object>) task.getResult().getValue();
                                                        announcementDetails.put("fullName", profileDetails.get("fullName"));

                                                        if(profileDetails.containsKey("profilePicture")) {
                                                            announcementDetails.put("profilePicture", profileDetails.get("profilePicture"));
                                                        }

                                                        announcements.add(announcementDetails);
                                                        courseGroupAnnouncementAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                queriesReference.removeEventListener(this);
                            }
                        });
                    }
                }
                else {
                    courseGroupAnnouncementRecyclerView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}