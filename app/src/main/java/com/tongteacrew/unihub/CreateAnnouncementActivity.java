package com.tongteacrew.unihub;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Pair;

public class CreateAnnouncementActivity extends AppCompatActivity {

    TextView activityTitle;
    EditText announcementMessage, deadline;
    Button btnAnnounce;
    ImageButton btnBack, addFile;
    LinearLayout selectedMedia;
    RecyclerView filesRecyclerView;
    FilesAdapter filesAdapter;
    ArrayList<Uri> files = new ArrayList<>();
    String announcementType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        announcementType = (String) getIntent().getSerializableExtra("announcement_type");

        activityTitle = findViewById(R.id.activity_title);
        announcementMessage = findViewById(R.id.announcement_message);
        deadline = findViewById(R.id.deadline);
        btnAnnounce = findViewById(R.id.btn_announce);
        btnBack = findViewById(R.id.btn_back);
        addFile = findViewById(R.id.btn_attach_file);
        selectedMedia = findViewById(R.id.selected_media);
        filesRecyclerView = findViewById(R.id.media_recycler_view);

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

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filesRecyclerView.setLayoutManager(layoutManager);
        filesRecyclerView.setHasFixedSize(true);
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

        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getContent.launch("application/pdf");
                filesRecyclerView.scrollToPosition(filesAdapter.getItemCount()-1);
            }
        });

        deadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerFragment datePickerFragment = new DatePickerFragment(deadline);
                datePickerFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
    }
}