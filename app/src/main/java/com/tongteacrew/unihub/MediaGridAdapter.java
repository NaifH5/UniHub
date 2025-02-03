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
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MediaGridAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> media;

    public MediaGridAdapter(Context context, ArrayList<String> media) {
        this.context = context;
        this.media = media;
    }

    @Override
    public int getCount() {
        return media.size();
    }

    @Override
    public Object getItem(int position) {
        return media.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView thumbnail;
        CircularProgressIndicator progressIndicator;

        if(convertView==null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(inflater!=null) {
                convertView = inflater.inflate(R.layout.card_media, null);
                thumbnail = convertView.findViewById(R.id.gridview_media_thumbnail);
                progressIndicator = convertView.findViewById(R.id.circular_progress_indicator);
            }
            else {
                return null;
            }
        }
        else {
            thumbnail = convertView.findViewById(R.id.gridview_media_thumbnail);
            progressIndicator = convertView.findViewById(R.id.circular_progress_indicator);
        }

        Glide.with(context)
                .load(media.get(position))
                .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(20)))
                .placeholder(R.drawable.icon_photo)
                .error(R.drawable.icon_photo)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(media.get(position)), "image/");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                context.startActivity(intent);
                            }
                        });

                        Glide.with(context).load(resource).apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(20))).into(thumbnail);
                        progressIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        func(media.get(position), thumbnail, progressIndicator);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(media.get(position)), "application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                context.startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        progressIndicator.setVisibility(View.GONE);
                        thumbnail.setImageDrawable(placeholder);
                    }
                });

        return convertView;
    }

    void func(String pdfUrl, ImageView thumbnail, CircularProgressIndicator progressIndicator) {

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
}
