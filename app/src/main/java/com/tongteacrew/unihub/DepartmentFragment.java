package com.tongteacrew.unihub;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Objects;

public class DepartmentFragment extends Fragment {

    RecyclerView postRecyclerView, courseOfferingRecyclerView, routineRecyclerView;
    PostAdapter postAdapter;
    ButtonListAdapter buttonListAdapter;
    ImageButton btnAbout;
    Button btnPost, btnPosts, btnCourseOffering, btnRoutine;
    ArrayList<DepartmentPost> departmentPosts;
    ArrayList<String> courseOffering, routine;

    public DepartmentFragment() {
        generateTestDepartmentPosts();
        generateTestCourseOffering();
        generateTestRoutine();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_department, container, false);

        postRecyclerView = view.findViewById(R.id.post_recycler_view);
        courseOfferingRecyclerView = view.findViewById(R.id.course_offering_recycler_view);
        routineRecyclerView = view.findViewById(R.id.routine_recycler_view);
        btnAbout = view.findViewById(R.id.btn_about);
        btnPost = view.findViewById(R.id.btn_add_post);
        btnPosts = view.findViewById(R.id.text_posts);
        btnCourseOffering = view.findViewById(R.id.text_course_offering);
        btnRoutine = view.findViewById(R.id.text_routine);

        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postRecyclerView.setHasFixedSize(true);
        postAdapter = new PostAdapter(getContext(), departmentPosts);
        postRecyclerView.setAdapter(postAdapter);

        courseOfferingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        courseOfferingRecyclerView.setHasFixedSize(true);
        buttonListAdapter = new ButtonListAdapter(getContext(), courseOffering);
        courseOfferingRecyclerView.setAdapter(buttonListAdapter);

        routineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        routineRecyclerView.setHasFixedSize(true);
        buttonListAdapter = new ButtonListAdapter(getContext(), routine);
        routineRecyclerView.setAdapter(buttonListAdapter);

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

    void generateTestDepartmentPosts() {

        departmentPosts = new ArrayList<>();

        ArrayList arrayList = new ArrayList();

        DepartmentPost departmentPost = new DepartmentPost(
                "https://i.pinimg.com/736x/5a/ab/f8/5aabf84d67477f77d3bb8f0fe4cfcd17.jpg",
                "Lorem Ipsum Ahmed",
                "06:00 pm  07 Nov 2024",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus feugiat hendrerit diam, euismod imperdiet eros eleifend id. Nulla orci nulla, luctus nec diam id, malesuada semper nisi.",
                "Student",
                arrayList,
                false
        );

        departmentPosts.add(departmentPost);

        arrayList = new ArrayList();
        arrayList.add("https://media.licdn.com/dms/image/v2/C4E1BAQHjtT5hLiNv0g/company-background_10000/company-background_10000/0/1599838298704/luec_cover?e=2147483647&v=beta&t=17kG_BFwoTu6IV5Qmz95ktHM_rrVImx28v55hIVNLbo");
        arrayList.add("https://image.free-apply.com/gallery/l/uni/gallery/lg/1005000032/73b73fd83a4fca0e4a3b914f03bc759357e79041.jpg?s=640");

        departmentPost = new DepartmentPost(
                "https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg",
                "Lorem Ipsum Khan",
                "06:10 pm  07 Nov 2024",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec eu ligula luctus, consequat enim nec, commodo ante.",
                "Faculty Member",
                arrayList,
                true
        );

        departmentPosts.add(departmentPost);

        arrayList = new ArrayList();

        departmentPost = new DepartmentPost(
                "https://i.pinimg.com/736x/5a/ab/f8/5aabf84d67477f77d3bb8f0fe4cfcd17.jpg",
                "Lorem Ipsum Ahmed",
                "06:00 pm  07 Nov 2024",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nulla nisl.",
                "Student",
                arrayList,
                true
        );

        departmentPosts.add(departmentPost);
    }

    void generateTestCourseOffering() {

        courseOffering = new ArrayList<>();
        courseOffering.add("Fall 2024");
        courseOffering.add("Spring 2024");
        courseOffering.add("Fall 2023");
        courseOffering.add("Spring 2023");
    }

    void generateTestRoutine() {

        routine = new ArrayList<>();
        routine.add("Fall 2023");
        routine.add("Spring 2023");
        routine.add("Fall 2022");
        routine.add("Spring 2022");
    }
}