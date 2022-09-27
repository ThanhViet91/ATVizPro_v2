package com.examples.atscreenrecord.adapter;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.examples.atscreenrecord.App;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.model.VideoModel;
import com.examples.atscreenrecord.ui.utils.MyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoProjectsAdapter extends RecyclerView.Adapter<VideoProjectsAdapter.ViewHolder> {

    private Context context;
    private List<VideoModel> list;

    private boolean selectable = false;


    public interface VideoProjectsListener {
        void onSelected(String path);

        void onDeleteFile(SecurityException e, Uri uri, ContentResolver contentResolver);
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_projects, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VideoModel video = list.get(position);
        if (selectable) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(video.isSelected());
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        if (video != null) {
            holder.duration.setText(video.getDuration());
            Glide.with(context)
                    .load(video.getThumb())
                    .into(holder.img);
            holder.name.setText(video.getName());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectable) {
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                    list.get(position).setSelected(holder.checkBox.isChecked());
                }
                listener.onSelected(list.get(position).getThumb());
            }
        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.get(position).setSelected(holder.checkBox.isChecked());
                listener.onSelected(list.get(position).getThumb());
            }
        });

//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                new AlertDialog.Builder(context)
//                        .setTitle("Delete Video")
//                        .setMessage("Are you sure you want to delete this video?")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            @SuppressLint("NotifyDataSetChanged")
//                            public void onClick(DialogInterface dialog, int which) {
//                                // Continue with delete operation
//                                File file = new File(list.get(position).getThumb());
//                                MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, new String[]{file.getName()},
//                                        new MediaScannerConnection.OnScanCompletedListener() {
//                                            @Override
//                                            public void onScanCompleted(String s, Uri uri) {
//                                                ContentResolver contentResolver = context.getContentResolver();
//                                                try {
//                                                    //delete object using resolver
//                                                    contentResolver.delete(uri, null, null);
//                                                    Toast.makeText(context, "The video is deleted!", Toast.LENGTH_SHORT).show();
//                                                } catch (SecurityException e) {
//                                                    listener.onDeleteFile(e, uri, contentResolver);
//                                                }
//                                            }
//                                        });
//                                list.remove(list.get(position));
//                                notifyDataSetChanged();
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                            }
//                        })
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show()
//                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                            @Override
//                            public void onCancel(DialogInterface dialogInterface) {
//                            }
//                        });
//                return false;
//            }
//        });

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
        private TextView duration;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            img = itemView.findViewById(R.id.img_item_video_thumb);
            name = itemView.findViewById(R.id.tv_video_name);
            duration = itemView.findViewById(R.id.tv_video_duration);
        }
    }
}
