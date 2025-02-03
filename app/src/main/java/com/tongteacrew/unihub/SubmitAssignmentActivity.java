package com.tongteacrew.unihub;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmitAssignmentActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    Button btnSubmit;
    ImageButton btnBack, addFile;
    RecyclerView filesRecyclerView;
    FilesAdapter filesAdapter;
    LinearLayout selectedMedia;
    RelativeLayout submissionLayout;
    EditText message;
    ProgressBar progressBar;
    ArrayList<Uri> files = new ArrayList<>();
    String courseGroupId, announcementId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        courseGroupId = (String) getIntent().getSerializableExtra("courseGroupId");
        announcementId = (String) getIntent().getSerializableExtra("announcementId");

        btnBack = findViewById(R.id.btn_back);
        addFile = findViewById(R.id.btn_attach_file);
        filesRecyclerView = findViewById(R.id.media_recycler_view);
        selectedMedia = findViewById(R.id.selected_media);
        btnSubmit = findViewById(R.id.btn_submit);
        message = findViewById(R.id.message);
        progressBar = findViewById(R.id.progress_bar);
        submissionLayout = findViewById(R.id.relativeLayout4);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filesRecyclerView.setLayoutManager(layoutManager);
        filesAdapter = new FilesAdapter(SubmitAssignmentActivity.this, files, selectedMedia);
        filesRecyclerView.setAdapter(filesAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ActivityResultLauncher<String> getContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            files.clear(); // Clear previous selections
                            files.add(uri);
                            selectedMedia.setVisibility(View.VISIBLE);
                            filesAdapter.notifyDataSetChanged();
                        }
                    }
                });

        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContent.launch("application/pdf");
                filesRecyclerView.scrollToPosition(filesAdapter.getItemCount()-1);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitAssignment(String.valueOf(message.getText()));
            }
        });
    }

    void submitAssignment(String text) {

        progressBar.setVisibility(View.VISIBLE);
        submissionLayout.setVisibility(View.GONE);

        setTimeStamp(new CompletionCallback() {
            @Override
            public void onCallback(Object timeData) {

                String assignmentPath = "assignments/"+courseGroupId+"/"+announcementId+"/"+user.getUid();
                String mediaPath = "assignmentMedia/"+courseGroupId+"/"+announcementId+"/"+user.getUid();

                uploadFile(files.get(0), new CompletionCallback() {
                    @Override
                    public void onCallback(Object data) {

                        if(data!=null) {
                            Map<String, String> mediaData = (Map<String, String>) data;
                            Map<String, Object> updates = new HashMap<>();
                            updates.put(assignmentPath+"/time", (long) timeData);
                            updates.put(assignmentPath+"/text", text);
                            updates.put(mediaPath, mediaData);

                            rootReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(SubmitAssignmentActivity.this, "Assignment submitted!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(SubmitAssignmentActivity.this, "Failed to submit assignment!", Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                    submissionLayout.setVisibility(View.VISIBLE);
                                    finish();
                                }
                            });
                        }
                        else {
                            progressBar.setVisibility(View.GONE);
                            submissionLayout.setVisibility(View.VISIBLE);
                            finish();
                        }
                    }
                });

            }
        });
    }

    void uploadFile(Uri file, CompletionCallback callback) {

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
                            mp.put("file", url);
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
        timeReference.keepSynced(true);

        timeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful()) {
                    callback.onCallback((long) task.getResult().getValue());
                }
            }
        });
    }
}