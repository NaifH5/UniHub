package com.tongteacrew.unihub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.GroupMembersViewHolder> {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    Context context;
    ArrayList<Map<String, Object>> members;
    String courseGroupId, myAccountType;
    CompletionCallback callback;

    public GroupMembersAdapter(Context context, ArrayList<Map<String, Object>> members, String courseGroupId, String myAccountType, CompletionCallback callback) {
        this.context = context;
        this.members = members;
        this.courseGroupId = courseGroupId;
        this.myAccountType = myAccountType;
        this.callback = callback;
    }

    @NonNull
    @Override
    public GroupMembersAdapter.GroupMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupMembersViewHolder(LayoutInflater.from(context).inflate(R.layout.card_group_member, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMembersAdapter.GroupMembersViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        if(members.get(index).containsKey("profilePicture")) {

            Glide.with(context)
                    .load(members.get(index).get("profilePicture"))
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

        holder.name.setText(String.valueOf(members.get(index).get("fullName")));

        if(String.valueOf(members.get(index).get("accountType")).equals("student")) {
            holder.id.setText(String.valueOf(members.get(index).get("id")));
            holder.id.setVisibility(View.VISIBLE);
        }

        if(!String.valueOf(members.get(index).get("uid")).equals(user.getUid()) && myAccountType.equals("student")) {
            holder.btnOptions.setVisibility(View.GONE);
        }

        holder.btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(context, view);
                boolean isCr = (boolean) members.get(index).get("isCr");

                if(String.valueOf(members.get(index).get("uid")).equals(user.getUid())) {
                    popup.getMenuInflater().inflate(R.menu.leave_group_overflow_menu, popup.getMenu());
                }
                else if(!String.valueOf(members.get(index).get("uid")).equals(user.getUid()) && !isCr) {
                    popup.getMenuInflater().inflate(R.menu.assign_cr_overflow_menu, popup.getMenu());
                }
                else {
                    popup.getMenuInflater().inflate(R.menu.remove_cr_overflow_menu, popup.getMenu());
                }

                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId()==R.id.leave_group) {
                            leaveGroup(index);
                        }
                        else if(item.getItemId()==R.id.assign_cr) {
                            makeCr(index);
                        }
                        else if(item.getItemId()==R.id.remove_cr) {
                            removeFromCr(index);
                        }

                        return true;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    void leaveGroup(int index) {

        String[] parts = courseGroupId.split("_");

        Map<String, Object> updates = new HashMap<>();
        updates.put("myCourses/" + user.getUid() + "/" + parts[0] + "/"
                + parts[3] + "/" + parts[1] + "/" + parts[2], null);

        updates.put("courseGroupMembers/"+courseGroupId+"/"+myAccountType+"/"+user.getUid(), null);
        updates.put("courseGroupMembers/"+courseGroupId+"/"+"classRepresentative"+"/"+user.getUid(), null);

        rootReference.updateChildren(updates).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                callback.onCallback(true);
            }
        });
    }

    void makeCr(int index) {

        DatabaseReference crReference = rootReference.child("courseGroupMembers").child(courseGroupId)
                .child("classRepresentative").child(String.valueOf(members.get(index).get("uid")));

        crReference.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("CR Updated!");
            }
        });
    }

    void removeFromCr(int index) {

        DatabaseReference crReference = rootReference.child("courseGroupMembers").child(courseGroupId)
                .child("classRepresentative").child(String.valueOf(members.get(index).get("uid")));

        crReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("CR Updated!");
            }
        });
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
