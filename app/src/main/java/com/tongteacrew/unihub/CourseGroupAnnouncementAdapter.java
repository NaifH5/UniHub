package com.tongteacrew.unihub;

import static java.lang.Math.min;

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
import java.util.Map;
import java.util.Objects;

public class CourseGroupAnnouncementAdapter extends RecyclerView.Adapter<CourseGroupAnnouncementAdapter.CourseGroupAnnouncementViewHolder> {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    Context context;
    ArrayList<Map<String, Object>> announcements;
    String myAccountType, courseGroupId;

    public CourseGroupAnnouncementAdapter(Context context, ArrayList<Map<String, Object>> announcements, String myAccountType, String courseGroupId) {
        this.context = context;
        this.announcements = announcements;
        this.myAccountType = myAccountType;
        this.courseGroupId = courseGroupId;
    }

    @NonNull
    @Override
    public CourseGroupAnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CourseGroupAnnouncementViewHolder(LayoutInflater.from(context).inflate(R.layout.card_group_announcement, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CourseGroupAnnouncementViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        if(announcements.get(index).containsKey("profilePicture")) {
            Glide.with(context)
                    .load(announcements.get(index).get("profilePicture"))
                    .error(R.drawable.icon_photo)
                    .placeholder(R.drawable.icon_photo)
                    .into(new CustomTarget<Drawable>(){

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            Glide.with(context).load(resource).circleCrop().into(holder.posterProfilePicture);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            holder.progressIndicator.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            holder.posterProfilePicture.setImageDrawable(placeholder);
                        }
                    });
        }
        else {
            holder.progressIndicator.setVisibility(View.GONE);
        }

        holder.posterName.setText(String.valueOf(announcements.get(index).get("fullName")));
        holder.postTime.setText(getDate((Long)announcements.get(index).get("time"))+" "+getTime((Long)announcements.get(index).get("time")));
        holder.message.setText(String.valueOf(announcements.get(index).get("text")));
        holder.queryCount.setText(String.valueOf(announcements.get(index).get("queriesCount")));

        long time = (long) announcements.get(index).get("schedule");

        if(time!=0L) {
            holder.deadline.setText(String.format("Schedule: %s", getDate((Long)announcements.get(index).get("schedule"))));
            holder.deadline.setVisibility(View.VISIBLE);
        }

        if(myAccountType.equals("student") && announcements.get(index).get("announcementType").equals("assignment")) {
            holder.btnSubmitAssignment.setVisibility(View.VISIBLE);
        }
        else if(announcements.get(index).get("announcementType").equals("assignment")) {
            holder.btnCheckSubmissions.setVisibility(View.VISIBLE);
        }

        holder.btnQueries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QueriesActivity.class);
                intent.putExtra("courseGroupId", courseGroupId);
                intent.putExtra("announcementId", String.valueOf(announcements.get(index).get("announcementId")));
                context.startActivity(intent);
            }
        });

        holder.btnSubmitAssignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SubmitAssignmentActivity.class);
                intent.putExtra("courseGroupId", courseGroupId);
                intent.putExtra("announcementId", String.valueOf(announcements.get(index).get("announcementId")));
                context.startActivity(intent);
            }
        });

        holder.btnCheckSubmissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CheckSubmissionsActivity.class);
                intent.putExtra("courseGroupId", courseGroupId);
                intent.putExtra("announcementId", String.valueOf(announcements.get(index).get("announcementId")));
                context.startActivity(intent);
            }
        });

        getMedias(String.valueOf(announcements.get(index).get("announcementId")), new CompletionCallback() {
            @Override
            public void onCallback(Object data) {

                if(data!=null) {

                    ArrayList<String> medias = (ArrayList<String>) data;
                    MediaGridAdapter mediaGridAdapter = new MediaGridAdapter(context, medias);
                    holder.mediaGridview.setAdapter(mediaGridAdapter);

                    // To dynamically set the width of the medias
                    if(medias.size()>0) {

                        holder.mediaGridview.setVisibility(View.VISIBLE);

                        holder.mediaGridview.post(new Runnable() {
                            @Override
                            public void run() {

                                int rowNum = medias.size()/4 + min(1, medias.size()%4);
                                int arrayListSize = medias.size();
                                int height = (holder.mediaGridview.getWidth()-5*(min(arrayListSize, 4)-1))/min(arrayListSize, 4);

                                holder.mediaGridview.getLayoutParams().height = height*rowNum+5*(rowNum-1);
                                holder.mediaGridview.setNumColumns(min(arrayListSize, 4));
                            }
                        });
                    }
                    else {
                        holder.mediaGridview.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    void getMedias(String announcementId, CompletionCallback callback) {

        DatabaseReference mediaReference = rootReference.child("courseMedias").child(announcementId);
        mediaReference.keepSynced(true);

        mediaReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mediaReference.removeEventListener(this);
                ArrayList<String> medias = new ArrayList<>();

                for(DataSnapshot s : snapshot.getChildren()) {

                    medias.add(String.valueOf(s.getValue()));

                    if(medias.size()==snapshot.getChildrenCount()) {
                        callback.onCallback(medias);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mediaReference.removeEventListener(this);
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

    public static class CourseGroupAnnouncementViewHolder extends RecyclerView.ViewHolder {

        GridView mediaGridview;
        ImageView posterProfilePicture;
        ImageButton btnQueries;
        Button btnSubmitAssignment, btnCheckSubmissions;
        TextView posterName, postTime, message, deadline, queryCount;
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
            mediaGridview = itemView.findViewById(R.id.media_gridview);
            queryCount = itemView.findViewById(R.id.comment_count);
        }
    }
}
