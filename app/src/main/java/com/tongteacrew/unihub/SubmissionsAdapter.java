package com.tongteacrew.unihub;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

public class SubmissionsAdapter extends RecyclerView.Adapter<SubmissionsAdapter.SubmissionsViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> submissions;

    public SubmissionsAdapter(Context context, ArrayList<ArrayList<String>> submissions) {
        this.context = context;
        this.submissions = submissions;
    }

    @NonNull
    @Override
    public SubmissionsAdapter.SubmissionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubmissionsViewHolder(LayoutInflater.from(context).inflate(R.layout.card_assignment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionsAdapter.SubmissionsViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        Glide.with(context)
                .load(submissions.get(index).get(0))
                .error(R.drawable.icon_default_profile)
                .placeholder(R.drawable.icon_default_profile)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        Glide.with(context).load(resource).apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(40))).into(holder.thumbnail);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        holder.thumbnail.setImageDrawable(placeholder);
                    }
                });

        holder.name.setText(submissions.get(index).get(1));
        holder.id.setText(submissions.get(index).get(2));
        holder.time.setText(submissions.get(index).get(3));

        holder.assignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Open PDF File.");
            }
        });
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    public static class SubmissionsViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView name, id, time;
        RelativeLayout assignment;
        CircularProgressIndicator progressIndicator;

        public SubmissionsViewHolder(@NonNull View itemView) {
            super(itemView);
            assignment = itemView.findViewById(R.id.assignment);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            name = itemView.findViewById(R.id.name);
            id = itemView.findViewById(R.id.id);
            time = itemView.findViewById(R.id.time);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
        }
    }
}
