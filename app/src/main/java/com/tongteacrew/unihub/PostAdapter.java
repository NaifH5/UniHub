package com.tongteacrew.unihub;

import static java.lang.Math.min;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    Context context;
    ArrayList<Map<String, Object>> departmentPosts;

    public PostAdapter(Context context, ArrayList<Map<String, Object>> departmentPosts) {
        this.context = context;
        this.departmentPosts = departmentPosts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater.from(context).inflate(R.layout.card_department_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        int index = departmentPosts.size()-position-1;
        holder.progressIndicator.setVisibility(View.VISIBLE);

        if(departmentPosts.get(index).containsKey("profilePicture")) {
            Glide.with(context)
                    .load(departmentPosts.get(index).get("profilePicture"))
                    .error(R.drawable.icon_photo)
                    .placeholder(R.drawable.icon_photo)
                    .into(new CustomTarget<Drawable>(){

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            Glide.with(context).load(resource).circleCrop().into(holder.posterProfilePicture);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            holder.progressIndicator.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.progressIndicator.setVisibility(View.GONE);
                            holder.posterProfilePicture.setImageDrawable(placeholder);
                        }
                    });
        }
        else {
            holder.progressIndicator.setVisibility(View.GONE);
        }

        holder.posterName.setText(String.valueOf(departmentPosts.get(index).get("fullName")));
        holder.postTime.setText(String.valueOf(departmentPosts.get(index).get("time")));
        holder.post.setText(String.valueOf(departmentPosts.get(index).get("text")));

        if(String.valueOf(departmentPosts.get(index).get("accountType")).equals("student")) {
            holder.textAccountType.setText("Student");
        }
        else {
            holder.textAccountType.setText("Faculty Member");
        }

        getMedias(String.valueOf(departmentPosts.get(index).get("postId")), new FirebaseCallback() {
            @Override
            public void onCallback(Object data) {

                if(data!=null) {

                    ArrayList<String> medias = (ArrayList<String>) data;
                    MediaGridAdapter mediaGridAdapter = new MediaGridAdapter(context, medias);
                    holder.mediaGridview.setAdapter(mediaGridAdapter);

                    // To dynamically set the width of the medias
                    if(medias.size()>0) {

                        holder.mediaGridview.setVisibility(View.VISIBLE);

                        holder.mediaGridview.post(new Runnable() {
                            @Override
                            public void run() {

                                int rowNum = medias.size()/4 + min(1, medias.size()%4);
                                int arrayListSize = medias.size();
                                int height = (holder.mediaGridview.getWidth()-5*(min(arrayListSize, 4)-1))/min(arrayListSize, 4);

                                holder.mediaGridview.getLayoutParams().height = height*rowNum+5*(rowNum-1);
                                holder.mediaGridview.setNumColumns(min(arrayListSize, 4));
                            }
                        });
                    }
                    else {
                        holder.mediaGridview.setVisibility(View.GONE);
                    }
                }
            }
        });

        if((Boolean) departmentPosts.get(index).get("approval")) {
            holder.notApproved.setVisibility(View.GONE);
        }
        else {
            holder.linearLayoutComment.setVisibility(View.GONE);
            holder.btnOptions.setVisibility(View.GONE);
        }

        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostRepliesActivity.class);
                context.startActivity(intent);
            }
        });

        holder.btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.post_overflow_menu, popup.getMenu());
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return departmentPosts.size();
    }

    void getMedias(String postId, FirebaseCallback callback) {

        DatabaseReference mediaReference = rootReference.child("medias").child(postId);
        mediaReference.keepSynced(true);

        mediaReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mediaReference.removeEventListener(this);
                ArrayList<String> medias = new ArrayList<>();

                for(DataSnapshot s : snapshot.getChildren()) {

                    medias.add(String.valueOf(s.getValue()));

                    if(medias.size()==snapshot.getChildrenCount()) {
                        callback.onCallback(medias);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mediaReference.removeEventListener(this);
                callback.onCallback(null);
            }
        });
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
