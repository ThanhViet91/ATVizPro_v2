package com.examples.atvizpro.adapter;

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
import com.examples.atvizpro.R;
import com.examples.atvizpro.model.PhotoModel;

import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.PhotoViewHolder> {

    private Context context;
    private List<PhotoModel> list;

    public interface StickerAdapterListener {
        void onClickStickerItem(int pos);
    }

    private StickerAdapterListener listener;

    public StickerAdapter(Context context, List<PhotoModel> list, StickerAdapterListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker, parent, false);
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                listener.onClickStickerItem(position);
                posSelected = position;
                notifyDataSetChanged();
            }
        });
//        container.addView(view);
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_sticker);
        }
    }
}
