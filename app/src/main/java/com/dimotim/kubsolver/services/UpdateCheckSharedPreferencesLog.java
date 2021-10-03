package com.dimotim.kubsolver.services;

import android.content.Context;

import com.dimotim.kubsolver.KubPreferences_;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.Value;

@Value
public class UpdateCheckSharedPreferencesLog {
    KubPreferences_ kubPreferences;

    public UpdateCheckSharedPreferencesLog(Context context) {
        this.kubPreferences = new KubPreferences_(context);
    }

    public void updateTimeLastSuccessCheck() {
        String preLastTime = getUpdateTimeLastSuccessCheck();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.getDefault());
        kubPreferences
                .edit()
                .lastUpdateCheckSuccess().put(formatter.format(new Date()))
                .preLastUpdateCheckSuccess().put(preLastTime)
                .apply();
    }

    public String getUpdateTimeLastSuccessCheck() {
        return kubPreferences.lastUpdateCheckSuccess().getOr("");
    }

    public String getUpdateTimePreLastSuccessCheck() {
        return kubPreferences.preLastUpdateCheckSuccess().getOr("");
    }
}
