package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    RelativeLayout profileDropdown, parentRelativeLayout;
    ViewPager2 viewPager;
    BottomNavigationView bottomNavigationView;
    TextView fragmentName;
    Button btnProfile, btnLogout;
    ImageView btnProfilePicture;
    CircularProgressIndicator progressIndicator;
    String url="", accountType="";

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
        progressIndicator = findViewById(R.id.circular_progress_indicator);

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
                intent.putExtra("id", user.getUid());
                intent.putExtra("account_type", accountType);
                HomeActivity.this.startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDropdown.setVisibility(View.GONE);
                parentRelativeLayout.setVisibility(View.GONE);
                logout();
            }
        });

        checkAccountType();
        setOnlineStatus();
    }

    void setOnlineStatus() {

        DatabaseReference onlineStatusReference = rootReference.child("onlineStatus").child(user.getUid());
        Map<String, Boolean> online = new HashMap<>();
        online.put("isOnline", true);
        onlineStatusReference.setValue(online);

        Map<String, Boolean> offline = new HashMap<>();
        offline.put("isOnline", false);
        onlineStatusReference.onDisconnect().setValue(offline);
    }

    void setDeviceRegistrationToken() {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                String account;

                if(Objects.equals(accountType, "student")) {
                    account = "student";
                }
                else {
                    account = "facultyMember";
                }

                DatabaseReference tokenReference = rootReference.child(account).child(user.getUid()).child("deviceToken");
                tokenReference.keepSynced(true);

                tokenReference.setValue(task.getResult()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        System.out.println("Token updated");
                    }
                });
            }
        });
    }

    void checkAccountType() {

        Query accountTypeReference = rootReference.child("student").orderByKey().equalTo(user.getUid());
        accountTypeReference.keepSynced(true);

        accountTypeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                accountTypeReference.removeEventListener(this);

                if(snapshot.exists()) {
                    accountType = "student";
                    getProfilePicUrl("student");
                }
                else {
                    accountType = "faculty_member";
                    getProfilePicUrl("facultyMember");
                }

                setDeviceRegistrationToken();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                accountTypeReference.removeEventListener(this);
                System.out.println("Failed! :(");
            }
        });
    }

    void getProfilePicUrl(String accountType) {

        DatabaseReference profilePicReference = rootReference.child(accountType).child(user.getUid()).child("profilePicture");
        profilePicReference.keepSynced(true);

        profilePicReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                url = (String) snapshot.getValue();
                setProfilePic(url);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void setProfilePic(String url) {

        Glide.with(HomeActivity.this)
                .load(url)
                .error(R.drawable.icon_default_profile)
                .placeholder(R.drawable.icon_default_profile)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        progressIndicator.setVisibility(View.GONE);
                        Glide.with(HomeActivity.this).load(resource).circleCrop().into(btnProfilePicture);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        progressIndicator.setVisibility(View.GONE);
                        btnProfilePicture.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        progressIndicator.setVisibility(View.GONE);
                    }
                });
    }

    void logout() {

        if(!Objects.equals(accountType, "")) {

            makeOffline();

            removeDeviceRegistrationToken(new FirebaseCallback() {
                @Override
                public void onCallback(Object data) {
                    if((boolean)data) {
                        mAuth.signOut();
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        HomeActivity.this.startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }

    void makeOffline() {

        DatabaseReference onlineStatusReference = rootReference.child("onlineStatus").child(user.getUid());
        Map<String, Boolean> offline = new HashMap<>();
        offline.put("isOnline", false);
        onlineStatusReference.setValue(offline);
    }

    void removeDeviceRegistrationToken(FirebaseCallback callback) {

        DatabaseReference tokenReference = rootReference.child(accountType).child(user.getUid()).child("deviceToken");
        tokenReference.keepSynced(true);

        tokenReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("Token removed!");
                callback.onCallback(true);
            }
        });
    }
}