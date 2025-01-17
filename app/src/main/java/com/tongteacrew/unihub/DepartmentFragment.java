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
import android.widget.TextView;

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

public class DepartmentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    SwipeRefreshLayout swipeRefreshLayout;
    TextView departmentName;
    RecyclerView postRecyclerView, courseOfferingRecyclerView, routineRecyclerView;
    PostAdapter postAdapter;
    ButtonListAdapter buttonListAdapter;
    ImageButton btnAbout;
    Button btnPost, btnPosts, btnCourseOffering, btnRoutine;
    ArrayList<String> courseOffering, routine;
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
        courseOfferingRecyclerView = view.findViewById(R.id.course_offering_recycler_view);
        routineRecyclerView = view.findViewById(R.id.routine_recycler_view);
        btnAbout = view.findViewById(R.id.btn_about);
        btnPost = view.findViewById(R.id.btn_add_post);
        btnPosts = view.findViewById(R.id.text_posts);
        btnCourseOffering = view.findViewById(R.id.text_course_offering);
        btnRoutine = view.findViewById(R.id.text_routine);

        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        courseOfferingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        buttonListAdapter = new ButtonListAdapter(getContext(), courseOffering);
        courseOfferingRecyclerView.setAdapter(buttonListAdapter);

        routineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        buttonListAdapter = new ButtonListAdapter(getContext(), routine);
        routineRecyclerView.setAdapter(buttonListAdapter);

        getMyAccountType();

        getDepartmentId(new FirebaseCallback() {
            @Override
            public void onCallback(Object data) {
                if(data!=null) {
                    departmentId = (long) data;
                    setDepartmentName();
                    getPostIds();
                    swipeRefreshLayout.setOnRefreshListener(DepartmentFragment.this);
                }
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DepartmentInformationActivity.class);
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
                        else if(item.getItemId()==R.id.course_offering) {
                            Intent intent = new Intent(getContext(), AddCourseOfferingActivity.class);
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

                btnCourseOffering.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle_outline));
                btnCourseOffering.setBackgroundTintList(null);
                btnCourseOffering.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                btnRoutine.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle_outline));
                btnRoutine.setBackgroundTintList(null);
                btnRoutine.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                postRecyclerView.setVisibility(View.VISIBLE);
                courseOfferingRecyclerView.setVisibility(View.GONE);
                routineRecyclerView.setVisibility(View.GONE);
            }
        });

        btnCourseOffering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnCourseOffering.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle));
                btnCourseOffering.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
                btnCourseOffering.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkShade4));

                btnPosts.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle_outline));
                btnPosts.setBackgroundTintList(null);
                btnPosts.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                btnRoutine.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle_outline));
                btnRoutine.setBackgroundTintList(null);
                btnRoutine.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                courseOfferingRecyclerView.setVisibility(View.VISIBLE);
                postRecyclerView.setVisibility(View.GONE);
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

                btnCourseOffering.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle_outline));
                btnCourseOffering.setBackgroundTintList(null);
                btnCourseOffering.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

                routineRecyclerView.setVisibility(View.VISIBLE);
                postRecyclerView.setVisibility(View.GONE);
                courseOfferingRecyclerView.setVisibility(View.GONE);
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
                        posts.add(post);

                        if(posts.size()==snapshot.getChildrenCount()) {
                            getPostDetails(0);
                        }
                    }
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

                    posts.get(index).put("time", time);
                    posts.get(index).put("approval", postDetails.get("approval"));
                    posts.get(index).put("accountType", postDetails.get("accountType"));
                    posts.get(index).put("text", postDetails.get("text"));
                    posts.get(index).put("posterId", postDetails.get("posterId"));

                    if(index<posts.size()-1) {
                        getPostDetails(index+1);
                    }
                    else {
                        getPosterDetails(0);
                    }
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
                    postAdapter = new PostAdapter(getContext(), posts);
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

    void getMyAccountType() {

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
                }
            }
        });
    }

    void getDepartmentId(FirebaseCallback callback) {

        DatabaseReference depIdReference = rootReference.child("student").child(user.getUid()).child("departmentId");
        depIdReference.keepSynced(true);

        depIdReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
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
    }
}