package com.tongteacrew.unihub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddRoutineActivity extends AppCompatActivity {

    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    EditText session, url;
    ImageButton btnBack;
    Button btnAdd;
    ProgressBar progressBar;
    Map<Integer, String> responses = new HashMap<>();
    Map<String, String> sessionData = new HashMap<>();
    long departmentId=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_routine);

        departmentId = (long) getIntent().getSerializableExtra("departmentId");
        sessionData = (Map<String, String>) getIntent().getSerializableExtra("sessionData");

        btnBack = findViewById(R.id.btn_back);
        session = findViewById(R.id.session);
        url = findViewById(R.id.url);
        btnAdd = findViewById(R.id.btn_add);
        progressBar = findViewById(R.id.progress_bar);

        if(sessionData!=null) {
            session.setText(sessionData.entrySet().iterator().next().getKey());
            url.setText(sessionData.entrySet().iterator().next().getValue());
            btnAdd.setText("Update");
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(sessionData!=null) {

                    btnAdd.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    deleteCurrentRoutine(sessionData.entrySet().iterator().next().getKey(), new CompletionCallback() {
                        @Override
                        public void onCallback(Object data) {

                            if(data==null) {
                                Toast.makeText(AddRoutineActivity.this, "Failed to update routine!", Toast.LENGTH_SHORT).show();
                                btnAdd.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                            else {

                                String sessionId = String.valueOf(session.getText()).trim();
                                String googleSheetUrl = String.valueOf(url.getText()).trim();

                                checkDuplicateSession(sessionId, new CompletionCallback() {
                                    @Override
                                    public void onCallback(Object data) {
                                        if(data!=null) {
                                            if(!validateSession(sessionId)) {
                                                Toast.makeText(AddRoutineActivity.this, "Invalid session name!", Toast.LENGTH_SHORT).show();
                                                btnAdd.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                            }
                                            else if(!validateUrl(googleSheetUrl)) {
                                                Toast.makeText(AddRoutineActivity.this, "Invalid url!", Toast.LENGTH_SHORT).show();
                                                btnAdd.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                            }
                                            else {
                                                getData(googleSheetUrl, sessionId);
                                            }
                                        }
                                        else {
                                            Toast.makeText(AddRoutineActivity.this, "Session already exists!", Toast.LENGTH_SHORT).show();
                                            btnAdd.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });

                            }
                        }
                    });
                }
                else {

                    String sessionId = String.valueOf(session.getText()).trim();
                    String googleSheetUrl = String.valueOf(url.getText()).trim();
                    btnAdd.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    checkDuplicateSession(sessionId, new CompletionCallback() {
                        @Override
                        public void onCallback(Object data) {
                            if(data!=null) {
                                if(!validateSession(sessionId)) {
                                    Toast.makeText(AddRoutineActivity.this, "Invalid session name!", Toast.LENGTH_SHORT).show();
                                    btnAdd.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                                else if(!validateUrl(googleSheetUrl)) {
                                    Toast.makeText(AddRoutineActivity.this, "Invalid url!", Toast.LENGTH_SHORT).show();
                                    btnAdd.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                                else {
                                    getData(googleSheetUrl, sessionId);
                                }
                            }
                            else {
                                Toast.makeText(AddRoutineActivity.this, "Session already exists!", Toast.LENGTH_SHORT).show();
                                btnAdd.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    void getData(String googleSheetUrl, String sessionId) {

        String accessToken = getAccessToken();
        String spreadsheetId = extractSpreadsheetId(googleSheetUrl);
        String sheetsApiUrl = "https://sheets.googleapis.com/v4/spreadsheets/";
        OkHttpClient client = new OkHttpClient();

        for(int i=0; i<7; i++) {
            callApi(i, accessToken, spreadsheetId, sheetsApiUrl, client, sessionId, googleSheetUrl);
        }
    }

    void callApi(int index, String accessToken, String spreadsheetId, String sheetsApiUrl, OkHttpClient client, String sessionId, String googleSheetUrl) {

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String range = days[index]+"!B2:J19";
        String url = sheetsApiUrl+spreadsheetId+"/values/"+range;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer "+accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                btnAdd.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if(response.isSuccessful()) {

                    String responseData = response.body().string();
                    responses.put(index, responseData);

                    if(responses.size()==7) {
                        insertIntoDatabase(sessionId, googleSheetUrl);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnAdd.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
                else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnAdd.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    System.err.println("Request failed: " + response.code());
                }
            }
        });
    }

    String getAccessToken() {

        try {

            String jsonToString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"unihub-98c4e\",\n" +
                    "  \"private_key_id\": \"ccb25bde35c0e89d3d233cc74b69c4e2a499c7fe\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCzsKGIsXEyTP/j\\nF6Vyn1qdvUNwFO7CXWle67YYEXhvMYHHb4EVoU/iaKHgMbiGGdCoEp8zEViEfafJ\\n3/N1npQ/arRTzPf/Y2dP2bgSNiTIJM7/QdwYugMf8LvLHHD9SgKJ2obtYt4F6TKk\\n/dPnmm/8j41h/1RH6APIqn8FqHhJ/3OZvy3YAiTjzL1fRmz0VqfMdhuSWvbA4RhO\\n4WY+bsSFwxGk+Wkv8oI/SQn4/bLdtZi2SbTfSX5H+tGged09mzWUrPcFsOcyAP38\\nv3dBlU6gdLM8hR2wGfovi9NNZEyoL7BFNNMvpLDTcIWcEmOjPMzePlCVmA5HrtYW\\nflB/fU3ZAgMBAAECggEABmIH2usysi66CD8WyXOPrHrEon6b3Juk2pJU7ZXxSUHg\\n8kyrsTEBvXEqDuS1QU45cz72GMJM+qfcBffGli8D5RzNOwzU4mWNjfCV8U+rDtD/\\n6WFViCtTYRcUFKr1+IlgfbUdheE1MdCO0/2QQXHi+H3A7/I59DPxrncf+/t4CmjR\\nVNbkTyeGpOSmT7+V6VORsucZYVpCavcK817ieoRYa65odapPYvsG/fI1INBKIdby\\nWYCJMwNELg9gxH0G+4icA4WHaH75ks2jLcYgPjyZCSDwsngqowgz2loWCt9HgQNQ\\n9JXkvD/uEmDkxyhlO01q2YMYCNTPZ2DEsjkp/0BBgQKBgQDlWpvn1+MorsKWtYQA\\nV1wmM/Z+1Hyez6nJKtCKJQZqiLWiU0mFULN9zj46OqIGA18pCAQDnO5Sla1Q0fJS\\naUui3LBF63EBHjgWrPQwe9EOhzIz86bRtJ6gbzY4SijYfoTj3jZ4yymUHv32HIrt\\n03E1JK4XufZi//4E5vxJAxJd9QKBgQDIkO174OivL2YrdTULvj7Kx+p3vlkXJdbE\\nwjbsqO6gXRJCAVPX2pUDdG3h4rX2hAGFn+EbUNIFuPMFOFbGlsZiwl/HUBc0LWIy\\nl8m8LQq1WiRiwizpGfXBRyiyOE9tF8M23B+jLIuSVY0LiKMg85KdHvU4bSIVRCch\\nypMo3rL91QKBgQDSpuslAOtZtVFyHJt1uMchK37NtJoVPwRhiNpq12DSPmgdBEQ1\\nlw6UkPYkgy/HOBeR1xPgwaU+4syBu6LGQIHAvtOEFKAA9+FqKkZJtZ8oqdHZV4Mz\\nfqJnFl4FS0/CsEmcBL+hKHAy5Fg7ULHlh9ulhOAFWL7M5PRJSmITKSgYdQKBgQCQ\\na8OF+zqxwujIDDrpPNGMRQ4xsVAHmgifX9Ya7b3+nWYjPz93Y/7/INxq1kv+uak6\\n5hg7CiRhWH8t2BasIy+xN5OuOp6qxK88DQ6HwMtAMSuYLYgXRckvpqTISEHxJTY9\\nj538anwKIC5TCs2kUZ/WIc+kFPmA5LVk4LC8sjejDQKBgQDEsO7T02J3KHeJk7sg\\nNtRgUZxQR9h6qksQ9/Y88JNmd95TIjD5QT/9mM8tsLsjrHHxl50EYQ+4DVpWHKDi\\n2XkM5SYBa4KAH8S+CZ6LpJir4M4VRYv/gA54CztbdDnDWvS6KQtOk5ZY7o1eQiU0\\nf6OF0I4thLt2RSt7/uMbex0WYA==\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-wizdi@unihub-98c4e.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"111666378841118393752\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-wizdi%40unihub-98c4e.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";

            InputStream stream = new ByteArrayInputStream(jsonToString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Arrays.asList("https://www.googleapis.com/auth/spreadsheets"));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();

        }
        catch(Exception e) {
            System.out.println(e);
            return null;
        }
    }

    void insertIntoDatabase(String sessionId, String googleSheetUrl) {

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> routineData = new HashMap<>();
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> facultyRoutineDetails = new HashMap<>();
        HashMap<String, HashMap<String, HashMap<String, Object>>> coursesData = new HashMap<>();

        for(int i=0; i<7; i++) {

            try {

                final int index = i;
                ArrayList<String> times = new ArrayList<>();

                JSONObject jsonObject = new JSONObject(responses.get(index));
                String values = jsonObject.getString("values");
                JSONArray jsonArray = new JSONArray(values);

                String regex = "\\b\\d{1,2}:\\d{2}(AM|PM|am|pm)?-\\d{1,2}:\\d{2}(AM|PM|am|pm)?\\b";
                Pattern pattern = Pattern.compile(regex);
                JSONArray time = jsonArray.getJSONArray(0);

                for(int j=2; j<time.length(); j++) {

                    Matcher matcher = pattern.matcher(time.getString(j));

                    while(matcher.find()) {
                        times.add(matcher.group());
                    }
                }

                for(int j=1; j<jsonArray.length(); j++) {

                    HashMap<String, Object> timeRange = new HashMap<>();

                    JSONArray data = jsonArray.getJSONArray(j);
                    String batch = String.valueOf(data.get(0));
                    String section = String.valueOf(data.get(1));

                    for(int k=2; k<times.size()+2; k++) {

                        HashMap<String, Object> routineDetails = new HashMap<>();

                        if(!data.isNull(k)) {

                            String[] parts = String.valueOf(data.get(k)).split("\\s+", 3);

                            if(parts.length==3) {

                                routineDetails.put("courseCode", parts[0]);
                                routineDetails.put("acronym", parts[1]);
                                routineDetails.put("room", parts[2]);

                                HashMap<String, Object> facultyRoutineDetail = new HashMap<>();
                                facultyRoutineDetail.put("courseCode", parts[0]);
                                facultyRoutineDetail.put("room", parts[2]);
                                facultyRoutineDetail.put("batch", batch);
                                facultyRoutineDetail.put("section", section);

                                facultyRoutineDetails
                                        .computeIfAbsent(parts[1], l -> new HashMap<>())
                                        .computeIfAbsent(days[index], l -> new HashMap<>())
                                        .put(times.get(k-2), facultyRoutineDetail);

                                coursesData
                                        .computeIfAbsent(parts[0], l -> new HashMap<>())
                                        .computeIfAbsent(batch, l -> new HashMap<>())
                                        .put(section, "");
                            }
                        }

                        timeRange.put(times.get(k-2), routineDetails);
                    }

                    routineData
                            .computeIfAbsent(batch, k -> new HashMap<>())
                            .computeIfAbsent(section, k -> new HashMap<>())
                            .put(days[index], timeRange);
                }
            }
            catch(Exception e) {
                System.out.println("Error: "+e);
            }
        }

        String sessionsKey = rootReference.child("sessions").child(String.valueOf(departmentId)).push().getKey();
        String routineUrlKey = rootReference.child("routineUrl").child(String.valueOf(departmentId)).push().getKey();

        Map<String, Object> updates = new HashMap<>();
        updates.put("courses/"+departmentId+"/" + sessionId, coursesData);
        updates.put("routine/"+departmentId+"/" + sessionId, routineData);
        updates.put("routineUrl/"+departmentId+"/"+routineUrlKey+"/"+sessionId, googleSheetUrl);
        updates.put("sessions/"+departmentId+"/"+sessionsKey, sessionId);
        updates.put("facultyRoutine/"+sessionId, facultyRoutineDetails);

        rootReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    System.out.println("All updates completed successfully!");
                }
                else {
                    System.out.println("Failed! " + task.getException());
                }
            }
        });
    }

    void deleteCurrentRoutine(String session, CompletionCallback callback) {

        DatabaseReference sessionReference = rootReference.child("routineUrl").child(String.valueOf(departmentId)).child(session);

        sessionReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {

                    DatabaseReference sessionReference = rootReference.child("routine").child(String.valueOf(departmentId)).child(session);

                    sessionReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {

                                DatabaseReference sessionReference = rootReference.child("sessions");
                                sessionReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            String value = snapshot.getValue(String.class);
                                            if(session.equals(value)) {
                                                String key = snapshot.getKey();
                                                DatabaseReference sessionReference = rootReference.child("sessions").child(key);
                                                sessionReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        callback.onCallback(true);
                                                    }
                                                });
                                                break;
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        callback.onCallback(null);
                                    }
                                });
                            }
                            else {
                                callback.onCallback(null);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onCallback(null);
                        }
                    });
                }
                else {
                    callback.onCallback(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(null);
            }
        });
    }

    void checkDuplicateSession(String sessionId, CompletionCallback callback) {

        DatabaseReference sessionReference = rootReference.child("sessions").child(sessionId);

        sessionReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists()) {
                    callback.onCallback(null);
                }
                else {
                    callback.onCallback(true);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(null);
            }
        });
    }

    String extractSpreadsheetId(String googleSheetUrl) {

        String regex = "https://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(googleSheetUrl);

        if(matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    boolean validateSession(String sessionId) {
        String regex = "^(Fall|Spring|Summer)-\\d{4}$";
        return !sessionId.isEmpty() && sessionId.matches(regex);
    }

    boolean validateUrl(String googleSheetUrl) {
        return !googleSheetUrl.isEmpty();
    }
}