package com.examples.atscreenrecord.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.model.VideoModel;
import java.util.List;

public class VideoProjectsAdapter extends RecyclerView.Adapter<VideoProjectsAdapter.ViewHolder> {

    private final Context context;
    private List<VideoModel> list;
    private boolean selectable = false;

    public interface VideoProjectsListener {
        void onSelected(String path);
    }

    private VideoProjectsListener listener;

    public VideoProjectsAdapter(Context context, List<VideoModel> list) {
        this.context = context;
        this.list = list;
    }

    public void setVideoProjectsListener(VideoProjectsListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<VideoModel> listNew) {
        this.list = listNew;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
        notifyDataSetChanged();
    }

    public boolean getSelectable() {
        return this.selectable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_projects, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VideoModel video = list.get(position);
        if (selectable) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(video.isSelected());
        } else
            holder.checkBox.setVisibility(View.GONE);

        holder.duration.setText(video.getDuration());
        Glide.with(context)
                .load(video.getPath())
                .into(holder.img);
        holder.name.setText(video.getName());
        holder.itemView.setOnClickListener(view -> {
            if (selectable) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
                list.get(position).setSelected(holder.checkBox.isChecked());
            }
            listener.onSelected(list.get(position).getPath());
        });
//        holder.checkBox.setBackgroundColor(Color.WHITE);
        holder.checkBox.setOnClickListener(view -> {
            list.get(position).setSelected(holder.checkBox.isChecked());
            listener.onSelected(list.get(position).getPath());
        });
        holder.itemView.setOnLongClickListener(view -> {
            if (!selectable) {
                list.get(position).setSelected(true);
                listener.onSelected("longClick");
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        if (list != null) return list.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView img;
        private final TextView name;
        private final TextView duration;
        private final CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            img = itemView.findViewById(R.id.img_item_video_thumb);
            name = itemView.findViewById(R.id.tv_video_name);
            duration = itemView.findViewById(R.id.tv_video_duration);
        }
    }
}
