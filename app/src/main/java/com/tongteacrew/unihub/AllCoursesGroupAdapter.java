package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class AllCoursesGroupAdapter extends RecyclerView.Adapter<AllCoursesGroupAdapter.AllCoursesGroupViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> courseGroups;

    public AllCoursesGroupAdapter(Context context, ArrayList<ArrayList<String>> courseGroups) {
        this.context = context;
        this.courseGroups = courseGroups;
    }

    @NonNull
    @Override
    public AllCoursesGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AllCoursesGroupViewHolder(LayoutInflater.from(context).inflate(R.layout.card_course_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AllCoursesGroupAdapter.AllCoursesGroupViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        holder.courseCode.setText(courseGroups.get(index).get(0));
        holder.courseName.setText(courseGroups.get(index).get(1));
        holder.batch.setText(courseGroups.get(index).get(2));
        holder.section.setText(courseGroups.get(index).get(3));

        holder.btnGroupParticipation.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return courseGroups.size();
    }

    public static class AllCoursesGroupViewHolder extends RecyclerView.ViewHolder {

        TextView courseCode, courseName, batch, section;
        Button btnGroupParticipation;
        RelativeLayout courseGroupRelativeLayout;

        public AllCoursesGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCode = itemView.findViewById(R.id.course_code);
            courseName = itemView.findViewById(R.id.course_name);
            batch = itemView.findViewById(R.id.batch);
            section = itemView.findViewById(R.id.section);
            btnGroupParticipation = itemView.findViewById(R.id.btn_group_participation);
            courseGroupRelativeLayout = itemView.findViewById(R.id.course_group_relative_layout);
        }
    }
}
