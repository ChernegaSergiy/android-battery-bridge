package com.chernegasergiy.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import java.io.PrintWriter;
import java.net.Socket;

public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        int port = intent.getIntExtra("remote_port", -1);
        Log.d(TAG, "port=" + port);

        if (port == -1) {
            Log.e(TAG, "No port in intent");
            return;
        }

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

        Log.d(TAG, "level=" + batteryPct + ", charging=" + isCharging);

        String jsonResponse = String.format(
            "{\"l\":%d,\"c\":%d,\"h\":%d,\"t\":%d,\"v\":%d,\"tech\":\"%s\"}",
            batteryPct, isCharging ? 1 : 0, health, temperature / 10, voltage, technology != null ? technology : ""
        );

        Log.d(TAG, "Sending: " + jsonResponse);

        new Thread(() -> {
            try {
                Thread.sleep(100);
                Log.d(TAG, "Connecting to 127.0.0.1:" + port);
                try (Socket socket = new Socket("127.0.0.1", port);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.print(jsonResponse);
                    Log.d(TAG, "Data sent successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending data", e);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}
