package com.tongteacrew.unihub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    ImageButton btnBack;
    RecyclerView chatRecyclerView;
    ChatAdapter chatAdapter;
    ArrayList<ArrayList<String>> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        generateTestMessages();

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        btnBack = findViewById(R.id.btn_back);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        chatRecyclerView.setHasFixedSize(true);
        chatAdapter = new ChatAdapter(ChatActivity.this, messages);
        chatRecyclerView.setAdapter(chatAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void generateTestMessages() {

        messages = new ArrayList<>();

        ArrayList arrayList = new ArrayList();
        arrayList.add("0");
        arrayList.add("Hello!");
        arrayList.add("03:00 pm");
        arrayList.add("28 Nov 2024");
        messages.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("1");
        arrayList.add("Hi!");
        arrayList.add("03:10 pm");
        arrayList.add("28 Nov 2024");
        messages.add(arrayList);
    }
}