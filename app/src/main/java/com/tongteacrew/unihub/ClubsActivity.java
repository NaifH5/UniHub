package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClubsActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    RecyclerView bnccRecyclerView, brsRecyclerView, bcRecyclerView, cefRecyclerView, ccRecyclerView,
            cc2RecyclerView, dcRecyclerView, ecRecyclerView, ieeesbRecyclerView, ieeeRecyclerView,
            icfRecyclerView, munaRecyclerView, mcsRecyclerView, orpheusRecyclerView, psRecyclerView,
            rsRecyclerView, rcRecyclerView, sscRecyclerView, scRecyclerView, tbcRecyclerView,
            tcRecyclerView;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubs);

        btnBack = findViewById(R.id.btn_back);
        bnccRecyclerView = findViewById(R.id.bncc_recycler_view);
        brsRecyclerView = findViewById(R.id.brs_recycler_view);
        bcRecyclerView = findViewById(R.id.bc_recycler_view);
        cefRecyclerView = findViewById(R.id.cef_recycler_view);
        ccRecyclerView = findViewById(R.id.cc_recycler_view);
        cc2RecyclerView = findViewById(R.id.cc2_recycler_view);
        dcRecyclerView = findViewById(R.id.dc_recycler_view);
        ecRecyclerView = findViewById(R.id.ec_recycler_view);
        ieeesbRecyclerView = findViewById(R.id.ieeesb_recycler_view);
        ieeeRecyclerView = findViewById(R.id.ieee_recycler_view);
        icfRecyclerView = findViewById(R.id.icf_recycler_view);
        munaRecyclerView = findViewById(R.id.muna_recycler_view);
        mcsRecyclerView = findViewById(R.id.mcs_recycler_view);
        orpheusRecyclerView = findViewById(R.id.orpheus_recycler_view);
        psRecyclerView = findViewById(R.id.ps_recycler_view);
        rsRecyclerView = findViewById(R.id.rs_recycler_view);
        rcRecyclerView = findViewById(R.id.rc_recycler_view);
        sscRecyclerView = findViewById(R.id.ssc_recycler_view);
        scRecyclerView = findViewById(R.id.sc_recycler_view);
        tbcRecyclerView = findViewById(R.id.tbc_recycler_view);
        tcRecyclerView = findViewById(R.id.tc_recycler_view);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getAllClubMembers();
    }

    void getAllClubMembers() {
        DatabaseReference clubsReference = rootReference.child("clubs");
        clubsReference.keepSynced(true);
        clubsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, ArrayList<Map<String, Object>>> clubMembers = new HashMap<>();
                if(snapshot.exists()) {
                    for(DataSnapshot memberSnapshot : snapshot.getChildren()) {

                        DatabaseReference memberReference = rootReference.child("student").child(memberSnapshot.getKey());
                        memberReference.keepSynced(true);
                        memberReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    Map<String, Object> memberData = (Map<String, Object>) snapshot.getValue();
                                    memberData.put("uid", snapshot.getKey());
                                    for(DataSnapshot clubSnapshot : memberSnapshot.getChildren()) {
                                        clubMembers.putIfAbsent(clubSnapshot.getKey(), new ArrayList<>());
                                        clubMembers.get(clubSnapshot.getKey()).add(memberData);
                                        setMemberList(clubSnapshot.getKey(), clubMembers.get(clubSnapshot.getKey()));
                                    }
                                }
                                else {
                                    DatabaseReference memberReference = rootReference.child("facultyMember").child(memberSnapshot.getKey());
                                    memberReference.keepSynced(true);
                                    memberReference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()) {
                                                Map<String, Object> memberData = (Map<String, Object>) snapshot.getValue();
                                                memberData.put("uid", snapshot.getKey());
                                                for(DataSnapshot clubSnapshot : memberSnapshot.getChildren()) {
                                                    clubMembers.putIfAbsent(clubSnapshot.getKey(), new ArrayList<>());
                                                    clubMembers.get(clubSnapshot.getKey()).add(memberData);
                                                    setMemberList(clubSnapshot.getKey(), clubMembers.get(clubSnapshot.getKey()));
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void setMemberList(String club, ArrayList<Map<String, Object>> list) {

        Map<String, RecyclerView> clubRecyclerViewMap = new HashMap<>();
        clubRecyclerViewMap.put("Bangladesh National Cadet Corps", bnccRecyclerView);
        clubRecyclerViewMap.put("Bangladesh Rover Scouts", brsRecyclerView);
        clubRecyclerViewMap.put("Business Club", bcRecyclerView);
        clubRecyclerViewMap.put("CE Family of Leading University", cefRecyclerView);
        clubRecyclerViewMap.put("Computer Club", ccRecyclerView);
        clubRecyclerViewMap.put("Cultural Club", cc2RecyclerView);
        clubRecyclerViewMap.put("Debating Club", dcRecyclerView);
        clubRecyclerViewMap.put("Electronics Club", ecRecyclerView);
        clubRecyclerViewMap.put("IEEE Computer Society LU SB Chapter", ieeesbRecyclerView);
        clubRecyclerViewMap.put("IEEE Student Branch", ieeeRecyclerView);
        clubRecyclerViewMap.put("Islamic Cultural Forum", icfRecyclerView);
        clubRecyclerViewMap.put("Model United Nations Association", munaRecyclerView);
        clubRecyclerViewMap.put("Moot Court Society", mcsRecyclerView);
        clubRecyclerViewMap.put("Orpheus", orpheusRecyclerView);
        clubRecyclerViewMap.put("Photographic Society", psRecyclerView);
        clubRecyclerViewMap.put("Research Society", rsRecyclerView);
        clubRecyclerViewMap.put("Rotaract Club", rcRecyclerView);
        clubRecyclerViewMap.put("Social Services Club", sscRecyclerView);
        clubRecyclerViewMap.put("Sports Club", scRecyclerView);
        clubRecyclerViewMap.put("Banned Community", tbcRecyclerView);
        clubRecyclerViewMap.put("Tourist Club", tcRecyclerView);

        clubRecyclerViewMap.get(club).setLayoutManager(new LinearLayoutManager(ClubsActivity.this));
        clubRecyclerViewMap.get(club).setAdapter(new ClubMembersAdapter(ClubsActivity.this, list));
    }
}