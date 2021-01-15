package com.dimotim.kubsolver.services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "boot receiver receive: "+intent.getAction(), Toast.LENGTH_LONG).show();
        Log.d(BootReceiver.class.getCanonicalName(), "BootCompleted handled, setup repeatable alarm");
        CheckUpdateReceiver.setupRepeatingCheck(context);
    }

    public static void enableBootReceiver(Context context){
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}