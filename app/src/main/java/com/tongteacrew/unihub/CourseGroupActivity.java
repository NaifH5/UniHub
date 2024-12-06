package com.tongteacrew.unihub;

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

import java.util.ArrayList;

public class CourseGroupActivity extends AppCompatActivity {

    RecyclerView courseGroupAnnouncementRecyclerView;
    CourseGroupAnnouncementAdapter courseGroupAnnouncementAdapter;
    ImageButton btnBack;
    Button btnAddAnnouncement;
    LinearLayout groupInfo;
    ArrayList<ArrayList<String>> announcements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_group);

        generateTestGroupAnnouncement();

        courseGroupAnnouncementRecyclerView = findViewById(R.id.announcement_recycler_view);
        btnBack = findViewById(R.id.btn_back);
        btnAddAnnouncement = findViewById(R.id.btn_add_announcement);
        groupInfo = findViewById(R.id.group_info);

        courseGroupAnnouncementRecyclerView.setLayoutManager(new LinearLayoutManager(CourseGroupActivity.this));
        courseGroupAnnouncementRecyclerView.setHasFixedSize(true);
        courseGroupAnnouncementAdapter = new CourseGroupAnnouncementAdapter(CourseGroupActivity.this, announcements);
        courseGroupAnnouncementRecyclerView.setAdapter(courseGroupAnnouncementAdapter);

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
                            CourseGroupActivity.this.startActivity(intent);
                        }
                        else if(item.getItemId()==R.id.assign_assignment) {
                            Intent intent = new Intent(CourseGroupActivity.this, CreateAnnouncementActivity.class);
                            intent.putExtra("announcement_type", "assignment");
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
                CourseGroupActivity.this.startActivity(intent);
            }
        });
    }

    void generateTestGroupAnnouncement() {

        announcements = new ArrayList<>();

        ArrayList arrayList = new ArrayList();
        arrayList.add("https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg");
        arrayList.add("Lorem Ipsum Khan");
        arrayList.add("06:00 pm  05 Dec 2024");
        arrayList.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec eu ligula luctus, consequat enim nec, commodo ante.");
        arrayList.add("28 Dec 2024");
        announcements.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg");
        arrayList.add("Lorem Ipsum Khan");
        arrayList.add("06:10 pm  05 Dec 2024");
        arrayList.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nulla nisl.");
        arrayList.add("28 Dec 2024");
        announcements.add(arrayList);
    }
}