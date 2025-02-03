package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Map;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    Context context;
    ArrayList<Map<String, Object>> schedules;

    public ScheduleAdapter(Context context, ArrayList<Map<String, Object>> schedules) {
        this.context = context;
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScheduleViewHolder(LayoutInflater.from(context).inflate(R.layout.card_schedule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapter.ScheduleViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        holder.courseCode.setText(String.valueOf(schedules.get(index).get("courseCode")));
        holder.date.setText(String.valueOf(schedules.get(index).get("date")));

        if(schedules.get(index).containsKey("text")) {
            holder.message.setText(String.valueOf(schedules.get(index).get("text")));
        }

        holder.scheduleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] parts = String.valueOf(schedules.get(index).get("courseGroupId")).split("_");
                Intent intent = new Intent(context, CourseGroupActivity.class);
                intent.putExtra("selectedSession", parts[0]);
                intent.putExtra("batch", parts[1]);
                intent.putExtra("section", parts[2]);
                intent.putExtra("courseCode", parts[3]);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        TextView courseCode, date, message;
        RelativeLayout scheduleLayout;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCode = itemView.findViewById(R.id.course_code);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            scheduleLayout = itemView.findViewById(R.id.schedule_layout);
        }
    }
}
