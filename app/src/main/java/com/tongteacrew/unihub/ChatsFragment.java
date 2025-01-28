package com.tongteacrew.unihub;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ChatsFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    String myId = user.getUid();
    EditText search;
    RecyclerView conversationRecyclerView;
    ConversationAdapter conversationAdapter;
    ArrayList<Map<String, Object>> messagedUser = new ArrayList<>();
    ArrayList<Map<String, Object>> searchResult = new ArrayList<>();
    ArrayList<Map<String, Object>> allUsers = new ArrayList<>();
    long messagedUserCount=0;

    public ChatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversations, container, false);

        conversationRecyclerView = view.findViewById(R.id.conversation_recycler_view);
        search = view.findViewById(R.id.search);

        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        conversationAdapter = new ConversationAdapter(getContext(), messagedUser);
        conversationRecyclerView.setAdapter(conversationAdapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setInteractionListener();

        return view;
    }

    void setInteractionListener() {

        DatabaseReference interactionReference = rootReference.child("interactions").child(myId);
        interactionReference.keepSynced(true);

        interactionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    messagedUserCount = snapshot.getChildrenCount();

                    for(DataSnapshot s : snapshot.getChildren()) {

                        Map<String, Object> user = new HashMap<>();
                        user.put("id", s.getKey());
                        user.put("messageId", s.getValue());
                        allUsers.add(user);

                        if(allUsers.size()==messagedUserCount) {
                            getMessagedUserDetails(0);
                            getAllUsers();
                            conversationRecyclerView.setAdapter(new ConversationAdapter(getContext(), allUsers));
                        }
                    }
                }
                else {
                    getAllUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void getMessagedUserDetails(int index) {

        String[] account = {"student", "facultyMember"};

        for(int i=0; i<2; i++) {

            final int pos = i;
            DatabaseReference usersReference = rootReference.child(account[i]).child(String.valueOf(allUsers.get(index).get("id")));
            usersReference.keepSynced(true);

            usersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists() && snapshot.getValue()!=null) {

                        Map<String, Object> userDetails = (Map<String, Object>) snapshot.getValue();
                        allUsers.get(index).put("accountType", account[pos]);
                        allUsers.get(index).put("fullName", userDetails.get("fullName"));

                        if(userDetails.containsKey("profilePicture")) {
                            allUsers.get(index).put("profilePicture", userDetails.get("profilePicture"));
                        }

                        if(userDetails.containsKey("deviceToken")) {
                            allUsers.get(index).put("deviceToken", userDetails.get("deviceToken"));
                        }

                        if(index+1<messagedUserCount) {
                            getMessagedUserDetails(index+1);
                        }
                        else {
                            getLastMessageTime(0);
                            setOnlineStatus(0);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if(index+1<messagedUserCount) {
                        getMessagedUserDetails(index+1);
                    }
                }
            });
        }
    }

    void getLastMessageTime(int index) {

        Query timeReference = rootReference.child("chats").child(String.valueOf(allUsers.get(index).get("messageId"))).limitToLast(1);
        timeReference.keepSynced(true);

        timeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    Map<String, Map<String, Object>> time = (Map<String, Map<String, Object>>) snapshot.getValue();
                    Map.Entry<String, Map<String, Object>> entry = time.entrySet().iterator().next();
                    allUsers.get(index).put("lastMessagedAt", getTime(Long.parseLong(entry.getKey()))+" "+getDate(Long.parseLong(entry.getKey())));
                }

                if(index+1<messagedUserCount) {
                    getLastMessageTime(index+1);
                }
                else if(index+1==messagedUserCount) {
                    sortList();
                    getMessagedUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(index+1<messagedUserCount) {
                    getLastMessageTime(index+1);
                }
            }
        });
    }

    void setOnlineStatus(int index) {

        DatabaseReference onlineStatusReference = rootReference.child("onlineStatus").child(String.valueOf(allUsers.get(index).get("id"))).child("isOnline");

        onlineStatusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    allUsers.get(index).put("isOnline", snapshot.getValue());
                }
                else {
                    allUsers.get(index).put("isOnline", false);
                }

                if(index+1<messagedUserCount) {
                    setOnlineStatus(index+1);
                }
                else {
                    conversationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(index+1<messagedUserCount) {
                    setOnlineStatus(index+1);
                }
            }
        });
    }

    void getAllUsers() {

        String[] account = {"student", "facultyMember"};

        for(int i=0; i<2; i++) {

            final int index = i;
            DatabaseReference usersReference = rootReference.child(account[index]);
            usersReference.keepSynced(true);

            usersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    usersReference.removeEventListener(this);

                    if(snapshot.exists()) {

                        for(DataSnapshot s : snapshot.getChildren()) {

                            if(messagedUserCount==0) {

                                Map<String, Object> userDetails = (Map<String, Object>) s.getValue();
                                Map<String, Object> user = new HashMap<>();
                                user.put("accountType", account[index]);
                                user.put("id", s.getKey());
                                user.put("fullName", userDetails.get("fullName"));

                                if(userDetails.containsKey("profilePicture")) {
                                    user.put("profilePicture", userDetails.get("profilePicture"));
                                }

                                if(userDetails.containsKey("deviceToken")) {
                                    user.put("deviceToken", userDetails.get("deviceToken"));
                                }

                                if(!Objects.equals(s.getKey(), myId)) {
                                    allUsers.add(user);
                                }
                            }
                            else {

                                for(int i=0; i<messagedUserCount; i++) {

                                    if(Objects.equals(s.getKey(), String.valueOf(allUsers.get(i).get("id")))) {
                                        break;
                                    }
                                    else if(i==messagedUserCount-1) {

                                        Map<String, Object> userDetails = (Map<String, Object>) s.getValue();
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("accountType", account[index]);
                                        user.put("id", s.getKey());
                                        user.put("fullName", userDetails.get("fullName"));

                                        if(userDetails.containsKey("profilePicture")) {
                                            user.put("profilePicture", userDetails.get("profilePicture"));
                                        }

                                        if(userDetails.containsKey("deviceToken")) {
                                            user.put("deviceToken", userDetails.get("deviceToken"));
                                        }

                                        if(!Objects.equals(s.getKey(), myId)) {
                                            allUsers.add(user);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    usersReference.removeEventListener(this);
                }
            });
        }
    }

    void getMessagedUsers() {

        if(messagedUser.size()>0) {
            messagedUser.clear();
        }

        for(int i=0; i<allUsers.size(); i++) {
            if(allUsers.get(i).containsKey("messageId")) {
                messagedUser.add(allUsers.get(i));
            }
        }

        conversationAdapter = new ConversationAdapter(getContext(), messagedUser);
        conversationRecyclerView.setAdapter(conversationAdapter);
    }

    void searchUser() {

        String nameToSearch = String.valueOf(search.getText()).trim().toLowerCase();

        if(searchResult.size()>0) {
            searchResult.clear();
        }

        for(int i=0; i<allUsers.size(); i++) {

            String name = String.valueOf(allUsers.get(i).get("fullName")).toLowerCase();
            Set<Character> set1 = new HashSet<>();
            Set<Character> set2 = new HashSet<>();

            for(char c : name.toCharArray()) {
                set1.add(c);
            }

            for(char c : nameToSearch.toCharArray()) {
                set2.add(c);
            }

            Set<Character> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);

            Set<Character> union = new HashSet<>(set1);
            union.addAll(set2);

            double score = (double) intersection.size()/union.size();

            if((score>0.3 || name.contains(nameToSearch)) && !String.valueOf(allUsers.get(i).get("id")).equals(user.getUid())) {
                searchResult.add(allUsers.get(i));
            }
        }

        if(searchResult.size()==0 || nameToSearch.equals("")) {
            sortList();
            conversationAdapter = new ConversationAdapter(getContext(), messagedUser);
            conversationRecyclerView.setAdapter(conversationAdapter);
        }
        else {
            conversationAdapter = new ConversationAdapter(getContext(), searchResult);
            conversationRecyclerView.setAdapter(conversationAdapter);
        }
    }

    String getDate(long time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(Instant.ofEpochMilli(time));
        return formattedDate;
    }

    String getTime(long time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault());
        String formattedTime = formatter.format(Instant.ofEpochMilli(time));
        return formattedTime;
    }

    void sortList() {

        messagedUser.sort((mapA, mapB) -> {
            long valueA = mapA.containsKey("lastMessagedAt") ? stringToLongTime(String.valueOf(mapA.get("lastMessagedAt"))) : Long.MIN_VALUE;
            long valueB = mapB.containsKey("lastMessagedAt") ? stringToLongTime(String.valueOf(mapB.get("lastMessagedAt"))) : Long.MIN_VALUE;
            return Long.compare(valueB, valueA);
        });
    }

    long stringToLongTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a dd MMM yyyy");
        LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}