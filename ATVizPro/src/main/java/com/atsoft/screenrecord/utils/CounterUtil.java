package com.atsoft.screenrecord.utils;

import android.os.Handler;
import android.os.Looper;

import com.atsoft.screenrecord.ui.utils.MyUtils;

public class CounterUtil {

    private static CounterUtil INSTANCE;

    public interface ICounterUtil {
        void onTickString(String sec);
    }

    public interface ICounterUtil2 {
        void onTickString(String sec);
    }

    private ICounterUtil mCallback;
    private ICounterUtil2 mCallback2;

    private CounterUtil(ICounterUtil callback, ICounterUtil2 callback2) {
        this.mCallback = callback;
        this.mCallback2 = callback2;
    }

    private CounterUtil() {

    }

    public static CounterUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CounterUtil();
        }
        return INSTANCE;
    }

    public void setCallback (ICounterUtil callback) {
        this.mCallback = callback;
    }
    public void setCallback (ICounterUtil2 callback) {
        this.mCallback2 = callback;
    }

    private long timer = 0;
    private Handler handlerTimer = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    public void startCounter() {
       runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) mCallback.onTickString(MyUtils.parseLongToTime(timer * 1000));
                    if (mCallback2 != null)  mCallback2.onTickString(MyUtils.parseLongToTime(timer * 1000));
                    timer++;
//                    System.out.println("thanhlv mCallback.onTickString " + timer);
                    handlerTimer.postDelayed(this, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handlerTimer.postDelayed(runnable, 0);
    }

    public void stopCounter() {
        if (handlerTimer != null && runnable != null) {
            handlerTimer.removeCallbacks(runnable);
            timer = 0;
        }
    }

    public void releaseCounter() {
        if (handlerTimer != null && runnable != null) {
            handlerTimer.removeCallbacks(runnable);
            timer = 0;
        }
    }

    public void pauseCounter() {
        if (handlerTimer != null && runnable != null) {
            handlerTimer.removeCallbacks(runnable);
        }
    }

    public void resumeCounter() {
        startCounter();
    }
}