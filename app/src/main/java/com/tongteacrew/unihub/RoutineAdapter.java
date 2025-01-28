package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    Context context;
    ArrayList<Map<String, String>> routine;
    String myAccountType;
    long departmentId;

    public RoutineAdapter(Context context, ArrayList<Map<String, String>> routine, String myAccountType, long departmentId) {
        this.context = context;
        this.routine = routine;
        this.myAccountType = myAccountType;
        this.departmentId = departmentId;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoutineViewHolder(LayoutInflater.from(context).inflate(R.layout.card_routine, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {

        int index = position;

        holder.btnSession.setText(routine.get(index).entrySet().iterator().next().getKey());

        if(myAccountType.equals("student")) {
            holder.linearLayout.setVisibility(View.GONE);
        }

        holder.btnSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(routine.get(index).entrySet().iterator().next().getValue()));
                context.startActivity(intent);
            }
        });

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddRoutineActivity.class);
                intent.putExtra("departmentId", departmentId);
                intent.putExtra("sessionData", (Serializable) routine.get(index));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routine.size();
    }

    public static class RoutineViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        Button btnSession;
        ImageButton btnEdit;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            btnSession = itemView.findViewById(R.id.btn_session);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }
    }
}
