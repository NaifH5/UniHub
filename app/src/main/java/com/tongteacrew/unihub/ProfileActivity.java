package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    LinearLayout designationsLayout, acronymLayout, batchLayout, sectionLayout, idLayout;
    FrameLayout editFrameLayout;
    RelativeLayout clubsSection, noteSection;
    ImageButton btnEdit, btnBack;
    ImageView profilePicture;
    CircularProgressIndicator progressIndicator;
    RecyclerView clubRecyclerView;
    ClubAdapter clubAdapter;
    ArrayList<String[]> clubs = new ArrayList<>();
    String accountType="student", profileId;
    TextView fullName, department, batch, section, id, acronym, note, designation;
    Map<String, Object> data, designationMap;
    Map<String, Map<String, Object>> clubDetails = new HashMap<>();

    String[] departments = {"Architecture", "Bangla", "Business Administration", "Civil Engineering",
            "Computer Science & Engineering", "Electrical and Electronics Engineering", "English",
            "Islamic Studies", "Law", "Public Health", "Tourism & Hospitality Management"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // To fetch data from previous activity
        profileId = (String) getIntent().getSerializableExtra("id");

        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit);
        editFrameLayout = findViewById(R.id.edit_frame_layout);
        designationsLayout = findViewById(R.id.designations_layout);
        profilePicture = findViewById(R.id.profile_picture);
        progressIndicator = findViewById(R.id.circular_progress_indicator);
        acronymLayout = findViewById(R.id.acronym_layout);
        batchLayout = findViewById(R.id.batch_layout);
        sectionLayout = findViewById(R.id.section_layout);
        idLayout = findViewById(R.id.id_layout);
        clubsSection = findViewById(R.id.clubs_section);
        noteSection = findViewById(R.id.note_section);
        clubRecyclerView = findViewById(R.id.post_recycler_view);
        fullName = findViewById(R.id.full_name);
        department = findViewById(R.id.department);
        batch = findViewById(R.id.batch);
        section = findViewById(R.id.section);
        id = findViewById(R.id.id);
        acronym = findViewById(R.id.acronym);
        designation = findViewById(R.id.designations);
        note = findViewById(R.id.note);

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
                intent.putExtra("id", profileId);
                intent.putExtra("account_type", accountType);
                intent.putExtra("profile_info", (Serializable) data);
                intent.putExtra("club_details", (Serializable) clubs);
                ProfileActivity.this.startActivity(intent);
            }
        });

        getAccountType(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                if(data!=null) {
                    accountType = (String) data;
                    getProfileDetails();
                }
            }
        });
    }

    void getAccountType(CompletionCallback callback) {

        DatabaseReference accountTypeReference = rootReference.child("student").child(profileId);
        accountTypeReference.keepSynced(true);

        accountTypeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()) {
                    if(task.getResult().getValue()!=null) {
                        callback.onCallback("student");
                    }
                    else if(task.isSuccessful()) {
                        callback.onCallback("facultyMember");
                    }
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

    void getProfileDetails() {

        String account;

        if(Objects.equals(accountType, "student")) {
            account = "student";
        }
        else {
            account = "facultyMember";
            getDesignations();
        }

        DatabaseReference profileDataReference = rootReference.child(account).child(profileId);
        profileDataReference.keepSynced(true);

        profileDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    data = (Map<String, Object>) snapshot.getValue();
                    System.out.println(data);

                    if(data.containsKey("profilePicture")) {
                        setProfilePicture(Objects.requireNonNull(data.get("profilePicture")).toString());
                    }

                    department.setText(data.containsKey("departmentId") ? departments[((Number)data.get("departmentId")).intValue()] : "");
                    fullName.setText(data.containsKey("fullName") ? Objects.requireNonNull(data.get("fullName")).toString() : "");
                    section.setText(data.containsKey("sectionId") ? "Section "+Objects.requireNonNull(data.get("sectionId")) : "");
                    id.setText(data.containsKey("id") ? Objects.requireNonNull(data.get("id")).toString() : "");
                    batch.setText(data.containsKey("batchId") ? "Batch "+Objects.requireNonNull(data.get("batchId")) : "");
                    acronym.setText(data.containsKey("acronym") ? Objects.requireNonNull(data.get("acronym")).toString() : "");

                    if(data.containsKey("notes")) {
                        note.setText(Objects.requireNonNull(data.get("notes")).toString());
                        noteSection.setVisibility(View.VISIBLE);
                    }
                    else {
                        noteSection.setVisibility(View.GONE);
                    }

                    if(Objects.equals(profileId, user.getUid())) {
                        editFrameLayout.setVisibility(View.VISIBLE);
                    }
                    else {
                        editFrameLayout.setVisibility(View.GONE);
                    }

                    if(Objects.equals(accountType, "student")) {
                        designationsLayout.setVisibility(View.GONE);
                        acronymLayout.setVisibility(View.GONE);
                    }
                    else {
                        batchLayout.setVisibility(View.GONE);
                        sectionLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        getClubs();
    }

    void setProfilePicture(String url) {

        if (!isDestroyed() && !isFinishing()) {
            Glide.with(ProfileActivity.this)
                    .load(url)
                    .error(R.drawable.icon_default_profile)
                    .placeholder(R.drawable.icon_default_profile)
                    .into(new CustomTarget<Drawable>(){

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            progressIndicator.setVisibility(View.GONE);
                            Glide.with(ProfileActivity.this).load(resource).into(profilePicture);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            progressIndicator.setVisibility(View.GONE);
                            profilePicture.setImageDrawable(placeholder);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            progressIndicator.setVisibility(View.GONE);
                        }
                    });
        }
    }

    void getDesignations() {

        DatabaseReference designationsReference = rootReference.child("designations").child(profileId);
        designationsReference.keepSynced(true);

        designationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    designationMap = (Map<String, Object>) snapshot.getValue();
                }
                setDesignations();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void setDesignations() {

        StringBuilder selectedOptions = new StringBuilder();

        for(Map.Entry<String, Object> entry : designationMap.entrySet()) {
            selectedOptions.append(entry.getKey()).append(", ");
        }

        if(selectedOptions.length()>0) {
            selectedOptions.setLength(selectedOptions.length()-2);
        }

        designation.setText(selectedOptions);
    }

    void getClubs() {

        rootReference.child("clubs").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    clubDetails = (Map<String, Map<String, Object>>) snapshot.getValue();

                    if(clubDetails!=null) {

                        clubs.clear();

                        for(Map.Entry<String, Map<String, Object>> clubEntry : clubDetails.entrySet()) {

                            Map<String, Object> roles = clubEntry.getValue();

                            for(Map.Entry<String, Object> roleEntry : roles.entrySet()) {
                                clubs.add(new String[]{clubEntry.getKey(), roleEntry.getKey()});
                                clubAdapter.notifyDataSetChanged();
                            }
                        }
                        clubsSection.setVisibility(View.VISIBLE);
                    }
                    else {
                        clubsSection.setVisibility(View.GONE);
                        clubs.clear();
                        clubAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    clubsSection.setVisibility(View.GONE);
                    clubs.clear();
                    clubAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}