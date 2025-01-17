package com.tongteacrew.unihub;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class SubmitAssignmentActivity extends AppCompatActivity {

    ImageButton btnBack, addFile;
    RecyclerView filesRecyclerView;
    FilesAdapter filesAdapter;
    LinearLayout selectedMedia;
    ArrayList<Uri> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        btnBack = findViewById(R.id.btn_back);
        addFile = findViewById(R.id.btn_attach_file);
        filesRecyclerView = findViewById(R.id.media_recycler_view);
        selectedMedia = findViewById(R.id.selected_media);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filesRecyclerView.setLayoutManager(layoutManager);
        //filesRecyclerView.setHasFixedSize(true);
        filesAdapter = new FilesAdapter(SubmitAssignmentActivity.this, files, selectedMedia);
        filesRecyclerView.setAdapter(filesAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
    }
}