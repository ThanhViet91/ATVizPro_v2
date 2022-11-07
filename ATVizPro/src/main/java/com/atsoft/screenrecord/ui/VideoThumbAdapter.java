package com.atsoft.screenrecord.ui;

import static com.atsoft.screenrecord.ui.VideoTrimmerUtil.MAX_COUNT_RANGE;
import static com.atsoft.screenrecord.ui.VideoTrimmerUtil.VIDEO_FRAMES_WIDTH;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atsoft.screenrecord.R;

import java.util.ArrayList;
import java.util.List;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2018/05/30/4:20 PM
 * version: 1.0
 * description:
 */
public class VideoThumbAdapter extends RecyclerView.Adapter {
  private List<Bitmap> mBitmaps = new ArrayList<>();
  private LayoutInflater mInflater;
  private Context context;

  public VideoThumbAdapter(Context context) {
    this.context = context;
    this.mInflater = LayoutInflater.from(context);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ThumbViewHolder(mInflater.inflate(R.layout.video_thumb_item_layout, parent, false));
  }

  @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    ((ThumbViewHolder) holder).thumbImageView.setImageBitmap(mBitmaps.get(position));
  }

  @Override public int getItemCount() {
    return mBitmaps.size();
  }

  public void addBitmaps(Bitmap bitmap) {
    mBitmaps.add(bitmap);
    notifyDataSetChanged();
  }


  public void resetBitmap() {
    mBitmaps = new ArrayList<>();
    mBitmaps.clear();
  }
  private final class ThumbViewHolder extends RecyclerView.ViewHolder {
    ImageView thumbImageView;

    ThumbViewHolder(View itemView) {
      super(itemView);
      thumbImageView = itemView.findViewById(R.id.thumb);
      LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) thumbImageView.getLayoutParams();
      layoutParams.width = VIDEO_FRAMES_WIDTH / MAX_COUNT_RANGE;
      thumbImageView.setLayoutParams(layoutParams);
    }
  }
}
