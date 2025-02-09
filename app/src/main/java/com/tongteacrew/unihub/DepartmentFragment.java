package com.tongteacrew.unihub;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DepartmentFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    TextView departmentName;
    RecyclerView postRecyclerView, routineRecyclerView;
    PostAdapter postAdapter;
    RoutineAdapter routineAdapter;
    ImageButton btnAbout;
    ImageView departmentImage;
    Button btnPost, btnPosts, btnRoutine;
    ArrayList<Map<String, String>> routine = new ArrayList<>();
    ArrayList<Map<String, Object>> posts = new ArrayList<>();
    Map<String, Object> myDetails = new HashMap<>();
    String myAccountType="student";

    public DepartmentFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_department, container, false);

        postRecyclerView = view.findViewById(R.id.post_recycler_view);
        departmentName = view.findViewById(R.id.department_name);
        routineRecyclerView = view.findViewById(R.id.routine_recycler_view);
        btnAbout = view.findViewById(R.id.btn_about);
        btnPost = view.findViewById(R.id.btn_add_post);
        btnPosts = view.findViewById(R.id.text_posts);
        btnRoutine = view.findViewById(R.id.text_routine);
        departmentImage = view.findViewById(R.id.department_image);

        routineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DepartmentInformationActivity.class);
                intent.putExtra("departmentId", (Long) myDetails.get("departmentId"));
                getContext().startActivity(intent);
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(requireContext(), v);
                popup.getMenuInflater().inflate(R.menu.post_type_overflow_menu, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId()==R.id.create_post) {
                            Intent intent = new Intent(getContext(), CreatePostActivity.class);
                            intent.putExtra("accountType", String.valueOf(myDetails.get("accountType")));
                            intent.putExtra("departmentId", (Long) myDetails.get("departmentId"));
                            intent.putExtra("myId", user.getUid());
                            requireContext().startActivity(intent);
                        }
                        else if(item.getItemId()==R.id.routine) {
                            Intent intent = new Intent(getContext(), AddRoutineActivity.class);
                            intent.putExtra("departmentId", (Long) myDetails.get("departmentId"));
                            requireContext().startActivity(intent);
                        }

                        return true;
                    }
                });
            }
        });

        btnPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnPosts.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle));
                btnPosts.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
                btnPosts.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkShade4));

                btnRoutine.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle_outline));
                btnRoutine.setBackgroundTintList(null);
                btnRoutine.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                postRecyclerView.setVisibility(View.VISIBLE);
                routineRecyclerView.setVisibility(View.GONE);
            }
        });

        btnRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnRoutine.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle));
                btnRoutine.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
                btnRoutine.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkShade4));

                btnPosts.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle_outline));
                btnPosts.setBackgroundTintList(null);
                btnPosts.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                routineRecyclerView.setVisibility(View.VISIBLE);
                postRecyclerView.setVisibility(View.GONE);
            }
        });

        getMyAccountType();
        return view;
    }

    void getMyAccountType() {

        DatabaseReference studentRef = rootReference.child("student").child(user.getUid());
        DatabaseReference facultyRef = rootReference.child("facultyMember").child(user.getUid());

        studentRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()) {
                    getMyDetails(task.getResult(), "student");
                }
                else {
                    facultyRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task2) {
                            if(task2.isSuccessful() && task2.getResult().exists()) {
                                getMyDetails(task2.getResult(), "facultyMember");
                            }
                        }
                    });
                }
            }
        });
    }

    void getMyDetails(DataSnapshot snapshot, String accountType) {

        if(snapshot.exists()) {

            myDetails = (Map<String, Object>) snapshot.getValue();
            myDetails.put("accountType", accountType);

            postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            postAdapter = new PostAdapter(getContext(), posts, String.valueOf(myDetails.get("accountType")));
            postRecyclerView.setAdapter(postAdapter);

            setDepartmentName();
            getDepartmentImage();
            getPosts();
            getRoutine();
        }
    }

    void setDepartmentName() {

        String[] departments = {"Architecture", "Bangla", "Business Administration", "Civil Engineering",
                "Computer Science & Engineering", "Electrical and Electronics Engineering", "English",
                "Islamic Studies", "Law", "Public Health", "Tourism & Hospitality Management"};

        int departmentId = ((Long) myDetails.get("departmentId")).intValue();
        String name = "Department of " + departments[departmentId];
        departmentName.setText(name);
    }

    void getPosts() {

        DatabaseReference postReference = rootReference.child("departmentPosts").child(String.valueOf(myDetails.get("departmentId")));
        postReference.keepSynced(true);

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(posts.size()>0) {
                    posts.clear();
                }

                if(snapshot.exists()) {
                    for(DataSnapshot s : snapshot.getChildren()) {
                        getPostDetails(s.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getPostDetails(String postId) {

        DatabaseReference postReference = rootReference.child("posts").child(postId);
        postReference.keepSynced(true);

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                postReference.removeEventListener(this);

                if(snapshot.exists()) {

                    Map<String, Object> postDetails = (Map<String, Object>) snapshot.getValue();
                    boolean approved = (boolean) postDetails.get("approval");

                    if(!approved && String.valueOf(myDetails.get("accountType")).equals("student") && !String.valueOf(postDetails.get("posterId")).equals(user.getUid())) {
                        return;
                    }

                    postDetails.put("postId", postId);
                    getMedias(postDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                postReference.removeEventListener(this);
            }
        });
    }

    void getMedias(Map<String, Object> postDetails) {

        DatabaseReference mediaReference = rootReference.child("medias").child(String.valueOf(postDetails.get("postId")));

        mediaReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    ArrayList<String> medias = new ArrayList<>();

                    for(DataSnapshot s : snapshot.getChildren()) {
                        medias.add(String.valueOf(s.getValue()));
                        if(medias.size()==snapshot.getChildrenCount()) {
                            postDetails.put("medias", medias);
                            getPosterDetails(postDetails);
                        }
                    }
                }
                else {
                    getPosterDetails(postDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getPosterDetails(Map<String, Object> postDetails) {

        String accountType = String.valueOf(postDetails.get("accountType"));
        String posterId = String.valueOf(postDetails.get("posterId"));
        DatabaseReference posterReference = rootReference.child(accountType).child(posterId);
        posterReference.keepSynced(true);

        posterReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful() && task.getResult().exists()) {

                    Map<String, Object> posterDetails = (Map<String, Object>) task.getResult().getValue();
                    postDetails.putAll(posterDetails);
                    String time = getTime((Long)postDetails.get("time"))+" "+getDate((Long)postDetails.get("time"));
                    postDetails.put("time", time);
                    boolean postExists = false;

                    for(int i=0; i<posts.size(); i++) {
                        if(String.valueOf(posts.get(i).get("postId")).equals(String.valueOf(postDetails.get("postId")))) {
                            posts.set(i, postDetails);
                            postExists = true;
                            break;
                        }
                    }

                    if(!postExists) {
                        getCommentCount(postDetails);
                    }
                }
            }
        });
    }

    void getCommentCount(Map<String, Object> postDetails) {

        DatabaseReference commentReference = rootReference.child("comments").child(String.valueOf(postDetails.get("postId")));
        commentReference.keepSynced(true);

        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                postDetails.put("commentCount", snapshot.getChildrenCount());
                boolean postExists = false;

                for(int i=0; i<posts.size(); i++) {
                    if(String.valueOf(posts.get(i).get("postId")).equals(String.valueOf(postDetails.get("postId")))) {
                        posts.set(i, postDetails);
                        postExists = true;
                        break;
                    }
                }

                if(!postExists) {
                    posts.add(postDetails);
                }

                postAdapter = new PostAdapter(getContext(), posts, String.valueOf(myDetails.get("accountType")));
                postRecyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getRoutine() {

        DatabaseReference routineReference = rootReference.child("routineUrl").child(String.valueOf(myDetails.get("departmentId")));
        routineReference.keepSynced(true);

        routineReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    if(routine.size()>0) {
                        routine.clear();
                    }

                    for(DataSnapshot s : snapshot.getChildren()) {

                        Map<String, String> map = (Map<String, String>) s.getValue();
                        routine.add(map);

                        if(routine.size()==snapshot.getChildrenCount()) {
                            Collections.reverse(routine);
                            routineAdapter = new RoutineAdapter(getContext(), routine, myAccountType, (Long) myDetails.get("departmentId"));
                            routineRecyclerView.setAdapter(routineAdapter);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getDepartmentImage() {

        DatabaseReference imageReference = rootReference.child("departmentImage").child(String.valueOf(myDetails.get("departmentId"))).child("image");
        imageReference.keepSynced(true);

        imageReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful() && task.getResult().getValue()!=null) {
                    setImage(String.valueOf(task.getResult().getValue()));
                }
            }
        });
    }

    void setImage(String url) {

        Glide.with(getContext())
                .load(url)
                .error(R.drawable.illustration_1)
                .placeholder(R.drawable.illustration_1)
                .into(departmentImage);
    }

    String getDate(long time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(Instant.ofEpochMilli(time));
        return formattedDate;
    }

    String getTime(long time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault());
        String formattedTime = formatter.format(Instant.ofEpochMilli(time));
        return formattedTime;
    }
}