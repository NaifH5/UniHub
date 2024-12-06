package com.tongteacrew.unihub;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    RecyclerView conversationRecyclerView;
    ConversationAdapter conversationAdapter;
    ArrayList<ArrayList<String>> conversation;

    public ChatsFragment() {
        generateConversation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversations, container, false);

        conversationRecyclerView = view.findViewById(R.id.conversation_recycler_view);

        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        conversationRecyclerView.setHasFixedSize(true);
        conversationAdapter = new ConversationAdapter(getContext(), conversation);
        conversationRecyclerView.setAdapter(conversationAdapter);

        return view;
    }

    void generateConversation() {

        conversation = new ArrayList<>();

        ArrayList arrayList = new ArrayList();
        arrayList.add("https://www.pbs.org/newshour/app/uploads/2017/02/GettyImages-200193780-001-1024x768.jpg");
        arrayList.add("Lorem Ipsum Khan");
        arrayList.add("03:00 pm 28 Nov 2024");
        conversation.add(arrayList);

        arrayList = new ArrayList();
        arrayList.add("https://i.pinimg.com/736x/5a/ab/f8/5aabf84d67477f77d3bb8f0fe4cfcd17.jpg");
        arrayList.add("Lorem Ipsum Ahmed");
        arrayList.add("03:10 pm 28 Nov 2024");
        conversation.add(arrayList);
    }
}