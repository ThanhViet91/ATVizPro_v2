package com.examples.atscreenrecord.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.model.PhotoModel;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.SliderPhotoViewHolder> {

    private Context context;
    private List<PhotoModel> list;

    public PhotoAdapter(Context context, List<PhotoModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SliderPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new SliderPhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderPhotoViewHolder holder, int position) {
        PhotoModel photo = list.get(position);
        if (photo != null) {
            Glide.with(context).load(photo.getResourceId()).into(holder.img);
        }
//        container.addView(view);
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public class SliderPhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public SliderPhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_item_video_thumb);
        }
    }
}
