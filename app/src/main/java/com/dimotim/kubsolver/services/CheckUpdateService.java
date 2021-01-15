package com.dimotim.kubsolver.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class CheckUpdateService extends Service {
    public CheckUpdateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "CheckForUpdate", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void setupRepeatingCheck(Context context){
        Intent intent=new Intent(context, CheckUpdateService.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(
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
        Log.d(CheckUpdateService.class.getCanonicalName(),"alart was set up");
    }
}