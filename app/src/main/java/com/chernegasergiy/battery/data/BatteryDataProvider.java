package com.chernegasergiy.battery.data;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryDataProvider {
    private final Context context;

    public BatteryDataProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    public String getBatteryDataJson() {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) {
            return "{}";
        }

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int batteryPct = level * 100 / scale;

        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL);

        int health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        int voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        String technology = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

        return String.format(
            "{\"l\":%d,\"c\":%d,\"h\":%d,\"t\":%d,\"v\":%d,\"tech\":\"%s\"}",
            batteryPct, isCharging ? 1 : 0, health, temperature / 10, voltage, technology != null ? technology : ""
        );
    }

    public BatteryInfo getBatteryInfo() {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) {
            return new BatteryInfo(0, false, 0.0f);
        }

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int batteryPct = level * 100 / scale;

        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL);

        int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);

        return new BatteryInfo(batteryPct, isCharging, temperature / 10.0f);
    }
}
