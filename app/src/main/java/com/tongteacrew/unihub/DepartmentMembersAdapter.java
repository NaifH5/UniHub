package com.tongteacrew.unihub;

import android.content.Context;
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

public class DepartmentMembersAdapter extends RecyclerView.Adapter<DepartmentMembersAdapter.DepartmentMembersViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> departmentMembers;

    public DepartmentMembersAdapter(Context context, ArrayList<ArrayList<String>> departmentMembers) {
        this.context = context;
        this.departmentMembers = departmentMembers;
    }

    @Override
    public DepartmentMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DepartmentMembersViewHolder(LayoutInflater.from(context).inflate(R.layout.card_department_members, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentMembersViewHolder holder, int position) {

        holder.userName.setText(departmentMembers.get(position).get(0));
        holder.userPosition.setText(departmentMembers.get(position).get(1));

        Glide.with(context)
                .load(departmentMembers.get(position).get(2))
                .error(R.drawable.icon_photo)
                .placeholder(R.drawable.icon_photo)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        Glide.with(context).load(resource).circleCrop().into(holder.profilePicture);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        holder.profilePicture.setImageDrawable(placeholder);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return departmentMembers.size();
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