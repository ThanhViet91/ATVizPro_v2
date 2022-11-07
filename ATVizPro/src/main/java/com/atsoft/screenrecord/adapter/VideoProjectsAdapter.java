package com.atsoft.screenrecord.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.model.VideoModel;
import com.atsoft.screenrecord.ui.utils.MyUtils;

import java.io.File;
import java.util.ArrayList;

public class VideoProjectsAdapter extends RecyclerView.Adapter<VideoProjectsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<VideoModel> list;
    private boolean selectable = false;

    public interface VideoProjectsListener {
        void onSelected(VideoModel videoModel);
    }

    private VideoProjectsListener listener;

    public VideoProjectsAdapter(Context context, ArrayList<VideoModel> list2) {
        this.context = context;
        this.list = list2;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    public void setVideoProjectsListener(VideoProjectsListener listener2) {
        this.listener = listener2;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<VideoModel> listNew) {
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

    @SuppressLint({"NotifyDataSetChanged", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VideoModel video = this.list.get(position);
        if (selectable) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(video.isSelected());
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.duration.setText(video.getDuration());
        holder.size.setText(String.format("%.1f MB", MyUtils.fileSize(new File(video.getPath()))));
        Glide.with(context)
                .load(video.getPath())
                .thumbnail(0.1f)
                .into(holder.img);
        holder.name.setText(video.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (selectable) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
                list.get(position).setSelected(holder.checkBox.isChecked());
            }
            listener.onSelected(list.get(position));
        }});
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            list.get(position).setSelected(holder.checkBox.isChecked());
            listener.onSelected(list.get(position));
        }});
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
        private TextView size;
        private final CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            img = itemView.findViewById(R.id.img_item_video_thumb);
            name = itemView.findViewById(R.id.tv_video_name);
            duration = itemView.findViewById(R.id.tv_video_duration);
            size = itemView.findViewById(R.id.tv_video_size);
        }
    }
}
