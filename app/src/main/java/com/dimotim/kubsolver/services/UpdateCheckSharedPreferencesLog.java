package com.dimotim.kubsolver.services;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.Value;

@Value
public class UpdateCheckSharedPreferencesLog {
    public static final String UPDATE_CHECK_PREFERENCES = "UPDATE_CHECK_PREFERENCES";
    public static final String TIME_LAST_SUCCESS_CHECK = "TIME_LAST_SUCCESS_CHECK";

    Context context;

    public void updateTimeLastSuccessCheck() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.getDefault());
        context.getSharedPreferences(UPDATE_CHECK_PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(TIME_LAST_SUCCESS_CHECK, formatter.format(new Date()))
                .apply();
    }

    public String getUpdateTimeLastSuccessCheck() {
        return context.getSharedPreferences(UPDATE_CHECK_PREFERENCES, Context.MODE_PRIVATE)
                .getString(TIME_LAST_SUCCESS_CHECK, "");
    }
}
