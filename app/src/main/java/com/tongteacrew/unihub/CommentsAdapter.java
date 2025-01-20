package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
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

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.RepliesViewHolder> {

    Context context;
    ArrayList<Map<String, Object>> comments;

    public CommentsAdapter(Context context, ArrayList<Map<String, Object>> comments) {
        this.context = context;
        this.comments = comments;
    }

    @Override
    public RepliesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RepliesViewHolder(LayoutInflater.from(context).inflate(R.layout.card_replies, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.RepliesViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        if(comments.get(index).containsKey("profilePicture")) {

            holder.progressIndicator.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(comments.get(index).get("profilePicture"))
                    .error(R.drawable.icon_photo)
                    .placeholder(R.drawable.icon_photo)
                    .into(new CustomTarget<Drawable>(){

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            Glide.with(context).load(resource).circleCrop().into(holder.replierProfilePicture);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            holder.progressIndicator.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            holder.replierProfilePicture.setImageDrawable(placeholder);
                        }
                    });
        }


        holder.replierName.setText(String.valueOf(comments.get(index).get("posterName")));
        holder.reply.setText(String.valueOf(comments.get(index).get("text")));

        holder.replierProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("id", String.valueOf(comments.get(index).get("posterId")));
                context.startActivity(intent);
            }
        });

        holder.replierName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("id", String.valueOf(comments.get(index).get("posterId")));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class RepliesViewHolder extends RecyclerView.ViewHolder {

        ImageView replierProfilePicture;
        TextView replierName, reply;
        CircularProgressIndicator progressIndicator;

        public RepliesViewHolder(@NonNull View itemView) {
            super(itemView);
            replierProfilePicture = itemView.findViewById(R.id.replier_profile_picture);
            replierName = itemView.findViewById(R.id.replier_name);
            reply = itemView.findViewById(R.id.reply);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
        }
    }
}
