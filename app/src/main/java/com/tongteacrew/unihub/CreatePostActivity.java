package com.tongteacrew.unihub;

import static java.lang.Math.min;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreatePostActivity extends AppCompatActivity {

    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    RelativeLayout postLayout, attachmentLayout;
    ProgressBar progressBar;
    ImageButton addImage, btnBack;
    Button btnPost;
    EditText text;
    LinearLayout selectedMedia;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    RecyclerView filesRecyclerView;
    FilesAdapter filesAdapter;
    ArrayList<Uri> files = new ArrayList<>();
    String accountType, myId, postId, postText;
    long departmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // To fetch data from previous activity
        accountType = getIntent().getSerializableExtra("accountType") != null
                ? (String) getIntent().getSerializableExtra("accountType")
                : "student";

        departmentId = getIntent().getSerializableExtra("departmentId") != null
                ? (Long) getIntent().getSerializableExtra("departmentId")
                : 1L;

        myId = getIntent().getSerializableExtra("myId") != null
                ? (String) getIntent().getSerializableExtra("myId")
                : "";

        postId = (String) getIntent().getSerializableExtra("postId");
        postText = (String) getIntent().getSerializableExtra("text");

        postLayout = findViewById(R.id.relativeLayout4);
        progressBar = findViewById(R.id.progress_bar);
        text = findViewById(R.id.post_message);
        addImage = findViewById(R.id.btn_attach_file);
        btnBack = findViewById(R.id.btn_back);
        btnPost = findViewById(R.id.btn_post);
        selectedMedia = findViewById(R.id.selected_media);
        filesRecyclerView = findViewById(R.id.media_recycler_view);
        attachmentLayout = findViewById(R.id.relativeLayout5);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filesRecyclerView.setLayoutManager(layoutManager);
        filesAdapter = new FilesAdapter(CreatePostActivity.this, files, selectedMedia);
        filesRecyclerView.setAdapter(filesAdapter);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(20), uris -> {

            if(!uris.isEmpty()) {

                for(Uri uri : uris) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    files.add(uri);
                }

                filesAdapter.notifyDataSetChanged();
                filesRecyclerView.scrollToPosition(filesAdapter.getItemCount()-1);
            }
            else {
                System.out.println("No media selected");
            }

            if(files.size()>0) {
                selectedMedia.setVisibility(View.VISIBLE);
            }
            else {
                selectedMedia.setVisibility(View.GONE);
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                String msg = String.valueOf(text.getText());

                if(postId==null) {

                    String postId = getPostId();

                    updateDepartmentPosts(postId, new CompletionCallback() {
                        @Override
                        public void onCallback(Object data) {
                            if((Boolean) data) {
                                createPost(postId, msg);
                            }
                        }
                    });
                }
                else {
                    postLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    editPost(String.valueOf(text.getText()));
                }
            }
        });

        if(postText!=null) {
            text.setText(postText);
            attachmentLayout.setVisibility(View.GONE);
        }
    }

    void editPost(String txt) {

        DatabaseReference updatePostReference = rootReference.child("posts").child(String.valueOf(postId)).child("text");

        updatePostReference.setValue(txt).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                postLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                finish();
            }
        });
    }

    void updateDepartmentPosts(String postId, CompletionCallback callback) {

        DatabaseReference updatePostReference = rootReference.child("departmentPosts").child(String.valueOf(departmentId));
        updatePostReference.keepSynced(true);

        updatePostReference.child(postId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onCallback(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(true);
            }
        });
    }

    String getPostId() {
        DatabaseReference createPostReference = rootReference.child("departments").child(String.valueOf(departmentId));
        return createPostReference.push().getKey();
    }

    void createPost(String postId, String msg) {

        Map<String, Object> post = new HashMap<>();
        post.put("accountType", accountType);
        post.put("posterId", myId);
        post.put("text", msg);

        if(Objects.equals(accountType, "student")) {
            post.put("approval", false);
        }
        else {
            post.put("approval", true);
        }

        setTimeStamp(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {

                post.put("time", data);
                DatabaseReference createPostReference = rootReference.child("posts").child(postId);
                createPostReference.keepSynced(true);

                createPostReference.setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(files.size()==0) {
                            postLayout.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            finish();
                        }

                        for(int i=0; i<files.size(); i++) {

                            final int fileNumber = i+1;

                            compressImage(files.get(i), new CompletionCallback() {
                                @Override
                                public void onCallback(Object data) {
                                    if(data!=null) {
                                        uploadFile((Uri) data, postId, fileNumber);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    void compressImage(Uri uri, CompletionCallback callback) {

        try {

            InputStream inputStream = this.getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            if(inputStream!=null) {
                inputStream.close();
            }

            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();
            float aspectRatio = (float) originalWidth / originalHeight;

            int newWidth = min(1024, originalWidth);
            int newHeight = (int) (min(1024, originalWidth) / aspectRatio);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            File tempFile = File.createTempFile("compressed_", ".jpg", this.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();
            callback.onCallback(Uri.fromFile(tempFile));
        }
        catch(Exception e) {
            System.out.println(e);
            callback.onCallback(null);
        }
    }

    void uploadFile(Uri file, String postId, int fileId) {

        MediaManager.get().upload(file).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                System.out.println("Starting to upload image.");
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                System.out.println("Upload in progress...");
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                System.out.println("Upload completed!");
                String url = (String) resultData.get("secure_url");
                updateRealtimeDatabase(postId, url, fileId);
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                System.out.println("Unable to upload.");
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                System.out.println("Upload rescheduled.");
            }
        }).dispatch();
    }

    void updateRealtimeDatabase(String postId, String url, int fileId) {

        DatabaseReference mediaReference = rootReference.child("medias").child(postId).child("file"+fileId);

        mediaReference.setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(fileId==files.size()) {
                    postLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    finish();
                }
            }
        });
    }

    void setTimeStamp(CompletionCallback callback) {

        DatabaseReference timeReference = rootReference.child("timeStamps").child(myId);
        timeReference.keepSynced(true);

        timeReference.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                getTimeStamp(callback);
            }
        });
    }

    void getTimeStamp(CompletionCallback callback) {

        DatabaseReference timeReference = rootReference.child("timeStamps").child(myId);
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