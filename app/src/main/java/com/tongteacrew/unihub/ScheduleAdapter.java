package com.tongteacrew.unihub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> schedules;

    public ScheduleAdapter(Context context, ArrayList<ArrayList<String>> schedules) {
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

        holder.courseCode.setText(schedules.get(index).get(0));
        holder.date.setText(schedules.get(index).get(1));
        holder.message.setText(schedules.get(index).get(2));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        TextView courseCode, date, message;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCode = itemView.findViewById(R.id.course_code);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
        }
    }
}
