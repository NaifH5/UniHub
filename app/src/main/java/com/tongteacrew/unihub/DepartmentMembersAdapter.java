package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Map;

public class DepartmentMembersAdapter extends RecyclerView.Adapter<DepartmentMembersAdapter.DepartmentMembersViewHolder> {

    Context context;
    ArrayList<Map<String, Object>> members;

    public DepartmentMembersAdapter(Context context, ArrayList<Map<String, Object>> members) {
        this.context = context;
        this.members = members;
    }

    @Override
    public DepartmentMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DepartmentMembersViewHolder(LayoutInflater.from(context).inflate(R.layout.card_department_members, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentMembersViewHolder holder, int position) {

        int index = holder.getAdapterPosition();
        holder.userName.setText(String.valueOf(members.get(index).get("fullName")));

        if(String.valueOf(members.get(index).get("accountType")).equals("student")) {
            holder.userPosition.setText("Student");
        }
        else {
            holder.userPosition.setText("Faculty Member");
        }

        if(members.get(index).containsKey("profilePicture")) {

            holder.progressIndicator.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(String.valueOf(members.get(index).get("profilePicture")))
                    .error(R.drawable.icon_photo)
                    .placeholder(R.drawable.icon_photo)
                    .into(new CustomTarget<Drawable>(){

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            Glide.with(context).load(resource).circleCrop().into(holder.profilePicture);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            holder.progressIndicator.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            holder.profilePicture.setImageDrawable(placeholder);
                        }
                    });
        }

        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("id", String.valueOf(members.get(index).get("id")));
                context.startActivity(intent);
            }
        });

        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("id", String.valueOf(members.get(index).get("id")));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class DepartmentMembersViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePicture;
        TextView userName, userPosition;
        CircularProgressIndicator progressIndicator;

        public DepartmentMembersViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.user_profile_picture);
            userName = itemView.findViewById(R.id.user_name);
            userPosition = itemView.findViewById(R.id.user_position);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
        }
    }
}