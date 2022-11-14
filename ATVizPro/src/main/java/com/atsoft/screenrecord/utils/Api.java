package com.atsoft.screenrecord.utils;

import com.atsoft.screenrecord.model.Results;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {
//    @GET("/thing")
//    void getTimeApi(@QueryMap Map<String, String> params, Callback<String> callback);
    @GET("zone")
    Call<Results> getTimeZone(@Query("timeZone") String timeZone);
}
