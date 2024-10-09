package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    BottomNavigationView bottomNavigationView;
    TextView fragmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        fragmentName = findViewById(R.id.fragment_name);

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
                else if(item.getItemId()==R.id.nav_courses) {
                    viewPager.setCurrentItem(1);
                    fragmentName.setText("My Courses");
                    return true;
                }
                else if(item.getItemId()==R.id.nav_department) {
                    viewPager.setCurrentItem(2);
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
                    bottomNavigationView.setSelectedItemId(R.id.nav_courses);
                    fragmentName.setText("My Courses");
                }
                else if(position==2) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_department);
                    fragmentName.setText("Department");
                }
            }
        });
    }
}