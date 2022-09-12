package com.examples.atvizpro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.examples.atvizpro.R;
import com.examples.atvizpro.model.VideoModel;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class VideoProjectsAdapter extends RecyclerView.Adapter<VideoProjectsAdapter.ViewHolder> {

    private Context context;
    private List<VideoModel> list;

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
    public void reloadData(List<VideoModel> listNew) {
        this.list = listNew;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_projects, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VideoModel video = list.get(position);
        holder.itemView.setAlpha(1);
        if (video != null) {
            Glide.with(context)
                    .load(video.getThumb())
                    .transform(new CenterCrop(), new RoundedCorners(25))
                    .into(holder.img);
            holder.name.setText(video.getName());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSelected(list.get(position).getThumb());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                holder.itemView.setAlpha(0.5f);
                new AlertDialog.Builder(context)
                        .setTitle("Delete Video")
                        .setMessage("Are you sure you want to delete this video?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                if (new File(list.get(position).getThumb()).delete()) {
                                    list.remove(list.get(position));
                                    notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                holder.itemView.setAlpha(1);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                holder.itemView.setAlpha(1);
                            }
                        });
                return false;
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
