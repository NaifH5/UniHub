package com.tongteacrew.unihub;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
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
import java.util.Objects;

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

        if(Objects.equals(getFileType(selectedMedia.get(position)), "Image")) {

            Glide.with(context)
                    .load(selectedMedia.get(position))
                    .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(20)))
                    .placeholder(R.drawable.icon_photo)
                    .error(R.drawable.icon_photo)
                    .into(holder.mediaThumbnail);
        }
        else if(Objects.equals(getFileType(selectedMedia.get(position)), "PDF")) {

            Glide.with(context)
                    .load(getPdfThumbnail(selectedMedia.get(position)))
                    .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(20)))
                    .placeholder(R.drawable.icon_photo)
                    .error(R.drawable.icon_photo)
                    .into(holder.mediaThumbnail);
        }

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

    String getFileType(Uri fileUri) {

        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(fileUri);

        if(mimeType != null) {

            if(mimeType.startsWith("image/")) {
                return "Image";
            }
            else if("application/pdf".equals(mimeType)) {
                return "PDF";
            }
        }
        return "Unknown";
    }

    Bitmap getPdfThumbnail(Uri pdfUri) {

        Bitmap bitmap = null;

        try {

            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(pdfUri, "r");

            if(fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                PdfRenderer.Page page = pdfRenderer.openPage(0);
                bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                pdfRenderer.close();
                fileDescriptor.close();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
