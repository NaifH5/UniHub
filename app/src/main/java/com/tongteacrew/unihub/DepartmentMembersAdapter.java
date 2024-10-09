package com.tongteacrew.unihub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DepartmentMembersAdapter extends RecyclerView.Adapter<DepartmentMembersAdapter.DepartmentMembersViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> departmentMembers;

    public DepartmentMembersAdapter(Context context, ArrayList<ArrayList<String>> departmentMembers) {
        this.context = context;
        this.departmentMembers = departmentMembers;
    }

    @Override
    public DepartmentMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DepartmentMembersViewHolder(LayoutInflater.from(context).inflate(R.layout.card_department_members, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentMembersViewHolder holder, int position) {

        holder.userName.setText(departmentMembers.get(position).get(0));
        holder.userPosition.setText(departmentMembers.get(position).get(1));
    }

    @Override
    public int getItemCount() {
        return departmentMembers.size();
    }

    public static class DepartmentMembersViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userPosition;

        public DepartmentMembersViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userPosition = itemView.findViewById(R.id.user_position);
        }
    }
}