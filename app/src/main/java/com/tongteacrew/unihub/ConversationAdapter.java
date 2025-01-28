package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    Context context;
    ArrayList<Map<String, Object>> user;

    public ConversationAdapter(Context context, ArrayList<Map<String, Object>> user) {
        this.context = context;
        this.user = user;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(LayoutInflater.from(context).inflate(R.layout.card_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationAdapter.ConversationViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        if(user.get(index).containsKey("profilePicture")) {

            holder.progressIndicator.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(user.get(index).get("profilePicture"))
                    .error(R.drawable.icon_default_profile)
                    .placeholder(R.drawable.icon_default_profile)
                    .into(new CustomTarget<Drawable>(){

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            Glide.with(context).load(resource).circleCrop().into(holder.userProfilePicture);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            holder.userProfilePicture.setImageDrawable(placeholder);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            holder.progressIndicator.setVisibility(View.GONE);
                        }
                    });
        }

        if(user.get(index).containsKey("fullName")) {
            holder.userName.setText(String.valueOf(user.get(index).get("fullName")));
        }

        if(user.get(index).containsKey("lastMessagedAt")) {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(String.valueOf(user.get(index).get("lastMessagedAt")));
        }
        else {
            holder.time.setVisibility(View.GONE);
        }

        if(user.get(index).containsKey("isOnline")) {
            if((Boolean) user.get(index).get("isOnline")) {
                holder.onlineStatus.setVisibility(View.VISIBLE);
            }
            else {
                holder.onlineStatus.setVisibility(View.GONE);
            }
        }

        holder.chatRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userData", (Serializable) user.get(index));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        ImageView userProfilePicture;
        TextView userName, time;
        CircularProgressIndicator progressIndicator;
        RelativeLayout chatRelativeLayout, onlineStatus;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfilePicture = itemView.findViewById(R.id.user_profile_picture);
            userName = itemView.findViewById(R.id.user_name);
            time = itemView.findViewById(R.id.time);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
            chatRelativeLayout = itemView.findViewById(R.id.chat_relative_layout);
            onlineStatus = itemView.findViewById(R.id.online_status);
        }
    }
}
