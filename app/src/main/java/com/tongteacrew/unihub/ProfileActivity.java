package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    LinearLayout designationsLayout, acronymLayout, batchLayout, sectionLayout, idLayout;
    RelativeLayout clubsSection;
    ImageButton btnEdit, btnBack;
    RecyclerView clubRecyclerView;
    ClubAdapter clubAdapter;
    ArrayList<String[]> clubs = new ArrayList<>();
    String accountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // To keep navigation bar, but make status bar transparent
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        // To fetch data from previous activity
        accountType = (String) getIntent().getSerializableExtra("account_type");

        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit);
        designationsLayout = findViewById(R.id.designations_layout);
        acronymLayout = findViewById(R.id.acronym_layout);
        batchLayout = findViewById(R.id.batch_layout);
        sectionLayout = findViewById(R.id.section_layout);
        idLayout = findViewById(R.id.id_layout);
        clubsSection = findViewById(R.id.clubs_section);
        clubRecyclerView = findViewById(R.id.post_recycler_view);

        if(Objects.equals(accountType, "student")) {
            designationsLayout.setVisibility(View.GONE);
            acronymLayout.setVisibility(View.GONE);
        }
        else if(Objects.equals(accountType, "faculty_member")) {
            batchLayout.setVisibility(View.GONE);
            sectionLayout.setVisibility(View.GONE);
            idLayout.setVisibility(View.GONE);
        }

        if(clubs.size()==0) {
            clubsSection.setVisibility(View.GONE);
        }

        clubRecyclerView.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
        clubRecyclerView.setHasFixedSize(true);
        clubAdapter = new ClubAdapter(ProfileActivity.this, clubs, View.GONE);
        clubRecyclerView.setAdapter(clubAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("account_type", accountType);
                ProfileActivity.this.startActivity(intent);
            }
        });
    }
}