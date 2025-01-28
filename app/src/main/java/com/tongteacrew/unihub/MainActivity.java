package com.tongteacrew.unihub;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import android.Manifest;
import android.os.StrictMode;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(isGranted) {
                    System.out.println("FCM SDK (and the app) can post notifications.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> true);

        // Persisting Firebase data
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Ask permission to send notifications
        //askNotificationPermission();
        createNotificationChannel();

        // Initializing Cloudinary SDK
        initializeSDK();

        // For Google Credentials to work
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(mAuth.getCurrentUser()!=null) {

                    if(getIntent().getExtras()!=null) {

                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("fullName", getIntent().getExtras().getString("fullName"));
                        userData.put("profilePicture", getIntent().getExtras().getString("profilePicture"));
                        userData.put("messageId", getIntent().getExtras().getString("messageId"));
                        userData.put("id", getIntent().getExtras().getString("id"));
                        userData.put("deviceToken", getIntent().getExtras().getString("deviceToken"));

                        intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.putExtra("userData", (Serializable) userData);
                        startActivity(intent);
                    }
                    else {

                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                else {

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 1000);
    }

    void initializeSDK() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "doobgozxj");
        config.put("api_key", "452217878946171");
        config.put("api_secret", "6pkYnzNrcg0jeo7FN6d8kvpZgNU");
        MediaManager.init(MainActivity.this, config);
    }

    private void askNotificationPermission() {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) {

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) {
                System.out.println("FCM SDK (and your app) can post notifications.");
            }
            else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "default_notification_channel_id",
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}