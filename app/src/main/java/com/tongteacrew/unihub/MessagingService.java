package com.tongteacrew.unihub;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = mAuth.getCurrentUser();

    @Override
    public void onNewToken(@NonNull String token) {

        if(user!=null) {

            DatabaseReference accountReference = rootReference.child("student").child(user.getUid()).child("fullName");

            accountReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful() && task.getResult().exists()) {
                        updateToken("student", token);
                    }
                    else {
                        updateToken("facultyMember", token);
                    }
                }
            });
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {

        if(message.getData().size()>0) {

            String title = "New message!";
            String body = message.getData().get("title")+": "+message.getData().get("body");

            Intent intent = new Intent(MessagingService.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("fullName", message.getData().get("fullName"));
            intent.putExtra("messageId", message.getData().get("messageId"));
            intent.putExtra("id", message.getData().get("id"));
            intent.putExtra("deviceToken", message.getData().get("deviceToken"));

            if(message.getData().get("profilePicture")!=null) {
                intent.putExtra("profilePicture", message.getData().get("profilePicture"));
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(MessagingService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            Glide.with(MessagingService.this)
                    .asBitmap()
                    .load(message.getData().get("profilePicture"))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MessagingService.this, "default_notification_channel_id")
                                    .setSmallIcon(R.drawable.unihub_logo_transparent)
                                    .setLargeIcon(resource)
                                    .setContentTitle(title)
                                    .setContentText(body)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);

                            NotificationManagerCompat manager = NotificationManagerCompat.from(MessagingService.this);

                            if(ActivityCompat.checkSelfPermission(MessagingService.this, android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
                                return;
                            }

                            manager.notify(0, builder.build());
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MessagingService.this, "default_notification_channel_id")
                                    .setSmallIcon(R.drawable.unihub_logo_transparent)
                                    .setContentTitle(title)
                                    .setContentText(body)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);

                            NotificationManagerCompat manager = NotificationManagerCompat.from(MessagingService.this);

                            if(ActivityCompat.checkSelfPermission(MessagingService.this, android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
                                return;
                            }

                            manager.notify(0, builder.build());
                        }
                    });
        }
    }

    void updateToken(String accountType, String token) {

        DatabaseReference accountReference = rootReference.child(accountType).child(user.getUid()).child("deviceToken");

        accountReference.setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    System.out.println("Token updated.");
                }
            }
        });
    }
}
