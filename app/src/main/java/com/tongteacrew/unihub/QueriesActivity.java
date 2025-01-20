package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class QueriesActivity extends AppCompatActivity {

    ImageButton btnBack;
    RecyclerView queriesRecyclerView;
    CommentsAdapter queriesAdapter;
    ArrayList<Replies> queries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queries);

        //generateTestQueries();

        btnBack = findViewById(R.id.btn_back);

        /*
        queriesRecyclerView = findViewById(R.id.queries_recycler_view);
        queriesRecyclerView.setLayoutManager(new LinearLayoutManager(QueriesActivity.this));
        queriesRecyclerView.setHasFixedSize(true);
        queriesAdapter = new CommentsAdapter(QueriesActivity.this, queries);
        */
        queriesRecyclerView.setAdapter(queriesAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void generateTestQueries() {

        queries = new ArrayList<>();

        Replies reply = new Replies(

                "https://i.pinimg.com/736x/5a/ab/f8/5aabf84d67477f77d3bb8f0fe4cfcd17.jpg",
                "Lorem Ipsum Ahmed",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus feugiat hendrerit diam, euismod imperdiet eros eleifend id. Nulla orci nulla, luctus nec diam id, malesuada semper nisi."
        );

        queries.add(reply);

        reply = new Replies(

                "https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg",
                "Lorem Ipsum Khan",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut nulla nisl."
        );

        queries.add(reply);
    }
}