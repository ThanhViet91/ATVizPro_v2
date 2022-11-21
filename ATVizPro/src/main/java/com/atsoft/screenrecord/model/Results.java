package com.atsoft.screenrecord.model;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Results {

    @SerializedName("year")
    private int year;

    @SerializedName("month")
    private int month;

    @SerializedName("day")
    private int day;

    @SerializedName("hour")
    private int hour;

    @SerializedName("minute")
    private int minute;

    @SerializedName("seconds")
    private int seconds;

    public long getDateTimeMs() {
        long timeInMilliseconds = 0;
        String dateTime = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" +seconds;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date mDate = sdf.parse(dateTime);
            if (mDate != null) {
                timeInMilliseconds = mDate.getTime();
            } else
                return System.currentTimeMillis();
//            System.out.println("thanhlv getDateTimeMs in milli :: " + timeInMilliseconds);
        } catch (ParseException e) {
            e.printStackTrace();

            return System.currentTimeMillis();
        }
        return timeInMilliseconds;
    }


}