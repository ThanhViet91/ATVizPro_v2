package com.atsoft.screenrecord.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.model.PhotoModel;
import com.atsoft.screenrecord.utils.OnSingleClickListener;

import java.util.List;

public class Sticker2Adapter extends RecyclerView.Adapter<Sticker2Adapter.PhotoViewHolder> {

    private Context context;
    private List<PhotoModel> list;

    public interface StickerAdapterListener {
        void onClickStickerItem(int pos);
    }

    private StickerAdapterListener listener;

    public Sticker2Adapter(Context context, List<PhotoModel> list, StickerAdapterListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker2, parent, false);
        return new PhotoViewHolder(view);
    }

    private int posSelected = 0;
    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PhotoModel photo = list.get(position);
        if (photo != null) {
            Glide.with(context).load(photo.getResourceId()).into(holder.img);
        }

        if (position != posSelected) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else
            holder.itemView.setBackgroundResource(R.drawable.shape_round_bg_video_item);

        holder.itemView.setOnClickListener(new OnSingleClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSingleClick(View v) {
                listener.onClickStickerItem(position);
                posSelected = position;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_sticker);
        }
    }
}
