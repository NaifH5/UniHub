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
import java.util.Map;

public class CourseGroupAdapter extends RecyclerView.Adapter<CourseGroupAdapter.CourseGroupViewHolder> {

    Context context;
    ArrayList<Map<String, String>> courseGroups;
    String selectedSession;

    public CourseGroupAdapter(Context context, ArrayList<Map<String, String>> courseGroups, String selectedSession) {
        this.context = context;
        this.courseGroups = courseGroups;
        this.selectedSession = selectedSession;
    }

    @NonNull
    @Override
    public CourseGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CourseGroupViewHolder(LayoutInflater.from(context).inflate(R.layout.card_course_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CourseGroupAdapter.CourseGroupViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        holder.courseCode.setText(courseGroups.get(index).get("courseCode"));
        holder.batch.setText(String.format("Batch %s", courseGroups.get(index).get("batch")));
        holder.section.setText(String.format("Section %s", courseGroups.get(index).get("section")));

        if(courseGroups.get(index).containsKey("courseName")) {
            holder.courseName.setText(courseGroups.get(index).get("courseName"));
        }

        holder.courseGroupRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, CourseGroupActivity.class);
                intent.putExtra("selectedSession", selectedSession);
                intent.putExtra("batch", courseGroups.get(index).get("batch"));
                intent.putExtra("section", courseGroups.get(index).get("section"));
                intent.putExtra("courseCode", courseGroups.get(index).get("courseCode"));

                if(courseGroups.get(index).containsKey("courseName")) {
                    intent.putExtra("courseName", courseGroups.get(index).get("courseName"));
                }

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
