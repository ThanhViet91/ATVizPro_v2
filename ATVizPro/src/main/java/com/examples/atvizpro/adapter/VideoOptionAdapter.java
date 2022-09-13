package com.examples.atvizpro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.examples.atvizpro.R;
import com.examples.atvizpro.model.VideoModel;

import java.util.ArrayList;

public class VideoOptionAdapter extends RecyclerView.Adapter<VideoOptionAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> list;

    public interface VideoOptionListener {
        void onClickItem(String text);
    }

    private VideoOptionListener listener;

    public VideoOptionAdapter(Context context, ArrayList<String> list, VideoOptionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String videoOpt = list.get(position);
        holder.name.setText(videoOpt);
        switch (videoOpt) {
            case "Trim":
                holder.img.setImageResource(R.drawable.video_trimming_24);
                break;
            case "Music":
                holder.img.setImageResource(R.drawable.music_video_24);
                break;
            case "Speed":
                holder.img.setImageResource(R.drawable.speed_skating_24);
                break;
            case "Text":
                holder.img.setImageResource(R.drawable.text_width_24);
                break;
            case "Image":
                holder.img.setImageResource(R.drawable.sticker_24);
                break;

            case "Merge":
                holder.img.setImageResource(R.drawable.merge_vertical_24);
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickItem(list.get(position));
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_item_video_thumb);
            name = itemView.findViewById(R.id.tv_video_name);
        }
    }
}
