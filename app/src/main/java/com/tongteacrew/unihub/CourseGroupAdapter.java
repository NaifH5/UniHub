package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

public class CourseGroupAdapter extends RecyclerView.Adapter<CourseGroupAdapter.CourseGroupViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> courseGroups;

    public CourseGroupAdapter(Context context, ArrayList<ArrayList<String>> courseGroups) {
        this.context = context;
        this.courseGroups = courseGroups;
    }

    @NonNull
    @Override
    public CourseGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CourseGroupViewHolder(LayoutInflater.from(context).inflate(R.layout.card_course_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CourseGroupAdapter.CourseGroupViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        holder.courseCode.setText(courseGroups.get(index).get(0));
        holder.courseName.setText(courseGroups.get(index).get(1));
        holder.batch.setText(courseGroups.get(index).get(2));
        holder.section.setText(courseGroups.get(index).get(3));

        holder.courseGroupRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CourseGroupActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseGroups.size();
    }

    public static class CourseGroupViewHolder extends RecyclerView.ViewHolder {

        TextView courseCode, courseName, batch, section;
        RelativeLayout courseGroupRelativeLayout;

        public CourseGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCode = itemView.findViewById(R.id.course_code);
            courseName = itemView.findViewById(R.id.course_name);
            batch = itemView.findViewById(R.id.batch);
            section = itemView.findViewById(R.id.section);
            courseGroupRelativeLayout = itemView.findViewById(R.id.course_group_relative_layout);
        }
    }
}
