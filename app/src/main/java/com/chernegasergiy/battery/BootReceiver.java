package com.chernegasergiy.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("pref_autostart", false)) {
                context.startService(new Intent(context, BatteryService.class));
            }
        }
    }
}
