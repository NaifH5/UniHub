package com.tongteacrew.unihub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ButtonListAdapter extends RecyclerView.Adapter<ButtonListAdapter.ButtonListViewHolder> {

    Context context;
    ArrayList<String> courseOffering;

    public ButtonListAdapter(Context context, ArrayList<String> courseOffering) {
        this.context = context;
        this.courseOffering = courseOffering;
    }

    @NonNull
    @Override
    public ButtonListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ButtonListViewHolder(LayoutInflater.from(context).inflate(R.layout.card_button_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonListViewHolder holder, int position) {

        holder.courseOffering.setText(courseOffering.get(position));
    }

    @Override
    public int getItemCount() {
        return courseOffering.size();
    }

    public static class ButtonListViewHolder extends RecyclerView.ViewHolder {

        Button courseOffering;

        public ButtonListViewHolder(@NonNull View itemView) {

            super(itemView);
            courseOffering = itemView.findViewById(R.id.btn_course_offering);
        }
    }
}
