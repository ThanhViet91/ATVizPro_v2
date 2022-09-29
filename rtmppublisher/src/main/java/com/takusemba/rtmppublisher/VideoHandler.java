package com.takusemba.rtmppublisher;

import android.media.MediaCodec;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

class VideoHandler {

    private static final int FRAME_RATE = 30;

    private Handler handler;
    private VideoEncoder videoEncoder;

    interface OnVideoEncoderStateListener {
        void onSpsPps(ByteBuffer sps, ByteBuffer pps);
        void onVideoDataEncoded(ByteBuffer h264Buffer, MediaCodec.BufferInfo info);
    }

    void setOnVideoEncoderStateListener(OnVideoEncoderStateListener listener) {
        videoEncoder.setOnVideoEncoderStateListener(listener);
    }

    VideoHandler(@NonNull MediaProjection mediaProjection) {
        this.videoEncoder = new VideoEncoder(mediaProjection);
        HandlerThread handlerThread = new HandlerThread("VideoHandler");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    void start(final int width, final int height, final int bitRate, final long startStreamingAt, final int density) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    videoEncoder.prepare(width, height, bitRate, FRAME_RATE, startStreamingAt, density);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            videoEncoder.start();
                        }
                    }, 500);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        });
    }

    void stop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (videoEncoder.isEncoding()) {
                    videoEncoder.stop();
                }
            }
        });
    }

}