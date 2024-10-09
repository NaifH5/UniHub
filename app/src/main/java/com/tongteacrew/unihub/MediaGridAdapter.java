package com.tongteacrew.unihub;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

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
                .error(R.drawable.icon_photo)
                .placeholder(R.drawable.icon_photo)
                .into(new CustomTarget<Drawable>(){

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Glide.with(context).load(resource).apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(20))).into(thumbnail);
                        progressIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        thumbnail.setImageDrawable(placeholder);
                    }
                });

        return convertView;
    }
}
