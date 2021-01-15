package com.dimotim.kubsolver.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class CheckUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Check for update", Toast.LENGTH_LONG).show();
        Log.d(BootReceiver.class.getCanonicalName(), "Check for update");
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