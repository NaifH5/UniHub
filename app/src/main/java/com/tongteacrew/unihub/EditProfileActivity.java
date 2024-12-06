package com.tongteacrew.unihub;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    RelativeLayout designationSection, acronymSection, batchSection, sectionSection, idSection;
    Spinner depSpinner, batchSpinner, sectionSpinner, clubsSpinner, clubDesignationsSpinner;
    RecyclerView clubRecyclerView;
    ClubAdapter clubAdapter;
    Button btnAddToMyClubs;
    ImageButton btnBack, selectImage;
    ImageView profilePicture;
    TextView designationText;
    EditText fullName, id, acronym, note;
    String accountType, department, batch, section, club, clubDesignation;
    ArrayList<String[]> selectedClubs = new ArrayList<>();
    Uri profilePictureURI;

    String[] departments = {"Architecture", "Bangla", "Business Administration", "Civil Engineering",
            "Computer Science & Engineering", "Electrical and Electronics Engineering", "English",
            "Islamic Studies", "Law", "Public Health", "Tourism & Hospitality Management"};
    String[] batches = {"Batch 56", "Batch 57", "Batch 58", "Batch 59", "Batch 60", "Batch 61",
            "Batch 62", "Batch 63", "Batch 64"};
    String[] sections = {"Section A", "Section B", "Section C", "Section D", "Section E", "Section F",
            "Section G", "Section H", "Section I", "Section J"};
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
        accountType = (String) getIntent().getSerializableExtra("account_type");

        batchSection = findViewById(R.id.batch_section);
        sectionSection = findViewById(R.id.section_section);
        idSection = findViewById(R.id.id_section);
        designationSection = findViewById(R.id.designation_section);
        acronymSection = findViewById(R.id.acronym_section);
        btnBack = findViewById(R.id.btn_back);
        profilePicture = findViewById(R.id.department_image);
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

        if(Objects.equals(accountType, "student")) {
            designationSection.setVisibility(View.GONE);
            acronymSection.setVisibility(View.GONE);
        }
        else if(Objects.equals(accountType, "faculty_member")) {
            batchSection.setVisibility(View.GONE);
            sectionSection.setVisibility(View.GONE);
        }

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
}