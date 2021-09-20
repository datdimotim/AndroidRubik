package com.dimotim.kubsolver.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.dimotim.kubsolver.updatecheck.HttpClient;
import com.dimotim.kubsolver.updatecheck.SchedulerProvider;
import com.dimotim.kubsolver.updatecheck.UpdatesUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;

import io.reactivex.disposables.Disposable;

@EReceiver
public class CheckUpdateReceiver extends BroadcastReceiver {
    @Bean
    protected UpdatesUtil updatesUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Check for update", Toast.LENGTH_LONG).show();
        Log.d(BootReceiver.class.getCanonicalName(), "Check for update");

        Disposable disposable = HttpClient.getCheckForUpdateService()
                .getLatestRelease()
                .map(UpdatesUtil::parseCheckResultFromGithubResponse)
                .observeOn(SchedulerProvider.ui())
                .subscribeOn(SchedulerProvider.io())
                .subscribe(
                        success -> {
                            if(updatesUtil.isSameVersion(success)){
                                Log.d(CheckUpdateReceiver.class.getCanonicalName(), "version is same: "+success);
                                return;
                            }
                            UpdateAvailableNotification.show(context, success.getTagName(), success.getHtmlUrl());

                        },
                        error -> {
                            //Toast.makeText(context, error.toString(),Toast.LENGTH_LONG).show();
                            Log.d(CheckUpdateReceiver.class.getCanonicalName(), error.toString());
                        }
                );
    }

    public static void setupRepeatingCheck(Context context){
        Intent intent=new Intent(context, CheckUpdateReceiver.class);
        intent.setAction("android.alarm.receiver");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                123,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        final long interval = 60*60*1000;

        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 10000,
                interval,
                pendingIntent
        );
        Log.d(CheckUpdateReceiver.class.getCanonicalName(),"alarm was set up");
    }
}