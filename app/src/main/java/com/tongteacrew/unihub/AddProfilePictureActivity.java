package com.tongteacrew.unihub;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.Objects;

public class AddProfilePictureActivity extends AppCompatActivity {

    ImageButton selectImage;
    ImageView bgImage, profilePicture;
    String accountType;
    Uri profilePictureURI;

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
    }
}