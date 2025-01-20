package com.tongteacrew.unihub;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DepartmentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    SwipeRefreshLayout swipeRefreshLayout;
    TextView departmentName;
    RecyclerView postRecyclerView, routineRecyclerView;
    PostAdapter postAdapter;
    ButtonListAdapter buttonListAdapter;
    ImageButton btnAbout;
    ImageView departmentImage;
    Button btnPost, btnPosts, btnRoutine;
    ArrayList<String> routine = new ArrayList<>();
    ArrayList<Map<String, Object>> posts = new ArrayList<>();
    String myAccountType="student";
    long departmentId=1;

    public DepartmentFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_department, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        postRecyclerView = view.findViewById(R.id.post_recycler_view);
        departmentName = view.findViewById(R.id.department_name);
        routineRecyclerView = view.findViewById(R.id.routine_recycler_view);
        btnAbout = view.findViewById(R.id.btn_about);
        btnPost = view.findViewById(R.id.btn_add_post);
        btnPosts = view.findViewById(R.id.text_posts);
        btnRoutine = view.findViewById(R.id.text_routine);
        departmentImage = view.findViewById(R.id.department_image);

        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        routineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        buttonListAdapter = new ButtonListAdapter(getContext(), routine);
        routineRecyclerView.setAdapter(buttonListAdapter);

        getMyAccountType(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                if(data!=null) {
                    getDepartmentId(new CompletionCallback() {
                        @Override
                        public void onCallback(Object data) {
                            if(data!=null) {
                                departmentId = (long) data;
                                setDepartmentName();
                                getDepartmentImage();
                                getPostIds();
                                swipeRefreshLayout.setOnRefreshListener(DepartmentFragment.this);
                            }
                        }
                    });
                }
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DepartmentInformationActivity.class);
                intent.putExtra("departmentId", departmentId);
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
                            intent.putExtra("accountType", myAccountType);
                            intent.putExtra("departmentId", departmentId);
                            intent.putExtra("myId", user.getUid());
                            requireContext().startActivity(intent);
                        }
                        else if(item.getItemId()==R.id.routine) {
                            Intent intent = new Intent(getContext(), AddRoutineActivity.class);
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

        return view;
    }

    void setDepartmentName() {

        String[] departments = {"Architecture", "Bangla", "Business Administration", "Civil Engineering",
                "Computer Science & Engineering", "Electrical and Electronics Engineering", "English",
                "Islamic Studies", "Law", "Public Health", "Tourism & Hospitality Management"};

        String name = "Department of "+departments[(int) departmentId];
        departmentName.setText(name);
    }

    void getDepartmentImage() {

        DatabaseReference imageReference = rootReference.child("departmentImage").child(String.valueOf(departmentId)).child("image");
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

    void getPostIds() {

        DatabaseReference depPostsReference = rootReference.child("departmentPosts").child(String.valueOf(departmentId));
        depPostsReference.keepSynced(true);

        depPostsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                depPostsReference.removeEventListener(this);

                if(snapshot.exists()) {

                    if(posts.size()>0) {
                        posts.clear();
                    }

                    for(DataSnapshot s : snapshot.getChildren()) {

                        Map<String, Object> post = new HashMap<>();
                        post.put("postId", s.getKey());
                        post.put("departmentId", departmentId);
                        posts.add(post);

                        if(posts.size()==snapshot.getChildrenCount()) {
                            getPostDetails(0);
                        }
                    }
                }
                else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                depPostsReference.removeEventListener(this);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    void getPostDetails(int index) {

        DatabaseReference postsReference = rootReference.child("posts").child(String.valueOf(posts.get(index).get("postId")));
        postsReference.keepSynced(true);

        postsReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful() && task.getResult().getValue()!=null) {

                    Map<String, Object> postDetails = (Map<String, Object>) task.getResult().getValue();
                    String time = getTime((Long)postDetails.get("time"))+" "+getDate((Long)postDetails.get("time"));

                    if(!((Boolean) postDetails.get("approval")) && !String.valueOf(postDetails.get("posterId")).equals(user.getUid()) && !Objects.equals(myAccountType, "facultyMember")) {

                        posts.remove(index);

                        if(index<posts.size()) {
                            getPostDetails(index+1);
                        }
                        else {
                            getPosterDetails(0);
                        }
                    }
                    else {

                        posts.get(index).put("time", time);
                        posts.get(index).put("approval", postDetails.get("approval"));
                        posts.get(index).put("accountType", postDetails.get("accountType"));
                        posts.get(index).put("text", postDetails.get("text"));
                        posts.get(index).put("posterId", postDetails.get("posterId"));


                        if(index+1<posts.size()) {
                            getPostDetails(index+1);
                        }
                        else {
                            getPosterDetails(0);
                        }
                    }
                }
                else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    void getPosterDetails(int index) {

        DatabaseReference posterReference = rootReference.child(String.valueOf(posts.get(index).get("accountType"))).child(String.valueOf(posts.get(index).get("posterId")));
        posterReference.keepSynced(true);

        posterReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful() && task.getResult().getValue()!=null) {

                    Map<String, Object> posterDetails = (Map<String, Object>) task.getResult().getValue();
                    posts.get(index).put("fullName", posterDetails.get("fullName"));

                    if(posterDetails.containsKey("profilePicture")) {
                        posts.get(index).put("profilePicture", posterDetails.get("profilePicture"));
                    }

                    if(index<posts.size()-1) {
                        getPosterDetails(index+1);
                    }
                    else {
                        getCommentCount(0);
                    }
                }
                else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    void getCommentCount(int index) {

        DatabaseReference commentReference = rootReference.child("comments").child(String.valueOf(posts.get(index).get("postId")));
        commentReference.keepSynced(true);

        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                commentReference.removeEventListener(this);
                posts.get(index).put("commentCount", snapshot.getChildrenCount());

                if(index<posts.size()-1) {
                    getCommentCount(index+1);
                }
                else {
                    postAdapter = new PostAdapter(getContext(), myAccountType, posts, new CompletionCallback() {
                        @Override
                        public void onCallback(Object data) {
                            onRefresh();
                        }
                    });
                    postRecyclerView.setAdapter(postAdapter);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                commentReference.removeEventListener(this);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    void getMyAccountType(CompletionCallback callback) {

        DatabaseReference accountTypeReference = rootReference.child("student").child(user.getUid());
        accountTypeReference.keepSynced(true);

        accountTypeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().exists()) {
                        myAccountType = "student";
                    }
                    else {
                        myAccountType = "facultyMember";
                    }
                    callback.onCallback(true);
                }
            }
        });
    }

    void getDepartmentId(CompletionCallback callback) {

        DatabaseReference depIdReference = rootReference.child("student").child(user.getUid()).child("departmentId");
        depIdReference.keepSynced(true);

        depIdReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().getValue()!=null) {
                    callback.onCallback(task.getResult().getValue());
                }
                else {

                    DatabaseReference depIdReference = rootReference.child("facultyMember").child(user.getUid()).child("departmentId");
                    depIdReference.keepSynced(true);

                    depIdReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful() && task.getResult().getValue()!=null) {
                                callback.onCallback(task.getResult().getValue());
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(null);
            }
        });
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

    @Override
    public void onRefresh() {
        getPostIds();
        getDepartmentImage();
    }
}