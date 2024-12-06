package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

public class CourseGroupAnnouncementAdapter extends RecyclerView.Adapter<CourseGroupAnnouncementAdapter.CourseGroupAnnouncementViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> announcements;

    public CourseGroupAnnouncementAdapter(Context context, ArrayList<ArrayList<String>> announcements) {
        this.context = context;
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public CourseGroupAnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CourseGroupAnnouncementViewHolder(LayoutInflater.from(context).inflate(R.layout.card_group_announcement, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CourseGroupAnnouncementViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        Glide.with(context)
                .load(announcements.get(index).get(0))
                .error(R.drawable.icon_photo)
                .placeholder(R.drawable.icon_photo)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        Glide.with(context).load(resource).circleCrop().into(holder.posterProfilePicture);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        holder.posterProfilePicture.setImageDrawable(placeholder);
                    }
                });

        holder.posterName.setText(announcements.get(index).get(1));
        holder.postTime.setText(announcements.get(index).get(2));
        holder.message.setText(announcements.get(index).get(3));
        holder.deadline.setText(String.format("Deadline: %s", announcements.get(index).get(4)));

        holder.btnQueries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QueriesActivity.class);
                context.startActivity(intent);
            }
        });

        holder.btnSubmitAssignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SubmitAssignmentActivity.class);
                context.startActivity(intent);
            }
        });

        holder.btnCheckSubmissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CheckSubmissionsActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public static class CourseGroupAnnouncementViewHolder extends RecyclerView.ViewHolder {

        ImageView posterProfilePicture;
        ImageButton btnQueries;
        Button btnSubmitAssignment, btnCheckSubmissions;
        TextView posterName, postTime, message, deadline;
        CircularProgressIndicator progressIndicator;

        public CourseGroupAnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            btnQueries = itemView.findViewById(R.id.btn_queries);
            btnSubmitAssignment = itemView.findViewById(R.id.btn_submit_assignment);
            btnCheckSubmissions = itemView.findViewById(R.id.btn_check_submissions);
            posterProfilePicture = itemView.findViewById(R.id.profile_picture);
            posterName = itemView.findViewById(R.id.name);
            postTime = itemView.findViewById(R.id.time);
            message = itemView.findViewById(R.id.message);
            deadline = itemView.findViewById(R.id.deadline);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
        }
    }
}
