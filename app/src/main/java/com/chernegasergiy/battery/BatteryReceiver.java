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
        int level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        int status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);

        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                              status == BatteryManager.BATTERY_STATUS_FULL);

        String statusStr = isCharging ? "charging" : "discharging";
        String jsonResponse = String.format("{\"l\":%d,\"c\":%s,\"s\":\"%s\"}",
            level, isCharging ? "true" : "false", statusStr);

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
