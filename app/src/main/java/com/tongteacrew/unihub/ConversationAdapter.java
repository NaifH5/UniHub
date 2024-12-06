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

import java.util.ArrayList;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> conversation;

    public ConversationAdapter(Context context, ArrayList<ArrayList<String>> conversation) {
        this.context = context;
        this.conversation = conversation;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(LayoutInflater.from(context).inflate(R.layout.card_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationAdapter.ConversationViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        Glide.with(context)
                .load(conversation.get(index).get(0))
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
                });

        holder.userName.setText(conversation.get(index).get(1));
        holder.time.setText(conversation.get(index).get(2));

        holder.chatRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversation.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        ImageView userProfilePicture;
        TextView userName, time;
        CircularProgressIndicator progressIndicator;
        RelativeLayout chatRelativeLayout;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfilePicture = itemView.findViewById(R.id.user_profile_picture);
            userName = itemView.findViewById(R.id.user_name);
            time = itemView.findViewById(R.id.time);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
            chatRelativeLayout = itemView.findViewById(R.id.chat_relative_layout);
        }
    }
}
