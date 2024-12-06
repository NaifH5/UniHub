package com.tongteacrew.unihub;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.GroupMembersViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> members;

    public GroupMembersAdapter(Context context, ArrayList<ArrayList<String>> members) {
        this.context = context;
        this.members = members;
    }

    @NonNull
    @Override
    public GroupMembersAdapter.GroupMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupMembersViewHolder(LayoutInflater.from(context).inflate(R.layout.card_group_member, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMembersAdapter.GroupMembersViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        Glide.with(context)
                .load(members.get(index).get(0))
                .error(R.drawable.icon_default_profile)
                .placeholder(R.drawable.icon_default_profile)
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

        holder.name.setText(members.get(index).get(1));
        holder.id.setText(members.get(index).get(2));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class GroupMembersViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePicture;
        TextView name, id;
        ImageButton btnOptions;
        CircularProgressIndicator progressIndicator;

        public GroupMembersViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            name = itemView.findViewById(R.id.name);
            id = itemView.findViewById(R.id.id);
            btnOptions = itemView.findViewById(R.id.btn_options);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
        }
    }
}
