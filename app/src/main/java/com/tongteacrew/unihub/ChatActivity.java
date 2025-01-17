package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    RelativeLayout onlineStatus, profilePicLayout;
    ImageView userProfilePicture;
    ImageButton btnBack, btnSend;
    TextView profileName;
    EditText message;
    CircularProgressIndicator progressIndicator;
    RecyclerView chatRecyclerView;
    ChatAdapter chatAdapter;
    ArrayList<Map<String, Object>> messages = new ArrayList<>();
    Map<String, Object> userData, myData;
    long serverTime;
    String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userData = (Map<String, Object>) getIntent().getSerializableExtra("userData");
        chatId = String.valueOf(userData.get("messageId"));

        onlineStatus = findViewById(R.id.online_status);
        profilePicLayout = findViewById(R.id.relativeLayout3);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        btnBack = findViewById(R.id.btn_back);
        btnSend = findViewById(R.id.btn_send);
        profileName = findViewById(R.id.profile_name);
        message = findViewById(R.id.message);
        userProfilePicture = findViewById(R.id.profile_picture);
        progressIndicator = findViewById(R.id.circular_progress_indicator);

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

        profilePicLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("id", String.valueOf(userData.get("id")));
                intent.putExtra("account_type", String.valueOf(userData.get("accountType")));
                ChatActivity.this.startActivity(intent);
            }
        });

        profileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("id", String.valueOf(userData.get("id")));
                intent.putExtra("account_type", String.valueOf(userData.get("accountType")));
                ChatActivity.this.startActivity(intent);
            }
        });

        profileName.setText(String.valueOf(userData.get("fullName")));

        if(userData.containsKey("profilePicture")) {
            setProfilePicture(String.valueOf(userData.get("profilePicture")));
        }

        if(userData.containsKey("messageId")) {
            getMessages();
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(message.getText()).trim().equals("")) {
                    Toast.makeText(ChatActivity.this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                }
                else {
                    checkIfInteracted(String.valueOf(message.getText()));
                    Map<String, Object> m = new HashMap<>();
                    m.put("date", getDate(System.currentTimeMillis()));
                    m.put("time", getTime(System.currentTimeMillis()));
                    m.put("text", String.valueOf(message.getText()));
                    m.put("sender", user.getUid());
                    m.put("isSent", false);
                    messages.add(m);
                    message.setText("");
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatAdapter.getItemCount()-1);
                }
            }
        });

        getMyData();
        setOnlineStatus();
        makeOnline();
    }

    void getMyData() {

        DatabaseReference myReference = rootReference.child("student").child(user.getUid());
        myReference.keepSynced(true);

        myReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                myReference.removeEventListener(this);

                if(snapshot.exists()) {
                    myData = (Map<String, Object>) snapshot.getValue();
                }
                else {

                    DatabaseReference myReference2 = rootReference.child("facultyMember").child(user.getUid());
                    myReference2.keepSynced(true);

                    myReference2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            myReference2.removeEventListener(this);
                            myData = (Map<String, Object>) snapshot.getValue();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            myReference2.removeEventListener(this);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                myReference.removeEventListener(this);
            }
        });
    }

    void getMessages() {

        DatabaseReference chatsReference = rootReference.child("chats").child(String.valueOf(userData.get("messageId")));
        chatsReference.keepSynced(true);

        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(messages.size()>0) {
                    messages.clear();
                }

                for(DataSnapshot s : snapshot.getChildren()) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("date", getDate(Long.parseLong(s.getKey())));
                    msg.put("time", getTime(Long.parseLong(s.getKey())));
                    Map<String, Object> m = (Map<String, Object>) s.getValue();
                    msg.put("text", m.get("text"));
                    msg.put("sender", m.get("sender"));
                    msg.put("isSent", true);
                    messages.add(msg);

                    if(messages.size()==snapshot.getChildrenCount()) {
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount()-1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    void makeOnline() {

        DatabaseReference onlineStatusReference = rootReference.child("onlineStatus").child(user.getUid());
        Map<String, Boolean> online = new HashMap<>();
        online.put("isOnline", true);
        onlineStatusReference.setValue(online);

        Map<String, Boolean> offline = new HashMap<>();
        offline.put("isOnline", false);
        onlineStatusReference.onDisconnect().setValue(offline);
    }

    void setOnlineStatus() {

        DatabaseReference onlineReference = rootReference.child("onlineStatus").child(String.valueOf(userData.get("id")));
        onlineReference.keepSynced(true);

        onlineReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    Map<String, Object> status = (Map<String, Object>) snapshot.getValue();

                    if((boolean) status.get("isOnline")) {
                        onlineStatus.setVisibility(View.VISIBLE);
                    }
                    else {
                        onlineStatus.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    void setProfilePicture(String profilePicture) {

        progressIndicator.setVisibility(View.VISIBLE);

        Glide.with(ChatActivity.this)
                .load(profilePicture)
                .error(R.drawable.icon_default_profile)
                .placeholder(R.drawable.icon_default_profile)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        progressIndicator.setVisibility(View.GONE);
                        Glide.with(ChatActivity.this).load(resource).circleCrop().into(userProfilePicture);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        progressIndicator.setVisibility(View.GONE);
                        userProfilePicture.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        progressIndicator.setVisibility(View.GONE);
                    }
                });
    }

    void checkIfInteracted(String msg) {

        Query interactionReference = rootReference.child("interactions").child(user.getUid()).orderByKey().equalTo(userData.get("id").toString());
        interactionReference.keepSynced(true);

        interactionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                interactionReference.removeEventListener(this);

                if(snapshot.exists()) { // Already interacted
                    setTimeStamp(msg);
                }
                else { // Not interacted yet
                    createInteraction(msg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                interactionReference.removeEventListener(this);
            }
        });
    }

    void createInteraction(String msg) {

        String receiverId = String.valueOf(userData.get("id"));
        chatId = rootReference.child("chats").push().getKey();
        DatabaseReference interactionReference = rootReference.child("interactions").child(user.getUid()).child(receiverId);
        interactionReference.keepSynced(true);

        interactionReference.setValue(chatId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                DatabaseReference interactionReference2 = rootReference.child("interactions").child(receiverId).child(user.getUid());
                interactionReference2.keepSynced(true);

                interactionReference2.setValue(chatId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        userData.put("messageId", chatId);
                        getMessages();
                        setTimeStamp(msg);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Failed to send message!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void setTimeStamp(String msg) {

        DatabaseReference timeReference = rootReference.child("timeStamps").child(user.getUid());
        timeReference.keepSynced(true);

        timeReference.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                getTimeStamp(msg);
            }
        });
    }

    void getTimeStamp(String msg) {

        DatabaseReference timeReference = rootReference.child("timeStamps").child(user.getUid());
        timeReference.keepSynced(true);

        timeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()) {
                    serverTime = (long) task.getResult().getValue();
                    sendMessage(msg);
                }
            }
        });
    }

    void sendMessage(String msg) {

        Map<String, Object> msgDetails = new HashMap<>();
        msgDetails.put("sender", user.getUid());
        msgDetails.put("text", msg);

        DatabaseReference textReference = rootReference.child("chats").child(String.valueOf(userData.get("messageId"))).child(String.valueOf(serverTime));
        textReference.keepSynced(true);

        textReference.setValue(msgDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sendNotification(msg);
            }
        });
    }

    void sendNotification(String msg) {

        JSONObject messageObject = new JSONObject();

        try {
            JSONObject dataObj = new JSONObject();
            dataObj.put("fullName", myData.get("fullName"));
            dataObj.put("messageId", chatId);
            dataObj.put("id", user.getUid());
            dataObj.put("title", myData.get("fullName"));
            dataObj.put("body", msg);

            if(myData.containsKey("profilePicture")) {
                dataObj.put("profilePicture", myData.get("profilePicture"));
            }

            if(myData.containsKey("deviceToken")) {
                dataObj.put("deviceToken", myData.get("deviceToken"));
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", dataObj);
            jsonObject.put("token", userData.get("deviceToken"));

            messageObject.put("message", jsonObject);
            System.out.println(messageObject);
        }
        catch(Exception e) {
            System.out.println(e);
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(String.valueOf(messageObject), MediaType.get("application/json; charset=utf-8"));

        // Build the request
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/unihub-98c4e/messages:send")
                .post(body)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    System.out.println("Notification sent successfully: "+response.body().string());
                }
                else {
                    System.out.println("Error: "+response.body().string());
                }
            }
        });
    }

    String getAccessToken() {

        try {

            String jsonToString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"unihub-98c4e\",\n" +
                    "  \"private_key_id\": \"ccb25bde35c0e89d3d233cc74b69c4e2a499c7fe\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCzsKGIsXEyTP/j\\nF6Vyn1qdvUNwFO7CXWle67YYEXhvMYHHb4EVoU/iaKHgMbiGGdCoEp8zEViEfafJ\\n3/N1npQ/arRTzPf/Y2dP2bgSNiTIJM7/QdwYugMf8LvLHHD9SgKJ2obtYt4F6TKk\\n/dPnmm/8j41h/1RH6APIqn8FqHhJ/3OZvy3YAiTjzL1fRmz0VqfMdhuSWvbA4RhO\\n4WY+bsSFwxGk+Wkv8oI/SQn4/bLdtZi2SbTfSX5H+tGged09mzWUrPcFsOcyAP38\\nv3dBlU6gdLM8hR2wGfovi9NNZEyoL7BFNNMvpLDTcIWcEmOjPMzePlCVmA5HrtYW\\nflB/fU3ZAgMBAAECggEABmIH2usysi66CD8WyXOPrHrEon6b3Juk2pJU7ZXxSUHg\\n8kyrsTEBvXEqDuS1QU45cz72GMJM+qfcBffGli8D5RzNOwzU4mWNjfCV8U+rDtD/\\n6WFViCtTYRcUFKr1+IlgfbUdheE1MdCO0/2QQXHi+H3A7/I59DPxrncf+/t4CmjR\\nVNbkTyeGpOSmT7+V6VORsucZYVpCavcK817ieoRYa65odapPYvsG/fI1INBKIdby\\nWYCJMwNELg9gxH0G+4icA4WHaH75ks2jLcYgPjyZCSDwsngqowgz2loWCt9HgQNQ\\n9JXkvD/uEmDkxyhlO01q2YMYCNTPZ2DEsjkp/0BBgQKBgQDlWpvn1+MorsKWtYQA\\nV1wmM/Z+1Hyez6nJKtCKJQZqiLWiU0mFULN9zj46OqIGA18pCAQDnO5Sla1Q0fJS\\naUui3LBF63EBHjgWrPQwe9EOhzIz86bRtJ6gbzY4SijYfoTj3jZ4yymUHv32HIrt\\n03E1JK4XufZi//4E5vxJAxJd9QKBgQDIkO174OivL2YrdTULvj7Kx+p3vlkXJdbE\\nwjbsqO6gXRJCAVPX2pUDdG3h4rX2hAGFn+EbUNIFuPMFOFbGlsZiwl/HUBc0LWIy\\nl8m8LQq1WiRiwizpGfXBRyiyOE9tF8M23B+jLIuSVY0LiKMg85KdHvU4bSIVRCch\\nypMo3rL91QKBgQDSpuslAOtZtVFyHJt1uMchK37NtJoVPwRhiNpq12DSPmgdBEQ1\\nlw6UkPYkgy/HOBeR1xPgwaU+4syBu6LGQIHAvtOEFKAA9+FqKkZJtZ8oqdHZV4Mz\\nfqJnFl4FS0/CsEmcBL+hKHAy5Fg7ULHlh9ulhOAFWL7M5PRJSmITKSgYdQKBgQCQ\\na8OF+zqxwujIDDrpPNGMRQ4xsVAHmgifX9Ya7b3+nWYjPz93Y/7/INxq1kv+uak6\\n5hg7CiRhWH8t2BasIy+xN5OuOp6qxK88DQ6HwMtAMSuYLYgXRckvpqTISEHxJTY9\\nj538anwKIC5TCs2kUZ/WIc+kFPmA5LVk4LC8sjejDQKBgQDEsO7T02J3KHeJk7sg\\nNtRgUZxQR9h6qksQ9/Y88JNmd95TIjD5QT/9mM8tsLsjrHHxl50EYQ+4DVpWHKDi\\n2XkM5SYBa4KAH8S+CZ6LpJir4M4VRYv/gA54CztbdDnDWvS6KQtOk5ZY7o1eQiU0\\nf6OF0I4thLt2RSt7/uMbex0WYA==\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-wizdi@unihub-98c4e.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"111666378841118393752\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-wizdi%40unihub-98c4e.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";

            InputStream stream = new ByteArrayInputStream(jsonToString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();

        }
        catch(Exception e) {
            System.out.println(e);
            return null;
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
}