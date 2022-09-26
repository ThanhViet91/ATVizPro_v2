package com.examples.atscreenrecord.ui;

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public interface VideoStreamListener {
    void onClickNext();
    void onStartRecord();
    void onStopRecord();
    void onDeleteRecord();
    void onCancel();
}
