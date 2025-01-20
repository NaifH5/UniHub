package com.tongteacrew.unihub;

import static java.lang.Math.min;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DepartmentInformationActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();
    RelativeLayout layoutDone, layoutEdit, editableItems;
    TextView departmentName;
    ImageButton btnBack, btnDone, btnEdit;;
    ImageView departmentImage;
    RecyclerView departmentMemberRecyclerView;
    DepartmentMembersAdapter departmentMemberAdapter;
    ArrayList<Map<String, Object>> members = new ArrayList<>();
    String accountType="student";
    long departmentId=1;
    Uri departmentPicURI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_information);

        departmentId = (long) getIntent().getSerializableExtra("departmentId");

        btnBack = findViewById(R.id.btn_back);
        departmentMemberRecyclerView = findViewById(R.id.users_recycler_view);
        departmentName = findViewById(R.id.department_name);
        departmentImage = findViewById(R.id.department_image);
        btnDone = findViewById(R.id.btn_done);
        btnEdit = findViewById(R.id.btn_edit);
        editableItems = findViewById(R.id.editable_items);
        layoutDone = findViewById(R.id.layout_done);
        layoutEdit = findViewById(R.id.layout_edit);

        ActivityResultLauncher<PickVisualMediaRequest> pickImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {

            if(uri!=null) {

                departmentPicURI = uri;
                layoutDone.setVisibility(View.VISIBLE);

                Glide.with(DepartmentInformationActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.icon_default_profile)
                        .error(R.drawable.icon_default_profile)
                        .into(departmentImage);
            }
        });

        getAccountType(new CompletionCallback() {
            @Override
            public void onCallback(Object data) {
                if(data!=null) {

                    accountType = (String) data;

                    if(accountType.equals("student")) {
                        layoutDone.setVisibility(View.GONE);
                        layoutEdit.setVisibility(View.GONE);
                    }
                }
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compressImage(new CompletionCallback() {
                    @Override
                    public void onCallback(Object data) {
                        if(data!=null) {
                            departmentPicURI = (Uri) data;
                            uploadImage();
                        }
                    }
                });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        departmentMemberRecyclerView.setLayoutManager(new LinearLayoutManager(DepartmentInformationActivity.this));

        setDepartmentName();
        getDepartmentImage();
        getGroupMembers(0);
    }

    void getAccountType(CompletionCallback callback) {

        DatabaseReference accountTypeReference = rootReference.child("student").child(user.getUid());
        accountTypeReference.keepSynced(true);

        accountTypeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful() && task.getResult().exists()) {
                    callback.onCallback("student");
                }
                else if(task.isSuccessful()) {
                    callback.onCallback("facultyMember");
                }
                else {
                    callback.onCallback(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(false);
            }
        });
    }

    void setDepartmentName() {

        String[] departments = {"Architecture", "Bangla", "Business Administration", "Civil Engineering",
                "Computer Science & Engineering", "Electrical and Electronics Engineering", "English",
                "Islamic Studies", "Law", "Public Health", "Tourism & Hospitality Management"};

        String name = "Department of "+departments[(int) departmentId];
        departmentName.setText(name);
    }

    void getDepartmentImage() {

        DatabaseReference imageReference = rootReference.child("departmentImage").child(String.valueOf(departmentId)).child("image");
        imageReference.keepSynced(true);

        imageReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful() && task.getResult().getValue()!=null) {
                    setImage(String.valueOf(task.getResult().getValue()));
                }
            }
        });
    }

    void setImage(String url) {

        Glide.with(DepartmentInformationActivity.this)
                .load(url)
                .error(R.drawable.illustration_1)
                .placeholder(R.drawable.illustration_1)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Glide.with(DepartmentInformationActivity.this).load(resource).into(departmentImage);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    void getGroupMembers(int index) {

        String[] accountType = {"facultyMember", "student"};
        DatabaseReference memberReference = rootReference.child(accountType[index]);
        memberReference.keepSynced(true);

        memberReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                memberReference.removeEventListener(this);

                if(snapshot.exists()) {

                    int i=0;

                    for(DataSnapshot s : snapshot.getChildren()) {

                        i++;
                        Map<String, Object> user = (Map<String, Object>) s.getValue();

                        if(String.valueOf(user.get("departmentId")).equals(String.valueOf(departmentId))) {

                            Map<String, Object> member = new HashMap<>();
                            member.put("accountType", accountType[index]);
                            member.put("id", s.getKey());
                            member.put("fullName", String.valueOf(user.get("fullName")));

                            if(user.containsKey("profilePicture")) {
                                member.put("profilePicture", String.valueOf(user.get("profilePicture")));
                            }

                            members.add(member);
                        }

                        if(i==snapshot.getChildrenCount() && index+1<2) {
                            getGroupMembers(index+1);
                        }
                        else if(i==snapshot.getChildrenCount() && index+1>=2) {
                            departmentMemberAdapter = new DepartmentMembersAdapter(DepartmentInformationActivity.this, members);
                            departmentMemberRecyclerView.setAdapter(departmentMemberAdapter);
                        }
                    }
                }
                else if(index+1<2) {
                    getGroupMembers(index+1);
                }
                else if(index+1>=2) {
                    departmentMemberAdapter = new DepartmentMembersAdapter(DepartmentInformationActivity.this, members);
                    departmentMemberRecyclerView.setAdapter(departmentMemberAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                memberReference.removeEventListener(this);
            }
        });
    }

    void compressImage(CompletionCallback callback) {

        try {

            InputStream inputStream = this.getContentResolver().openInputStream(departmentPicURI);
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

    void uploadImage() {

        MediaManager.get().upload(departmentPicURI).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Toast.makeText(DepartmentInformationActivity.this, "Starting to upload image.", Toast.LENGTH_SHORT).show();
                editableItems.setVisibility(View.GONE);
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                System.out.println("Upload in progress...");
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String url = (String) resultData.get("secure_url");
                updateRealtimeDatabase(url);
                editableItems.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                editableItems.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                editableItems.setVisibility(View.VISIBLE);
            }
        }).dispatch();
    }

    void updateRealtimeDatabase(String url) {

        DatabaseReference imageReference = rootReference.child("departmentImage").child(String.valueOf(departmentId)).child("image");

        imageReference.setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                editableItems.setVisibility(View.VISIBLE);
                finish();
            }
        });
    }
}