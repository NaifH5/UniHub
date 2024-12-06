package com.tongteacrew.unihub;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class CreatePostActivity extends AppCompatActivity {

    ImageButton addImage, btnBack;
    LinearLayout selectedMedia;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    RecyclerView filesRecyclerView;
    FilesAdapter filesAdapter;
    ArrayList<Uri> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        addImage = findViewById(R.id.btn_attach_file);
        btnBack = findViewById(R.id.btn_back);
        selectedMedia = findViewById(R.id.selected_media);

        filesRecyclerView = findViewById(R.id.media_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filesRecyclerView.setLayoutManager(layoutManager);
        filesRecyclerView.setHasFixedSize(true);
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
    }
}