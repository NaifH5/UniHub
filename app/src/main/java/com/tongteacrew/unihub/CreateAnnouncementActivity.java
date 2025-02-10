package com.tongteacrew.unihub;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import kotlin.Pair;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateAnnouncementActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    ProgressBar progressBar;
    TextView activityTitle;
    EditText announcementMessage, schedule;
    Button btnAnnounce;
    ImageButton btnBack, addFile;
    LinearLayout selectedMedia;
    RelativeLayout announceLayout;
    RecyclerView filesRecyclerView;
    FilesAdapter filesAdapter;
    ArrayList<Uri> files = new ArrayList<>();
    String announcementType;
    String courseGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        announcementType = (String) getIntent().getSerializableExtra("announcement_type");
        courseGroupId = (String) getIntent().getSerializableExtra("courseGroupId");

        activityTitle = findViewById(R.id.activity_title);
        announcementMessage = findViewById(R.id.announcement_message);
        schedule = findViewById(R.id.schedule);
        btnAnnounce = findViewById(R.id.btn_announce);
        btnBack = findViewById(R.id.btn_back);
        addFile = findViewById(R.id.btn_attach_file);
        selectedMedia = findViewById(R.id.selected_media);
        filesRecyclerView = findViewById(R.id.media_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        announceLayout = findViewById(R.id.relativeLayout4);

        if(Objects.equals(announcementType, "announcement")) {
            activityTitle.setText("Announcement");
            announcementMessage.setHint("Write your announcement...");
            btnAnnounce.setText("Announce");
        }
        else {
            activityTitle.setText("Assignment");
            announcementMessage.setHint("Assign something...");
            btnAnnounce.setText("Assign");
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filesRecyclerView.setLayoutManager(layoutManager);
        filesAdapter = new FilesAdapter(CreateAnnouncementActivity.this, files, selectedMedia);
        filesRecyclerView.setAdapter(filesAdapter);

        ActivityResultLauncher<String> getContent = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> uri) {
                        for(int i=0; i<uri.size(); i++) {
                            if(uri.get(i)!=null) {
                                files.add(uri.get(i));
                                selectedMedia.setVisibility(View.VISIBLE);
                                filesAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContent.launch("application/pdf");
                filesRecyclerView.scrollToPosition(filesAdapter.getItemCount()-1);
            }
        });

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment(schedule);
                datePickerFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        btnAnnounce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                announce(String.valueOf(announcementMessage.getText()));
            }
        });
    }

    void announce(String text) {

        announceLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        setTimeStamp(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {

                long time = (long) data;
                String postId = rootReference.child("courseAnnouncements").child(courseGroupId).push().getKey();
                Map<String, String> urlList = new HashMap<>();
                Map<String, Object> updates = new HashMap<>();

                Map<String, Object> postDetails = new HashMap<>();
                postDetails.put("posterId", user.getUid());
                postDetails.put("time", time);
                postDetails.put("text", text);
                postDetails.put("schedule", convertDateToTime(String.valueOf(schedule.getText())));
                postDetails.put("announcementType", announcementType);

                if(files.size()>0) {

                    for(int i=0; i<files.size(); i++) {

                        uploadFile(i+1, files.get(i), new CompletionCallback() {
                            @Override
                            public void onCallback(Object data) {

                                if(data!=null) {

                                    Map<String, String> url = (Map<String, String>) data;
                                    urlList.put(url.entrySet().iterator().next().getKey(), url.entrySet().iterator().next().getValue());

                                    if(urlList.size()==files.size()) {

                                        updates.put("courseAnnouncements/"+courseGroupId+"/"+postId, postDetails);
                                        updates.put("courseMedias/"+postId, urlList);

                                        rootReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()) {
                                                    Toast.makeText(CreateAnnouncementActivity.this, "Announced!", Toast.LENGTH_SHORT).show();
                                                    getMyData("student");
                                                    getMyData("facultyMember");
                                                }
                                                else {
                                                    Toast.makeText(CreateAnnouncementActivity.this, "Failed to announce!", Toast.LENGTH_SHORT).show();
                                                }

                                                announceLayout.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                                finish();
                                            }
                                        });
                                    }
                                }
                                else {
                                    Toast.makeText(CreateAnnouncementActivity.this, "Announced!", Toast.LENGTH_SHORT).show();
                                    announceLayout.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                }
                            }
                        });
                    }
                }
                else {

                    updates.put("courseAnnouncements/"+courseGroupId+"/"+postId, postDetails);

                    rootReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {
                                Toast.makeText(CreateAnnouncementActivity.this, "Announced!", Toast.LENGTH_SHORT).show();
                                getMyData("student");
                                getMyData("facultyMember");
                            }
                            else {
                                Toast.makeText(CreateAnnouncementActivity.this, "Failed to announce!", Toast.LENGTH_SHORT).show();
                            }

                            announceLayout.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            finish();
                        }
                    });
                }
            }
        });
    }

    void getMyData(String accountType) {

        DatabaseReference userReference = rootReference.child(accountType).child(user.getUid());
        userReference.keepSynced(true);

        userReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()) {
                    Map<String, Object> myData = (Map<String, Object>) task.getResult().getValue();
                    getAllGroupMembersId("student", myData);
                    getAllGroupMembersId("facultyMember", myData);
                }
            }
        });
    }

    void getAllGroupMembersId(String accountType, Map<String, Object> myData) {

        DatabaseReference userReference = rootReference.child("courseGroupMembers").child(courseGroupId).child(accountType);
        userReference.keepSynced(true);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userReference.removeEventListener(this);
                if(snapshot.exists()) {
                    for(DataSnapshot s : snapshot.getChildren()) {
                        getGroupMemberDetails(s.getKey(), accountType, myData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userReference.removeEventListener(this);
            }
        });
    }

    void getGroupMemberDetails(String id, String accountType, Map<String, Object> myData) {

        if(!id.equals(user.getUid())) {
            DatabaseReference userReference = rootReference.child(accountType).child(id);
            userReference.keepSynced(true);

            userReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful() && task.getResult().exists()) {
                        Map<String, Object> userData = (Map<String, Object>) task.getResult().getValue();
                        sendNotification(myData, userData);
                    }
                }
            });
        }
    }

    void sendNotification(Map<String, Object> myData, Map<String, Object> userData) {

        JSONObject messageObject = new JSONObject();

        try {
            JSONObject dataObj = new JSONObject();
            dataObj.put("fullName", myData.get("fullName"));
            dataObj.put("messageId", "");
            dataObj.put("id", user.getUid());
            dataObj.put("title", myData.get("fullName"));
            dataObj.put("body", "New "+announcementType+" from "+courseGroupId.split("_")[3]);

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

    public String getAccessToken() {

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

    void uploadFile(int index, Uri file, CompletionCallback callback) {

        MediaManager.get().upload(file)
                .option("resource_type", "raw")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        System.out.println("Starting to upload file...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        if(totalBytes>0) {
                            System.out.println((bytes*100)/totalBytes+"% uploaded...");
                        }
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        System.out.println("Upload completed!");
                        String url = (String) resultData.get("secure_url");

                        if(url!=null) {
                            Map<String, String> mp = new HashMap<>();
                            mp.put("file" + index, url);
                            callback.onCallback(mp);
                        }
                        else {
                            System.out.println("Error: No URL returned from Cloudinary.");
                            callback.onCallback(null);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        System.out.println("Upload failed: " + error.getDescription());
                        callback.onCallback(null);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        System.out.println("Upload rescheduled: " + error.getDescription());
                        callback.onCallback(null);
                    }
                })
                .dispatch();
    }


    void setTimeStamp(CompletionCallback callback) {

        DatabaseReference timeReference = rootReference.child("timeStamps").child(user.getUid());

        timeReference.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                getTimeStamp(callback);
            }
        });
    }

    void getTimeStamp(CompletionCallback callback) {

        DatabaseReference timeReference = rootReference.child("timeStamps").child(user.getUid());

        timeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()) {
                    callback.onCallback((long) task.getResult().getValue());
                }
            }
        });
    }

    long convertDateToTime(String time) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        long epochTime = 0;

        try {
            Date date = sdf.parse(time);
            epochTime = date.getTime();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return epochTime;
    }
}