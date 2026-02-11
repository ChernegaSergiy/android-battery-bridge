package com.chernegasergiy.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import java.io.PrintWriter;
import java.net.Socket;

public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int port = intent.getIntExtra("remote_port", -1);
        if (port == -1) return;

        final PendingResult pendingResult = goAsync();

        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int batteryPct = level * 100 / scale;

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                              status == BatteryManager.BATTERY_STATUS_FULL);

        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

        String jsonResponse = String.format(
            "{\"l\":%d,\"c\":%d,\"h\":%d,\"t\":%d,\"v\":%d,\"tech\":\"%s\"}",
            batteryPct, isCharging ? 1 : 0, health, temperature / 10, voltage, technology != null ? technology : ""
        );

        new Thread(() -> {
            try {
                Thread.sleep(50);
                try (Socket socket = new Socket("127.0.0.1", port);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.print(jsonResponse);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}
