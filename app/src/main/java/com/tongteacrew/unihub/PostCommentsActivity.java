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

public class PostCommentsActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    ImageButton btnBack, btnComment;
    RecyclerView repliesRecyclerView;
    EditText commentText;
    CommentsAdapter commentsAdapter;
    String postId="";
    ArrayList<Map<String, Object>> comments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_replies);

        postId = (String) getIntent().getSerializableExtra("postId");

        btnBack = findViewById(R.id.btn_back);
        btnComment = findViewById(R.id.btn_send);
        commentText = findViewById(R.id.text_message);
        repliesRecyclerView = findViewById(R.id.replies_recycler_view);

        repliesRecyclerView.setLayoutManager(new LinearLayoutManager(PostCommentsActivity.this));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment(String.valueOf(commentText.getText()));
                commentText.setText("");
            }
        });

        getComments();
    }

    void addComment(String text) {

        String commentId = rootReference.child("comments").child(postId).push().getKey();
        Map<String, Object> comment = new HashMap<>();
        comment.put("posterId", user.getUid());
        comment.put("text", text);

        rootReference.child("comments").child(postId).child(commentId).setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("Commented!");
            }
        });
    }

    void getComments() {

        DatabaseReference commentsReference = rootReference.child("comments").child(postId);
        commentsReference.keepSynced(true);

        commentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    comments.clear();

                    for(DataSnapshot s : snapshot.getChildren()) {

                        Map<String, Object> data = (Map<String, Object>) s.getValue();
                        Map<String, Object> comment = new HashMap<>();
                        comment.put("posterId", data.get("posterId"));
                        comment.put("text", data.get("text"));
                        comments.add(comment);

                        if(comments.size()==snapshot.getChildrenCount()) {
                            getPosterDetails(0, "student");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getPosterDetails(int index, String accountType) {

        DatabaseReference posterReference = rootReference.child(accountType).child(String.valueOf(comments.get(index).get("posterId")));
        posterReference.keepSynced(true);

        posterReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()) {

                    if(task.getResult().getValue()!=null) {

                        Map<String, Object> poster = (Map<String, Object>) task.getResult().getValue();
                        comments.get(index).put("posterName", String.valueOf(poster.get("fullName")));

                        if(poster.containsKey("profilePicture")) {
                            comments.get(index).put("profilePicture", String.valueOf(poster.get("profilePicture")));
                        }

                        if(index+1<comments.size()) {
                            getPosterDetails(index+1, accountType);
                        }
                        else {
                            commentsAdapter = new CommentsAdapter(PostCommentsActivity.this, comments);
                            repliesRecyclerView.setAdapter(commentsAdapter);
                        }
                    }
                    else {

                        DatabaseReference posterReference = rootReference.child("facultyMember").child(String.valueOf(comments.get(index).get("posterId")));
                        posterReference.keepSynced(true);

                        posterReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {

                                if(task.isSuccessful() && task.getResult().getValue()!=null) {

                                    Map<String, Object> poster = (Map<String, Object>) task.getResult().getValue();
                                    comments.get(index).put("posterName", String.valueOf(poster.get("fullName")));

                                    if(poster.containsKey("profilePicture")) {
                                        comments.get(index).put("profilePicture", String.valueOf(poster.get("profilePicture")));
                                    }

                                    if(index+1<comments.size()) {
                                        getPosterDetails(index+1, "student");
                                    }
                                    else {
                                        commentsAdapter = new CommentsAdapter(PostCommentsActivity.this, comments);
                                        repliesRecyclerView.setAdapter(commentsAdapter);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}