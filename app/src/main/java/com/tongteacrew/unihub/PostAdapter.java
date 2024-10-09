package com.tongteacrew.unihub;

import static java.lang.Math.min;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    Context context;
    ArrayList<DepartmentPost> departmentPosts;

    public PostAdapter(Context context, ArrayList<DepartmentPost> departmentPosts) {
        this.context = context;
        this.departmentPosts = departmentPosts;
    }

    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater.from(context).inflate(R.layout.card_department_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        Glide.with(context)
                .load(departmentPosts.get(index).posterProfilePicture)
                .error(R.drawable.icon_photo)
                .placeholder(R.drawable.icon_photo)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        Glide.with(context).load(resource).circleCrop().into(holder.posterProfilePicture);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        holder.progressIndicator.setVisibility(View.GONE);
                        holder.posterProfilePicture.setImageDrawable(placeholder);
                    }
                });

        holder.posterName.setText(departmentPosts.get(index).posterName);
        holder.postTime.setText(departmentPosts.get(index).postTime);
        holder.post.setText(departmentPosts.get(index).post);
        holder.textAccountType.setText(departmentPosts.get(index).textAccountType);

        MediaGridAdapter mediaGridAdapter = new MediaGridAdapter(context, departmentPosts.get(index).mediaList);
        holder.mediaGridview.setAdapter(mediaGridAdapter);

        // To dynamically set the width of the medias
        if(departmentPosts.get(index).mediaList.size()>0) {
            holder.mediaGridview.post(new Runnable() {
                @Override
                public void run() {

                    int rowNum = departmentPosts.get(index).mediaList.size()/4 + min(1, departmentPosts.get(index).mediaList.size()%4);
                    int arrayListSize = departmentPosts.get(index).mediaList.size();
                    int height = (holder.mediaGridview.getWidth()-5*(min(arrayListSize, 4)-1))/min(arrayListSize, 4);

                    holder.mediaGridview.getLayoutParams().height = height*rowNum+5*(rowNum-1);
                    holder.mediaGridview.setNumColumns(min(arrayListSize, 4));
                }
            });
        }
        else {
            holder.mediaGridview.setVisibility(View.GONE);
        }

        if(departmentPosts.get(index).adminApproved) {
            holder.notApproved.setVisibility(View.GONE);
        }
        else {
            holder.linearLayoutComment.setVisibility(View.GONE);
            holder.btnOptions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return departmentPosts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView posterProfilePicture;
        TextView posterName, postTime, post, textAccountType;
        GridView mediaGridview;
        RelativeLayout notApproved;
        ImageButton btnOptions, btnComment;
        LinearLayout linearLayoutComment;
        CircularProgressIndicator progressIndicator;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            posterProfilePicture = itemView.findViewById(R.id.poster_profile_picture);
            posterName = itemView.findViewById(R.id.poster_name);
            postTime = itemView.findViewById(R.id.post_time);
            post = itemView.findViewById(R.id.post);
            textAccountType = itemView.findViewById(R.id.text_account_type);
            mediaGridview = itemView.findViewById(R.id.media_gridview);
            notApproved = itemView.findViewById(R.id.not_approved);
            btnOptions = itemView.findViewById(R.id.btn_options);
            btnComment = itemView.findViewById(R.id.btn_comment);
            linearLayoutComment = itemView.findViewById(R.id.linear_layout_comment);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
        }
    }
}
