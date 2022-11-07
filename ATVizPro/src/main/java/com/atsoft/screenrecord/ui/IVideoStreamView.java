package com.atsoft.screenrecord.ui;

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public interface IVideoStreamView {
    void onClickNext();
    void onStartRecord();
    void onStopRecord();
    void onDeleteRecord();
    void onCancel();
}
