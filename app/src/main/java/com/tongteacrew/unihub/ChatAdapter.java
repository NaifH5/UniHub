package com.tongteacrew.unihub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Context context;
    ArrayList<Map<String, Object>> messages;
    String myID = mAuth.getCurrentUser().getUid();

    public ChatAdapter(Context context, ArrayList<Map<String, Object>> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==0) {
            View v = LayoutInflater.from(context).inflate(R.layout.card_message_sent, parent, false);
            return new ChatViewHolder(v);
        }
        else {
            View v = LayoutInflater.from(context).inflate(R.layout.card_message_received, parent, false);
            return new ChatViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ChatViewHolder holder, int position) {

        if(Objects.equals(messages.get(position).get("sender"), myID)) {

            holder.messageSent.setText(messages.get(position).get("text").toString());
            holder.timeSent.setText(messages.get(position).get("time").toString());
            holder.dateSent.setText(messages.get(position).get("date").toString());
            boolean isSent = (boolean) messages.get(position).get("isSent");

            if(isSent) {
                holder.delivered.setVisibility(View.VISIBLE);
            }
            else {
                holder.delivered.setVisibility(View.GONE);
            }
        }
        else {
            holder.messageReceived.setText(messages.get(position).get("text").toString());
            holder.timeReceived.setText(messages.get(position).get("time").toString());
            holder.dateReceived.setText(messages.get(position).get("date").toString());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

        if(Objects.equals(messages.get(position).get("sender"), myID)) {
            return 0;
        }
        else {
            return 1;
        }
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView messageReceived, timeReceived, dateReceived, messageSent, timeSent, dateSent;
        ImageView delivered;

        public ChatViewHolder(@NonNull View itemView) {

            super(itemView);
            messageReceived = itemView.findViewById(R.id.text_received);
            timeReceived = itemView.findViewById(R.id.time_received);
            dateReceived = itemView.findViewById(R.id.date_received);
            messageSent = itemView.findViewById(R.id.text_sent);
            timeSent = itemView.findViewById(R.id.time_sent);
            dateSent = itemView.findViewById(R.id.date_sent);
            delivered = itemView.findViewById(R.id.delivered);
        }
    }
}
