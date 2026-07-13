package com.chernegasergiy.battery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.chernegasergiy.battery.service.BatteryService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            com.chernegasergiy.battery.data.SettingsRepository settings = new com.chernegasergiy.battery.data.SettingsRepository(context);
            if (settings.isAutostartEnabled()) {
                Intent serviceIntent = new Intent(context, BatteryService.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && settings.isForegroundEnabled()) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
