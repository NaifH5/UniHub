package com.tongteacrew.unihub;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.RepliesViewHolder> {

    Context context;
    ArrayList<Replies> replies;

    public RepliesAdapter(Context context, ArrayList<Replies> replies) {
        this.context = context;
        this.replies = replies;
    }

    @Override
    public RepliesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RepliesViewHolder(LayoutInflater.from(context).inflate(R.layout.card_replies, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RepliesAdapter.RepliesViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        Glide.with(context)
                .load(replies.get(index).replierProfilePicture)
                .error(R.drawable.icon_photo)
                .placeholder(R.drawable.icon_photo)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        Glide.with(context).load(resource).circleCrop().into(holder.replierProfilePicture);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        holder.replierProfilePicture.setImageDrawable(placeholder);
                    }
                });

        holder.replierName.setText(replies.get(index).replierName);
        holder.reply.setText(replies.get(index).reply);
    }

    @Override
    public int getItemCount() {
        return replies.size();
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
