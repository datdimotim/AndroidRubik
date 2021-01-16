package com.dimotim.kubsolver.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.dimotim.kubsolver.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class UpdateAvailableNotification {
    public static void show(Context context, String version, String downloadUrl){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);


        Notification.Builder n  = new Notification.Builder(context)
                .setContentTitle("New update available")
                .setContentText("version: "+version)
                .setSmallIcon(R.drawable.blue_button_active)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
                //.addAction(R.drawable.icon, "Call", pIntent)
                //.addAction(R.drawable.icon, "More", pIntent)
                //.addAction(R.drawable.icon, "And more", pIntent)


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Update_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Update channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            n.setChannelId(channelId);
        }

        notificationManager.notify(0, n.build());
    }
}
