package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    RelativeLayout profileDropdown, parentRelativeLayout;
    ViewPager2 viewPager;
    BottomNavigationView bottomNavigationView;
    TextView fragmentName;
    Button btnProfile, btnLogout;
    ImageButton btnProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        parentRelativeLayout = findViewById(R.id.parent_relative_layout);
        profileDropdown = findViewById(R.id.profile_dropdown);
        btnProfile = findViewById(R.id.btn_profile);
        btnLogout = findViewById(R.id.btn_logout);
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        fragmentName = findViewById(R.id.fragment_name);
        btnProfilePicture = findViewById(R.id.btn_profile_picture);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId()==R.id.nav_home) {
                    viewPager.setCurrentItem(0);
                    fragmentName.setText("Home");
                    return true;
                }
                else if(item.getItemId()==R.id.nav_chats) {
                    viewPager.setCurrentItem(1);
                    fragmentName.setText("Chats");
                    return true;
                }
                else if(item.getItemId()==R.id.nav_courses) {
                    viewPager.setCurrentItem(2);
                    fragmentName.setText("My Courses");
                    return true;
                }
                else if(item.getItemId()==R.id.nav_department) {
                    viewPager.setCurrentItem(3);
                    fragmentName.setText("Department");
                    return true;
                }

                return false;
            }
        });

        // ViewPager page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {

                super.onPageSelected(position);

                if(position==0) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    fragmentName.setText("Home");
                }
                else if(position==1) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_chats);
                    fragmentName.setText("Chats");
                }
                else if(position==2) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_courses);
                    fragmentName.setText("My Courses");
                }
                else if(position==3) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_department);
                    fragmentName.setText("Department");
                }
            }
        });

        btnProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDropdown.setVisibility(View.VISIBLE);
                parentRelativeLayout.setVisibility(View.VISIBLE);
            }
        });

        parentRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDropdown.setVisibility(View.GONE);
                parentRelativeLayout.setVisibility(View.GONE);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDropdown.setVisibility(View.GONE);
                parentRelativeLayout.setVisibility(View.GONE);
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDropdown.setVisibility(View.GONE);
                parentRelativeLayout.setVisibility(View.GONE);
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });
    }
}