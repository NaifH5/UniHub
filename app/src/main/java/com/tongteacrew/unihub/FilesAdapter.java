package com.tongteacrew.unihub;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    Context context;
    ArrayList<Uri> selectedMedia;
    LinearLayout selectedMediaWindow;

    public FilesAdapter(Context context, ArrayList<Uri> selectedMedia, LinearLayout selectedMediaWindow) {
        this.context = context;
        this.selectedMedia = selectedMedia;
        this.selectedMediaWindow = selectedMediaWindow;
    }

    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FilesViewHolder(LayoutInflater.from(context).inflate(R.layout.card_files, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FilesViewHolder holder, int position) {

        Glide.with(context)
                .load(selectedMedia.get(position))
                .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(20)))
                .placeholder(R.drawable.icon_photo)
                .error(R.drawable.icon_photo)
                .into(holder.mediaThumbnail);

        holder.removeMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedMedia.remove(holder.getAdapterPosition());
                notifyDataSetChanged();

                if(selectedMedia.size()==0) {
                    selectedMediaWindow.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedMedia.size();
    }

    public static class FilesViewHolder extends RecyclerView.ViewHolder {

        ImageView mediaThumbnail;
        ImageButton removeMedia;

        public FilesViewHolder(@NonNull View itemView) {

            super(itemView);
            mediaThumbnail = itemView.findViewById(R.id.media_thumbnail);
            removeMedia = itemView.findViewById(R.id.remove_media);
        }
    }
}
