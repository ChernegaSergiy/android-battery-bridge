package com.chernegasergiy.battery.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import com.chernegasergiy.battery.R;

public class NotificationHelper {
    public static final int NOTIF_ID = 1;
    private static final String CHANNEL_ID = "battery_service_channel";

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification buildForegroundNotification() {
        createChannelIfNeeded();

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }

        android.content.Intent stopIntent = new android.content.Intent(context, com.chernegasergiy.battery.service.BatteryService.class);
        stopIntent.setAction("ACTION_STOP_SERVICE");
        int flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= android.app.PendingIntent.FLAG_IMMUTABLE;
        }
        android.app.PendingIntent stopPendingIntent = android.app.PendingIntent.getService(context, 0, stopIntent, flags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Notification.Action stopAction = new Notification.Action.Builder(
                    android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_menu_close_clear_cancel),
                    context.getString(R.string.action_stop_server),
                    stopPendingIntent
            ).build();
            builder.addAction(stopAction);
        } else {
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.action_stop_server), stopPendingIntent);
        }

        return builder
                .setContentTitle(context.getString(R.string.notif_title))
                .setContentText(context.getString(R.string.notif_text))
                .setSmallIcon(android.R.drawable.ic_menu_preferences)
                .setOngoing(true)
                .build();
    }
}
