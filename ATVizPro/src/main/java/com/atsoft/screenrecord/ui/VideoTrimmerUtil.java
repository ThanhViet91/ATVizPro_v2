package com.atsoft.screenrecord.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.atsoft.screenrecord.utils.DisplayUtil;

import iknow.android.utils.callback.SingleCallback;
import iknow.android.utils.thread.BackgroundExecutor;

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoTrimmerUtil {

  private static final String TAG = VideoTrimmerUtil.class.getSimpleName();
  public static final long MIN_SHOOT_DURATION = 3000L;// 最小剪辑时间3s
  public static final int VIDEO_MAX_TIME = 10000;// 1000秒
  public static final long MAX_SHOOT_DURATION = VIDEO_MAX_TIME * 1000L;//视频最多剪切多长时间10s

  public static final int MAX_COUNT_RANGE = 16;  //seekBar的区域内一共有多少张图片
  private static final int SCREEN_WIDTH_FULL = DisplayUtil.getDeviceWidth();
  public static final int RECYCLER_VIEW_PADDING = DisplayUtil.dpToPx(20);
  public static final int VIDEO_FRAMES_WIDTH = SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2;
  public static final int THUMB_WIDTH = (SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2) / VIDEO_MAX_TIME;
  private static final int THUMB_HEIGHT = DisplayUtil.dpToPx(50);

  public static void shootVideoThumbInBackground(final Context context, final Uri videoUri, final int totalThumbsCount, final long startPosition,
      final long endPosition, final SingleCallback<Bitmap, Integer> callback) {
    BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0L, "") {
      @Override public void execute() {
        try {
          MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
          mediaMetadataRetriever.setDataSource(context, videoUri);
          // Retrieve media data use microsecond
          long interval = (endPosition - startPosition) / (totalThumbsCount - 1);
          for (long i = 0; i < totalThumbsCount; ++i) {
            long frameTime = startPosition + interval * i;
            Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(frameTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if(bitmap == null) continue;
            try {
              bitmap = Bitmap.createScaledBitmap(bitmap, THUMB_WIDTH, THUMB_HEIGHT, false);
            } catch (final Throwable t) {
              t.printStackTrace();
            }
            callback.onSingleCallback(bitmap, (int) interval);
          }
          mediaMetadataRetriever.release();
        } catch (final Throwable e) {
          Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
      }
    });
  }

}
