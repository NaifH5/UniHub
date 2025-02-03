package com.tongteacrew.unihub;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SubmissionsAdapter extends RecyclerView.Adapter<SubmissionsAdapter.SubmissionsViewHolder> {

    Context context;
    ArrayList<Map<String, Object>> submissions;

    public SubmissionsAdapter(Context context, ArrayList<Map<String, Object>> submissions) {
        this.context = context;
        this.submissions = submissions;
    }

    @NonNull
    @Override
    public SubmissionsAdapter.SubmissionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubmissionsViewHolder(LayoutInflater.from(context).inflate(R.layout.card_assignment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionsAdapter.SubmissionsViewHolder holder, int position) {

        int index = holder.getAdapterPosition();

        long time = (long) submissions.get(index).get("time");
        String dateAndTime = getDate(time)+" "+getTime(time);
        holder.name.setText(String.valueOf(submissions.get(index).get("fullName")));
        holder.id.setText(String.valueOf(submissions.get(index).get("id")));
        holder.time.setText(dateAndTime);

        holder.assignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(String.valueOf(submissions.get(index).get("url"))), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            }
        });

        loadThumbnail(String.valueOf(submissions.get(index).get("url")), holder.thumbnail, holder.progressIndicator);
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    String getDate(long time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(Instant.ofEpochMilli(time));
        return formattedDate;
    }

    String getTime(long time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault());
        String formattedTime = formatter.format(Instant.ofEpochMilli(time));
        return formattedTime;
    }

    void loadThumbnail(String pdfUrl, ImageView thumbnail, CircularProgressIndicator progressIndicator) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(pdfUrl).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if(response.isSuccessful()) {

                    InputStream inputStream = response.body().byteStream();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                File tempFile = File.createTempFile("pdf_temp", ".pdf");

                                try(FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                                    byte[] buffer = new byte[1024];
                                    int length;
                                    while ((length = inputStream.read(buffer))!=-1) {
                                        fileOutputStream.write(buffer, 0, length);
                                    }
                                }

                                ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
                                PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                                PdfRenderer.Page page = pdfRenderer.openPage(0);
                                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                canvas.drawColor(Color.WHITE);
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                page.close();
                                pdfRenderer.close();
                                tempFile.delete();

                                new Handler(Looper.getMainLooper()).post(() -> {

                                    Glide.with(context)
                                            .load(bitmap)
                                            .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(20)))
                                            .placeholder(R.drawable.icon_photo)
                                            .error(R.drawable.icon_photo)
                                            .into(thumbnail);

                                    progressIndicator.setVisibility(View.GONE);
                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}
        });
    }

    public static class SubmissionsViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView name, id, time;
        RelativeLayout assignment;
        CircularProgressIndicator progressIndicator;

        public SubmissionsViewHolder(@NonNull View itemView) {
            super(itemView);
            assignment = itemView.findViewById(R.id.assignment);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            name = itemView.findViewById(R.id.name);
            id = itemView.findViewById(R.id.id);
            time = itemView.findViewById(R.id.time);
            progressIndicator = itemView.findViewById(R.id.circular_progress_indicator);
        }
    }
}
