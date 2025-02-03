package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

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
import java.util.HashMap;
import java.util.Map;

public class QueriesActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    ImageButton btnBack, btnSend;
    RecyclerView queriesRecyclerView;
    CommentsAdapter queriesAdapter;
    EditText text;
    ArrayList<Map<String, Object>> queries = new ArrayList<>();
    String courseGroupId, announcementId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queries);

        courseGroupId = (String) getIntent().getSerializableExtra("courseGroupId");
        announcementId = (String) getIntent().getSerializableExtra("announcementId");

        btnBack = findViewById(R.id.btn_back);
        text = findViewById(R.id.text_message);
        btnSend = findViewById(R.id.btn_send);

        queriesRecyclerView = findViewById(R.id.queries_recycler_view);
        queriesRecyclerView.setLayoutManager(new LinearLayoutManager(QueriesActivity.this));
        queriesRecyclerView.setHasFixedSize(true);
        queriesAdapter = new CommentsAdapter(QueriesActivity.this, queries);

        queriesRecyclerView.setAdapter(queriesAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postQuery(String.valueOf(text.getText()));
                text.setText("");
            }
        });

        getQueries();
    }

    void getQueries() {

        DatabaseReference queryReference = rootReference.child("queries").child(courseGroupId).child(announcementId);
        queryReference.keepSynced(true);

        queryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    if(queries.size()>0) {
                        queries.clear();
                    }

                    for(DataSnapshot s : snapshot.getChildren()) {

                        Map<String, Object> data = (Map<String, Object>) s.getValue();
                        DatabaseReference profileReference = rootReference.child("student").child(String.valueOf(data.get("posterId")));
                        profileReference.keepSynced(true);

                        profileReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {

                                if(task.isSuccessful() && task.getResult().exists()) {

                                    Map<String, Object> user = (Map<String, Object>) task.getResult().getValue();
                                    data.put("posterName", user.get("fullName"));

                                    if(user.containsKey("profilePicture")) {
                                        data.put("profilePicture", user.get("profilePicture"));
                                    }

                                    queries.add(data);

                                    if(queries.size()==snapshot.getChildrenCount()) {
                                        queriesAdapter.notifyDataSetChanged();
                                    }
                                }
                                else {

                                    DatabaseReference profileReference = rootReference.child("facultyMember").child(String.valueOf(data.get("posterId")));
                                    profileReference.keepSynced(true);

                                    profileReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {

                                            if(task.isSuccessful() && task.getResult().exists()) {

                                                Map<String, Object> user = (Map<String, Object>) task.getResult().getValue();
                                                data.put("posterName", user.get("fullName"));

                                                if(user.containsKey("profilePicture")) {
                                                    data.put("profilePicture", user.get("profilePicture"));
                                                }

                                                queries.add(data);

                                                if(queries.size()==snapshot.getChildrenCount()) {
                                                    queriesAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void postQuery(String text) {

        String id = rootReference.child("queries").child(courseGroupId).child(announcementId).push().getKey();
        DatabaseReference queryReference = rootReference.child("queries").child(courseGroupId).child(announcementId).child(id);

        Map<String, Object> data = new HashMap<>();
        data.put("posterId", user.getUid());
        data.put("text", text);

        queryReference.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("Queried");
            }
        });
    }
}