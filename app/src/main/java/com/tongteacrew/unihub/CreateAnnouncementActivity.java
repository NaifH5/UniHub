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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import kotlin.Pair;

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