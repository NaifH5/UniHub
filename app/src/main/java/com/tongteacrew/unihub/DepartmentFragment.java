package com.tongteacrew.unihub;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;

public class DepartmentFragment extends Fragment {

    RecyclerView postRecyclerView;
    PostAdapter postAdapter;
    ImageButton btnAbout;
    ArrayList<DepartmentPost> departmentPosts;

    public DepartmentFragment() {
        generateTestDepartmentPosts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_department, container, false);

        postRecyclerView = view.findViewById(R.id.post_recycler_view);
        btnAbout = view.findViewById(R.id.btn_about);

        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postRecyclerView.setHasFixedSize(true);
        postAdapter = new PostAdapter(getContext(), departmentPosts);
        postRecyclerView.setAdapter(postAdapter);

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DepartmentInformationActivity.class);
                getContext().startActivity(intent);
            }
        });

        return view;
    }

    void generateTestDepartmentPosts() {

        departmentPosts = new ArrayList<>();

        ArrayList arrayList = new ArrayList();
        arrayList.add("https://c1.wallpaperflare.com/path/852/733/196/dog-lake-mountains-water-40a62041cc4e4f7fbd058f4892ba3ef6.jpg");

        DepartmentPost departmentPost = new DepartmentPost(
                "https://c1.wallpaperflare.com/path/852/733/196/dog-lake-mountains-water-40a62041cc4e4f7fbd058f4892ba3ef6.jpg",
                "Naif Haider Chowdhury",
                "06:00 pm  02 Oct 2024",
                "Hello, I am Naif Haider Chowdhury!",
                "student",
                arrayList,
                true
        );

        departmentPosts.add(departmentPost);

        arrayList = new ArrayList();
        arrayList.add("https://media.licdn.com/dms/image/v2/C4E1BAQHjtT5hLiNv0g/company-background_10000/company-background_10000/0/1599838298704/luec_cover?e=2147483647&v=beta&t=17kG_BFwoTu6IV5Qmz95ktHM_rrVImx28v55hIVNLbo");
        arrayList.add("https://image.free-apply.com/gallery/l/uni/gallery/lg/1005000032/73b73fd83a4fca0e4a3b914f03bc759357e79041.jpg?s=640");

        departmentPost = new DepartmentPost(
                "https://static.dc.com/dc/files/default_images/Char_Profile_Batman_20190116_5c3fc4b40faec2.47318964.jpg",
                "Shahriar Jahan Sunny",
                "07:00 pm  02 Oct 2024",
                "Hello, I am Shahriar Jahan Sunny!",
                "faculty_member",
                arrayList,
                true
        );

        departmentPosts.add(departmentPost);

        arrayList = new ArrayList();
        arrayList.add("https://media.licdn.com/dms/image/v2/C4E1BAQHjtT5hLiNv0g/company-background_10000/company-background_10000/0/1599838298704/luec_cover?e=2147483647&v=beta&t=17kG_BFwoTu6IV5Qmz95ktHM_rrVImx28v55hIVNLbo");
        arrayList.add("https://image.free-apply.com/gallery/l/uni/gallery/lg/1005000032/73b73fd83a4fca0e4a3b914f03bc759357e79041.jpg?s=640");
        arrayList.add("https://image.free-apply.com/gallery/l/uni/gallery/lg/1005000032/73b73fd83a4fca0e4a3b914f03bc759357e79041.jpg?s=640");
        arrayList.add("https://image.free-apply.com/gallery/l/uni/gallery/lg/1005000032/73b73fd83a4fca0e4a3b914f03bc759357e79041.jpg?s=640");
        arrayList.add("https://image.free-apply.com/gallery/l/uni/gallery/lg/1005000032/73b73fd83a4fca0e4a3b914f03bc759357e79041.jpg?s=640");

        departmentPost = new DepartmentPost(
                "https://media.cnn.com/api/v1/images/stellar/prod/221024162135-man-of-steel-cavill.jpg?c=16x9&q=h_833,w_1480,c_fill",
                "MD. Ali Hossain Sagor",
                "08:00 pm  02 Oct 2024",
                "Hello, I am MD. Ali Hossain Sagor!",
                "student",
                arrayList,
                false
        );

        departmentPosts.add(departmentPost);

        arrayList = new ArrayList();

        departmentPost = new DepartmentPost(
                "https://media.cnn.com/api/v1/images/stellar/prod/221024162135-man-of-steel-cavill.jpg?c=16x9&q=h_833,w_1480,c_fill",
                "MD. Ali Hossain Sagor",
                "08:00 pm  02 Oct 2024",
                "Hello, I am MD. Ali Hossain Sagor!",
                "student",
                arrayList,
                false
        );

        departmentPosts.add(departmentPost);
    }
}