package com.tongteacrew.unihub;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class ClubMembersAdapter extends RecyclerView.Adapter<ClubMembersAdapter.ClubMembersViewHolder> {

    Context context;
    ArrayList<Map<String, Object>> members;

    public ClubMembersAdapter(Context context, ArrayList<Map<String, Object>> members) {
        this.context = context;
        this.members = members;
    }

    @NonNull
    @Override
    public ClubMembersAdapter.ClubMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClubMembersViewHolder(LayoutInflater.from(context).inflate(R.layout.card_club_member, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ClubMembersAdapter.ClubMembersViewHolder holder, int position) {

        if(members.get(position).containsKey("profilePicture")) {

            holder.progressIndicator.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(members.get(position).get("profilePicture"))
                    .error(R.drawable.icon_default_profile)
                    .placeholder(R.drawable.icon_default_profile)
                    .into(new CustomTarget<Drawable>(){

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            Glide.with(context).load(resource).circleCrop().into(holder.profilePicture);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            holder.profilePicture.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            holder.profilePicture.setImageDrawable(placeholder);
                        }
                    });
        }
        else {
            holder.progressIndicator.setVisibility(View.GONE);
        }

        holder.name.setText(String.valueOf(members.get(position).get("fullName")));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class ClubMembersViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView profilePicture;
        CircularProgressIndicator progressIndicator;

        public ClubMembersViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
        }
    }
}
