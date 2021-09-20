package com.dimotim.kubsolver.services;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dimotim.kubsolver.updatecheck.HttpClient;
import com.dimotim.kubsolver.updatecheck.UpdatesUtil;
import com.dimotim.kubsolver.updatecheck.UpdatesUtil_;
import com.dimotim.kubsolver.updatecheck.model.CheckResult;

import com.dimotim.kubsolver.updatecheck.model.ReleaseModel;
public class CheckForUpdatesWork extends Worker {

    public static final String TAG = CheckForUpdatesWork.class.getName();

    public CheckForUpdatesWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "check for update start");

        try {
            ReleaseModel releaseModel = HttpClient.getCheckForUpdateService()
                    .getLatestRelease()
                    .execute()
                    .body();

            CheckResult success = UpdatesUtil.parseCheckResultFromGithubResponse(releaseModel);
            Context context = getApplicationContext();

            ContextCompat.getMainExecutor(context).execute(() -> {
                if(UpdatesUtil_.getInstance_(context).isSameVersion(success)){
                    Log.d(CheckForUpdatesWork.class.getCanonicalName(), "version is same: "+success);
                    return;
                }
                UpdateAvailableNotification.show(context, success.getTagName(), success.getHtmlUrl());
            });
        }
        catch (Exception e) {
            Log.e(TAG, "check for update error", e);
            return Result.retry();
        }

        Log.d(TAG, "check for update success");

        return Result.success();
    }
}


