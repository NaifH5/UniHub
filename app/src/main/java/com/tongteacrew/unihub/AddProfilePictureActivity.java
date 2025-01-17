package com.tongteacrew.unihub;

import static java.lang.Math.min;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddProfilePictureActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    LinearLayout layoutComplete;
    ProgressBar progressBar;
    Button btnSkip, btnFinish;
    ImageButton selectImage;
    ImageView bgImage, profilePicture;
    String accountType;
    Uri profilePictureURI=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile_picture);

        // To make light status bar background
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // To fetch data from previous activity
        accountType = (String) getIntent().getSerializableExtra("account_type");

        layoutComplete = findViewById(R.id.layout_complete);
        progressBar = findViewById(R.id.progress_bar);
        btnSkip = findViewById(R.id.btn_skip);
        btnFinish = findViewById(R.id.btn_finish);
        bgImage = findViewById(R.id.bg_image);
        profilePicture = findViewById(R.id.department_image);
        selectImage = findViewById(R.id.btn_select);

        if(Objects.equals(accountType, "student")) {
            bgImage.setImageResource(R.drawable.illustration_1);
        }
        else {
            bgImage.setImageResource(R.drawable.illustration_2);
        }

        // To pick and get URI of the image
        ActivityResultLauncher<PickVisualMediaRequest> pickImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {

            if(uri!=null) {

                profilePictureURI = uri;

                Glide.with(AddProfilePictureActivity.this)
                        .load(uri)
                        .circleCrop()
                        .placeholder(R.drawable.icon_default_profile)
                        .error(R.drawable.icon_default_profile)
                        .into(profilePicture);
            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickImage.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddProfilePictureActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("account_type", accountType);
                AddProfilePictureActivity.this.startActivity(intent);
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(profilePictureURI==null) {
                    Toast.makeText(AddProfilePictureActivity.this, "Pick a picture first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                compressImage(new FirebaseCallback() {
                    @Override
                    public void onCallback(Object data) {
                        if(data!=null) {
                            profilePictureURI = (Uri) data;
                            setProfilePicture();
                        }
                    }
                });
            }
        });
    }

    void compressImage(FirebaseCallback callback) {

        try {

            InputStream inputStream = this.getContentResolver().openInputStream(profilePictureURI);
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

    void setProfilePicture() {

        layoutComplete.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        MediaManager.get().upload(profilePictureURI).callback(new UploadCallback() {
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
                updateRealtimeDatabase(url);
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                System.out.println("Unable to upload.");
                layoutComplete.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                System.out.println("Upload rescheduled.");
                layoutComplete.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }).dispatch();
    }

    void updateRealtimeDatabase(String url) {

        String account;

        if(Objects.equals(accountType, "student")) {
            account = "student";
        }
        else {
            account = "facultyMember";
        }

        rootReference.child(account).child(user.getUid()).child("profilePicture").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                layoutComplete.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                Intent intent = new Intent(AddProfilePictureActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("account_type", accountType);
                AddProfilePictureActivity.this.startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(AddProfilePictureActivity.this, "Failed to set profile picture!", Toast.LENGTH_SHORT).show();
                layoutComplete.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}