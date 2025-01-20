package com.tongteacrew.unihub;

import static java.lang.Math.min;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    Context context;
    ArrayList<Map<String, Object>> departmentPosts;
    CompletionCallback refresh;
    String myAccountType;

    public PostAdapter(Context context, String myAccountType, ArrayList<Map<String, Object>> departmentPosts, CompletionCallback refresh) {
        this.context = context;
        this.departmentPosts = departmentPosts;
        this.refresh = refresh;
        this.myAccountType = myAccountType;
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

        holder.commentCount.setText(String.valueOf(departmentPosts.get(index).get("commentCount")));

        if((Boolean) departmentPosts.get(index).get("approval")) {
            holder.notApproved.setVisibility(View.GONE);
        }
        else if(String.valueOf(departmentPosts.get(index).get("posterId")).equals(user.getUid())) {
            holder.linearLayoutComment.setVisibility(View.GONE);
        }
        else {
            holder.linearLayoutComment.setVisibility(View.GONE);
            holder.btnOptions.setVisibility(View.GONE);
        }

        if(String.valueOf(departmentPosts.get(index).get("accountType")).equals("student")) {

            holder.textAccountType.setText("Student");

            if(String.valueOf(departmentPosts.get(index).get("posterId")).equals(user.getUid())) {
                holder.pendingPostText.setText("Your post has not been approved yet.");
                holder.linearLayoutAccept.setVisibility(View.GONE);
                holder.linearLayoutReject.setVisibility(View.GONE);
            }
        }
        else {
            holder.textAccountType.setText("Faculty Member");
        }

        if(myAccountType.equals("student") && !String.valueOf(departmentPosts.get(index).get("posterId")).equals(user.getUid())) {
            holder.btnOptions.setVisibility(View.GONE);
        }

        getMedias(String.valueOf(departmentPosts.get(index).get("postId")), new CompletionCallback() {
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

        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostCommentsActivity.class);
                intent.putExtra("postId", String.valueOf(departmentPosts.get(index).get("postId")));
                context.startActivity(intent);
            }
        });

        holder.btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(context, v);

                if(String.valueOf(departmentPosts.get(index).get("posterId")).equals(user.getUid())) {
                    popup.getMenuInflater().inflate(R.menu.edit_delete_post_overflow_menu, popup.getMenu());
                }
                else if(myAccountType.equals("facultyMember")) {
                    popup.getMenuInflater().inflate(R.menu.delete_post_overflow_menu, popup.getMenu());
                }

                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId()==R.id.edit_post) {
                            Intent intent = new Intent(context, CreatePostActivity.class);
                            intent.putExtra("postId", String.valueOf(departmentPosts.get(index).get("postId")));
                            intent.putExtra("text", String.valueOf(departmentPosts.get(index).get("text")));
                            context.startActivity(intent);
                        }
                        else if(item.getItemId()==R.id.delete_post) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Confirm Deletion");
                            builder.setMessage("Are you sure you want to delete this post?");

                            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deletePost(String.valueOf(departmentPosts.get(index).get("postId")), String.valueOf(departmentPosts.get(index).get("departmentId")));
                                }
                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }

                        return true;
                    }
                });
            }
        });

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptPost(String.valueOf(departmentPosts.get(index).get("postId")));
            }
        });

        holder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectPost(String.valueOf(departmentPosts.get(index).get("postId")), String.valueOf(departmentPosts.get(index).get("departmentId")), index);
            }
        });

        holder.posterProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("id", String.valueOf(departmentPosts.get(index).get("posterId")));
                context.startActivity(intent);
            }
        });

        holder.posterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("id", String.valueOf(departmentPosts.get(index).get("posterId")));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return departmentPosts.size();
    }

    void deletePost(String postId, String departmentId) {

        DatabaseReference removeReference = rootReference.child("departmentPosts").child(departmentId).child(postId);
        removeReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                DatabaseReference removeReference = rootReference.child("posts").child(postId);
                removeReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DatabaseReference removeReference = rootReference.child("medias").child(postId);
                        removeReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                DatabaseReference removeReference = rootReference.child("comments").child(postId);
                                removeReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "Post deleted!", Toast.LENGTH_SHORT).show();
                                        refresh.onCallback(true);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    void getMedias(String postId, CompletionCallback callback) {

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

    void acceptPost(String postId) {

        setTimeStamp(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                if(data!=null) {

                    long time = (long) data;
                    rootReference.child("posts").child(postId).child("time").setValue(time).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {

                                rootReference.child("posts").child(postId).child("approval").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "Post approved!", Toast.LENGTH_SHORT).show();
                                        refresh.onCallback(true);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    void setTimeStamp(CompletionCallback callback) {

        DatabaseReference timeReference = rootReference.child("timeStamps").child(user.getUid());

        timeReference.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                DatabaseReference timeReference = rootReference.child("timeStamps").child(user.getUid());

                timeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {

                        if(task.isSuccessful()) {
                            callback.onCallback((long) task.getResult().getValue());
                        }
                    }
                });
            }
        });
    }

    void rejectPost(String postId, String departmentId, int index) {

        DatabaseReference postReference = rootReference.child("posts").child(postId);

        postReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                DatabaseReference postReference = rootReference.child("departmentPosts").child(departmentId).child(postId);

                postReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        refresh.onCallback(true);
                        Toast.makeText(context, "Post approval rejected!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView posterProfilePicture;
        TextView posterName, postTime, post, textAccountType, commentCount, pendingPostText;
        GridView mediaGridview;
        RelativeLayout notApproved;
        ImageButton btnOptions, btnComment, btnAccept, btnReject;
        LinearLayout linearLayoutComment, linearLayoutAccept, linearLayoutReject;
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
            commentCount = itemView.findViewById(R.id.comment_count);
            linearLayoutComment = itemView.findViewById(R.id.linear_layout_comment);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
            pendingPostText = itemView.findViewById(R.id.pending_post_text);
            linearLayoutAccept = itemView.findViewById(R.id.linearLayout3);
            linearLayoutReject = itemView.findViewById(R.id.linearLayout2);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}
