package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class AllCoursesGroupAdapter extends RecyclerView.Adapter<AllCoursesGroupAdapter.AllCoursesGroupViewHolder> {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    Context context;
    ArrayList<Map<String, String>> courseGroups;
    String selectedSession;
    String myAccountType;

    public AllCoursesGroupAdapter(Context context, ArrayList<Map<String, String>> courseGroups, String selectedSession, String myAccountType) {
        this.context = context;
        this.courseGroups = courseGroups;
        this.selectedSession = selectedSession;
        this.myAccountType = myAccountType;
    }

    @NonNull
    @Override
    public AllCoursesGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AllCoursesGroupViewHolder(LayoutInflater.from(context).inflate(R.layout.card_course_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AllCoursesGroupAdapter.AllCoursesGroupViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        holder.courseCode.setText(courseGroups.get(index).get("courseCode"));
        holder.batch.setText(String.format("Batch %s", courseGroups.get(index).get("batch")));
        holder.section.setText(String.format("Section %s", courseGroups.get(index).get("section")));

        if(courseGroups.get(index).containsKey("courseName")) {
            holder.courseName.setText(courseGroups.get(index).get("courseName"));
            holder.courseName.setVisibility(View.VISIBLE);
        }

        boolean isMember = Boolean.parseBoolean(courseGroups.get(index).get("isMember"));

        if(isMember) {
            holder.btnGroupParticipation.setText("Leave");
        }
        else {
            holder.btnGroupParticipation.setText("Join");
        }

        holder.btnGroupParticipation.setVisibility(View.VISIBLE);

        holder.btnGroupParticipation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isMember) {
                    leaveGroup(index, holder.btnGroupParticipation);
                }
                else {
                    joinGroup(index, holder.btnGroupParticipation);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseGroups.size();
    }

    void joinGroup(int index, Button button) {

        String courseCode = courseGroups.get(index).get("courseCode");
        String batch = courseGroups.get(index).get("batch");
        String section = courseGroups.get(index).get("section");
        String courseName = courseGroups.get(index).getOrDefault("courseName", "");
        String groupId = selectedSession+"_"+batch+"_"+section+"_"+courseCode;

        Map<String, Object> updates = new HashMap<>();
        updates.put("myCourses/"+user.getUid()+"/"+selectedSession+"/"+courseCode+"/"+batch+"/"+section, courseName);
        updates.put("courseGroupMembers/"+groupId+"/"+myAccountType+"/"+user.getUid(), true);

        rootReference.updateChildren(updates).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                courseGroups.get(index).put("isMember", "true");
                button.setText("Leave");
                notifyDataSetChanged();
            }
        });
    }

    void leaveGroup(int index, Button button) {

        String groupId = selectedSession + "_" + courseGroups.get(index).get("batch") + "_"
                + courseGroups.get(index).get("section") + "_"
                + courseGroups.get(index).get("courseCode");

        Map<String, Object> updates = new HashMap<>();
        updates.put("myCourses/" + user.getUid() + "/" + selectedSession + "/"
                + courseGroups.get(index).get("courseCode") + "/"
                + courseGroups.get(index).get("batch") + "/"
                + courseGroups.get(index).get("section"), null);

        updates.put("courseGroupMembers/"+groupId+"/"+myAccountType+"/"+user.getUid(), null);

        rootReference.updateChildren(updates).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                courseGroups.get(index).put("isMember", "false");
                button.setText("Join");
                notifyDataSetChanged();
            }
        });
    }

    public static class AllCoursesGroupViewHolder extends RecyclerView.ViewHolder {

        TextView courseCode, courseName, batch, section;
        Button btnGroupParticipation;
        RelativeLayout courseGroupRelativeLayout;

        public AllCoursesGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCode = itemView.findViewById(R.id.course_code);
            courseName = itemView.findViewById(R.id.course_name);
            batch = itemView.findViewById(R.id.batch);
            section = itemView.findViewById(R.id.section);
            btnGroupParticipation = itemView.findViewById(R.id.btn_group_participation);
            courseGroupRelativeLayout = itemView.findViewById(R.id.course_group_relative_layout);
        }
    }
}
