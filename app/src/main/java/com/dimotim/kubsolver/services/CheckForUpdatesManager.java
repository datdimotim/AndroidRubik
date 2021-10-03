package com.dimotim.kubsolver.services;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class CheckForUpdatesManager {
    public static final String WORK_TAG = "CHECK_FOR_UPDATE_WORK";

    public static void setupCheckForUpdates(Context context) {
        PeriodicWorkRequest checkForUpdatesTask = new PeriodicWorkRequest.Builder(
                CheckForUpdatesWork.class,
                1,
                TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP,checkForUpdatesTask);

    }

    public static void cancelCheckForUpdates(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG);
    }
}
