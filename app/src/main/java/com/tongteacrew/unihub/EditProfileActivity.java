package com.tongteacrew.unihub;

import static java.lang.Math.min;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    RelativeLayout designationSection, acronymSection, batchSection, sectionSection, idSection;
    CircularProgressIndicator progressIndicator;
    ProgressBar progressBar;
    Spinner depSpinner, batchSpinner, sectionSpinner, clubsSpinner, clubDesignationsSpinner;
    RecyclerView clubRecyclerView;
    ClubAdapter clubAdapter;
    Button btnAddToMyClubs, btnUpdateProfile;
    ImageButton btnBack, selectImage;
    ImageView profilePicture;
    TextView designationText;
    EditText fullName, id, acronym, note;
    String accountType, department, batch, section, club, clubDesignation, profileId, sectionId, phone, email, profilePic;
    ArrayList<String[]> selectedClubs = new ArrayList<>();
    Uri profilePictureURI;
    Map<String, Object> profileInfo, designationMap;
    Map<String, Map<String, Object>> clubDetails = new HashMap<>();
    long departmentID, batchId;

    String[] departments = {"Architecture", "Bangla", "Business Administration", "Civil Engineering",
            "Computer Science & Engineering", "Electrical and Electronics Engineering", "English",
            "Islamic Studies", "Law", "Public Health", "Tourism & Hospitality Management"};
    String[] batches = {"Batch 56", "Batch 57", "Batch 58", "Batch 59", "Batch 60", "Batch 61",
            "Batch 62", "Batch 63", "Batch 64"};
    int[] batchIds = {56, 57, 58, 59, 60, 61, 62, 63, 64};
    String[] sections = {"Section A", "Section B", "Section C", "Section D", "Section E", "Section F",
            "Section G", "Section H", "Section I", "Section J"};
    String[] sectionIds = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    String[] designations = {"Adjunct Lecturer", "Assistant Proctor", "Assistant Professor",
            "Associate Professor", "Head", "Lecturer"};
    boolean[] selectedDesignations = new boolean[designations.length];
    String[] clubs = {"Computer Club", "Orpheus", "Banned Community"};
    String[] clubDesignations = {"Member", "Executive Member"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // To fetch data from previous activity
        profileId = (String) getIntent().getSerializableExtra("id");
        accountType = (String) getIntent().getSerializableExtra("account_type");
        profileInfo = (Map<String, Object>) getIntent().getSerializableExtra("profile_info");
        selectedClubs = (ArrayList<String[]>) getIntent().getSerializableExtra("club_details");
        phone = (String) profileInfo.get("phoneNumber");
        email = (String) profileInfo.get("email");

        if(profileInfo.containsKey("profilePicture")) {
            profilePic = (String) profileInfo.get("profilePicture");
        }

        progressIndicator = findViewById(R.id.circular_progress_indicator);
        progressBar = findViewById(R.id.progress_bar);
        batchSection = findViewById(R.id.batch_section);
        sectionSection = findViewById(R.id.section_section);
        idSection = findViewById(R.id.id_section);
        designationSection = findViewById(R.id.designation_section);
        acronymSection = findViewById(R.id.acronym_section);
        btnBack = findViewById(R.id.btn_back);
        profilePicture = findViewById(R.id.profile_picture);
        selectImage = findViewById(R.id.btn_select);
        depSpinner = findViewById(R.id.department_spinner);
        batchSpinner = findViewById(R.id.batch_spinner);
        sectionSpinner = findViewById(R.id.section_spinner);
        clubsSpinner = findViewById(R.id.club_name_spinner);
        clubDesignationsSpinner = findViewById(R.id.club_designation_spinner);
        fullName = findViewById(R.id.full_name_in_register);
        id = findViewById(R.id.id_in_register);
        acronym = findViewById(R.id.acronym_in_register);
        designationText = findViewById(R.id.designation_text);
        note = findViewById(R.id.note);
        clubRecyclerView = findViewById(R.id.post_recycler_view);
        btnAddToMyClubs = findViewById(R.id.btn_add_to_my_clubs);
        btnUpdateProfile = findViewById(R.id.btn_update_profile);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // To pick and get URI of the image
        ActivityResultLauncher<PickVisualMediaRequest> pickImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {

            if(uri!=null) {

                profilePictureURI = uri;

                Glide.with(EditProfileActivity.this)
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

        ArrayAdapter depArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, departments);
        depArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        depSpinner.setAdapter(depArrayAdapter);

        depSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                department = departments[position];
                departmentID = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter batchArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, batches);
        batchArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchSpinner.setAdapter(batchArrayAdapter);

        batchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                batch = batches[position];
                batchId = batchIds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter sectionArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sections);
        sectionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(sectionArrayAdapter);

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                section = sections[position];
                sectionId = sectionIds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        designationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDesignationChoice();
            }
        });

        ArrayAdapter clubsArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, clubs);
        clubsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clubsSpinner.setAdapter(clubsArrayAdapter);

        clubsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                club = clubs[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        setInfo();

        ArrayAdapter clubDesignationsArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, clubDesignations);
        clubDesignationsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clubDesignationsSpinner.setAdapter(clubDesignationsArrayAdapter);

        clubDesignationsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clubDesignation = clubDesignations[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        clubRecyclerView.setLayoutManager(new LinearLayoutManager(EditProfileActivity.this));
        clubRecyclerView.setHasFixedSize(true);
        clubAdapter = new ClubAdapter(EditProfileActivity.this, selectedClubs, View.VISIBLE);
        clubRecyclerView.setAdapter(clubAdapter);

        btnAddToMyClubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] newClub = {club, clubDesignation};
                selectedClubs.add(newClub);
                clubAdapter.notifyDataSetChanged();
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMyInformation();
            }
        });
    }

    void showDesignationChoice() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Designations");

        builder.setMultiChoiceItems(designations, selectedDesignations, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedDesignations[which] = isChecked;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                StringBuilder selectedOptions = new StringBuilder();

                for(int i=0; i<selectedDesignations.length; i++) {

                    if(selectedDesignations[i]) {
                        selectedOptions.append(designations[i]).append(", ");
                    }
                }

                // To remove trailing comma and space
                if(selectedOptions.length()>0) {
                    selectedOptions.setLength(selectedOptions.length()-2);
                }

                designationText.setText(selectedOptions);
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void setInfo() {

        System.out.println(profileInfo.get("fullName"));
        fullName.setText(profileInfo.containsKey("fullName") ? profileInfo.get("fullName").toString() : "");

        if(profileInfo.containsKey("departmentId")) {
            departmentID = ((Number)profileInfo.get("departmentId")).intValue();
            depSpinner.setSelection(((Number)profileInfo.get("departmentId")).intValue());
            System.out.println(profileInfo.get("departmentId"));
        }

        if(profileInfo.containsKey("batchId")) {
            int position = Arrays.asList(batches).indexOf("Batch "+profileInfo.get("batchId"));
            batchId = (long) profileInfo.get("batchId");
            batchSpinner.setSelection(position);
            System.out.println(profileInfo.get("batchId"));
        }

        if(profileInfo.containsKey("sectionId")) {
            int position = Arrays.asList(sections).indexOf("Section "+profileInfo.get("sectionId"));
            sectionId = sectionIds[position];
            sectionSpinner.setSelection(position);
            System.out.println(profileInfo.get("sectionId"));
        }

        if(profileInfo.containsKey("id")) {
            System.out.println(profileInfo.get("id"));
            id.setText(profileInfo.get("id").toString());
        }

        if(profileInfo.containsKey("acronym")) {
            System.out.println(profileInfo.get("acronym"));
            acronym.setText(profileInfo.get("acronym").toString());
        }

        if(profileInfo.containsKey("notes")) {
            System.out.println(profileInfo.get("notes"));
            note.setText(profileInfo.get("notes").toString());
        }

        if(Objects.equals(accountType, "student")) {
            designationSection.setVisibility(View.GONE);
            acronymSection.setVisibility(View.GONE);
        }
        else {
            batchSection.setVisibility(View.GONE);
            sectionSection.setVisibility(View.GONE);
            getDesignations();
        }

        setProfilePic();
    }

    void setProfilePic() {

        Glide.with(EditProfileActivity.this)
                .load(profilePic)
                .error(R.drawable.icon_default_profile)
                .placeholder(R.drawable.icon_default_profile)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        progressIndicator.setVisibility(View.GONE);
                        Glide.with(EditProfileActivity.this).load(resource).circleCrop().into(profilePicture);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        progressIndicator.setVisibility(View.GONE);
                        profilePicture.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        progressIndicator.setVisibility(View.GONE);
                    }
                });
    }

    void getDesignations() {

        DatabaseReference designationsReference = rootReference.child("designations").child(profileId);
        designationsReference.keepSynced(true);

        designationsReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                designationMap = (Map<String, Object>) task.getResult().getValue();
                setDesignations();
            }
        });
    }

    void setDesignations() {

        StringBuilder selectedOptions = new StringBuilder();
        int position;

        for(Map.Entry<String, Object> entry : designationMap.entrySet()) {
            selectedOptions.append(entry.getKey()).append(", ");
            position = Arrays.asList(designations).indexOf(entry.getKey());
            selectedDesignations[position] = true;
        }

        if(selectedOptions.length()>0) {
            selectedOptions.setLength(selectedOptions.length()-2);
        }

        designationText.setText(selectedOptions);
    }

    void updateMyInformation() {

        btnUpdateProfile.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String fName = String.valueOf(fullName.getText());
        String iden = String.valueOf(id.getText());
        String acr = String.valueOf(acronym.getText());
        String nte = String.valueOf(note.getText());
        String account="student";

        if(profileInfo.size()>0) {
            profileInfo.clear();
        }

        profileInfo.put("profilePicture", profilePic);
        profileInfo.put("email", email);
        profileInfo.put("fullName", fName);
        profileInfo.put("departmentId", departmentID);
        profileInfo.put("id", iden);
        profileInfo.put("phoneNumber", phone);

        if(Objects.equals(accountType, "student")) {
            profileInfo.put("batchId", batchId);
            profileInfo.put("sectionId", sectionId);
        }
        else {
            profileInfo.put("acronym", acr);
        }

        if(!String.valueOf(note.getText()).equals("")) {
            profileInfo.put("notes", nte);
        }

        if(!Objects.equals(accountType, "student")) {
            account = "facultyMember";
        }

        rootReference.child(account).child(profileId).setValue(profileInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("Updated!");
                registerDesignations();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnUpdateProfile.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void registerDesignations() {

        Map<String, Object> designation = new HashMap<>();

        for(int i=0; i<selectedDesignations.length; i++) {
            if(selectedDesignations[i]) {
                designation.put(designations[i], true);
            }
        }

        rootReference.child("designations").child(profileId).setValue(designation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println();
                updateClubs();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnUpdateProfile.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void updateClubs() {

        if(clubDetails.size()>0) {
            clubDetails.clear();
        }

        for(int i=0; i<selectedClubs.size(); i++) {
            clubDetails.putIfAbsent(selectedClubs.get(i)[0], new HashMap<>());
            clubDetails.get(selectedClubs.get(i)[0]).put(selectedClubs.get(i)[1], true);
        }

        rootReference.child("clubs").child(profileId).setValue(clubDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("Uploaded!");
                updateProfilePic();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Failed");
                btnUpdateProfile.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void updateProfilePic() {

        if(profilePictureURI!=null) {

            compressImage(new CompletionCallback() {
                @Override
                public void onCallback(Object data) {

                    if(data!=null) {

                        profilePictureURI = (Uri) data;

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
                                btnUpdateProfile.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                System.out.println("Upload rescheduled.");
                                btnUpdateProfile.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                        }).dispatch();
                    }
                    else {
                        updateRealtimeDatabase(profilePic);
                    }
                }
            });


        }
        else {
            updateRealtimeDatabase(profilePic);
        }
    }

    void compressImage(CompletionCallback callback) {

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

    void updateRealtimeDatabase(String url) {

        String account;

        if(Objects.equals(accountType, "student")) {
            account = "student";
        }
        else {
            account = "facultyMember";
        }

        rootReference.child(account).child(profileId).child("profilePicture").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("Complete!");
                btnUpdateProfile.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Failed!");
                btnUpdateProfile.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}