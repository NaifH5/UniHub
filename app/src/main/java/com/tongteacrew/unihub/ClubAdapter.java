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

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ClubViewHolder> {

    Context context;
    ArrayList<String[]> selectedClubs;
    int deleteButtonVisibility;

    public ClubAdapter(Context context, ArrayList<String[]> selectedClubs, int deleteButtonVisibility) {
        this.context = context;
        this.selectedClubs = selectedClubs;
        this.deleteButtonVisibility = deleteButtonVisibility;
    }

    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClubViewHolder(LayoutInflater.from(context).inflate(R.layout.card_club, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {

        holder.clubName.setText(selectedClubs.get(position)[0]);
        holder.clubDesignation.setText(selectedClubs.get(position)[1]);
        holder.deleteSection.setVisibility(deleteButtonVisibility);

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedClubs.remove(holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedClubs.size();
    }

    public static class ClubViewHolder extends RecyclerView.ViewHolder {

        TextView clubName, clubDesignation;
        ImageButton btnDelete;
        LinearLayout deleteSection;

        public ClubViewHolder(@NonNull View itemView) {
            super(itemView);
            clubName = itemView.findViewById(R.id.club_name);
            clubDesignation = itemView.findViewById(R.id.club_designation);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            deleteSection = itemView.findViewById(R.id.delete_section);
        }
    }
}
