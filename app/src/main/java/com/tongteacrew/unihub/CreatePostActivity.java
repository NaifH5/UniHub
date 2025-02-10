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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreatePostActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
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
                    createPost(postId, msg);
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
                Map<String, Object> updates = new HashMap<>();
                updates.put("departmentPosts/"+departmentId+"/"+postId, true);
                updates.put("posts/"+postId, post);

                rootReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()) {

                            if(files.size()==0) {
                                postLayout.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                finish();
                            }
                            else {
                                for(int i = 0; i < files.size(); i++) {

                                    final int fileNumber = i + 1;

                                    compressImage(files.get(i), new CompletionCallback() {
                                        @Override
                                        public void onCallback(Object data) {
                                            if(data != null) {
                                                uploadFile((Uri) data, postId, fileNumber);
                                            }
                                        }
                                    });
                                }
                            }

                            getMyData();
                        }
                    }
                });
            }
        });
    }

    void getMyData() {

        DatabaseReference userReference = rootReference.child(accountType).child(user.getUid());
        userReference.keepSynced(true);

        userReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()) {
                    Map<String, Object> myData = (Map<String, Object>) task.getResult().getValue();
                    getDepartmentMembers("student", myData);
                    getDepartmentMembers("facultyMember", myData);
                }
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

    void getDepartmentMembers(String accountType, Map<String, Object> myData) {

        DatabaseReference userReference = rootReference.child(accountType);
        userReference.keepSynced(true);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userReference.removeEventListener(this);
                if(snapshot.exists()) {
                    for(DataSnapshot s : snapshot.getChildren()) {
                        Map<String, Object> users = (Map<String, Object>) s.getValue();
                        if(String.valueOf(users.get("departmentId")).equals(String.valueOf(departmentId))) {
                            if(!String.valueOf(s.getKey()).equals(user.getUid())) {
                                sendNotification(myData, users);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userReference.removeEventListener(this);
            }
        });
    }

    public void sendNotification(Map<String, Object> myData, Map<String, Object> userData) {

        JSONObject messageObject = new JSONObject();

        try {
            JSONObject dataObj = new JSONObject();
            dataObj.put("fullName", myData.get("fullName"));
            dataObj.put("messageId", "");
            dataObj.put("id", user.getUid());
            dataObj.put("title", myData.get("fullName"));
            dataObj.put("body", "New department post...");

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