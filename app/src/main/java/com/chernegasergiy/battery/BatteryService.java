package com.chernegasergiy.battery;

import android.app.Service;
import android.content.Intent;
import java.net.InetAddress;
import java.net.ServerSocket;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.net.Socket;

public class BatteryService extends Service {
    private static final String TAG = "BatteryService";
    private static final int PORT = 8765;

    private Thread listenerThread;
    private boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        startListener();
        return Service.START_STICKY;
    }

    private void startListener() {
        if (listenerThread != null && listenerThread.isAlive()) {
            return;
        }

        listenerThread = new Thread(() -> {
            try {
                Log.d(TAG, "Opening ServerSocket on port " + PORT);
                try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName("127.0.0.1"))) {
                    while (running) {
                        try (Socket client = server.accept()) {
                            Log.d(TAG, "Client connected");
                            String batteryData = getBatteryData();
                            PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
                            pw.print(batteryData);
                            Log.d(TAG, "Sent: " + batteryData);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in listener", e);
            }
        });
        listenerThread.start();
    }

    private String getBatteryData() {
        android.content.Intent batteryIntent = registerReceiver(null, new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) {
            return "{}";
        }

        int level = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100);
        int batteryPct = level * 100 / scale;

        int status = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == android.os.BatteryManager.BATTERY_STATUS_FULL);

        int health = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1);
        int temperature = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0);
        int voltage = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0);
        String technology = batteryIntent.getStringExtra(android.os.BatteryManager.EXTRA_TECHNOLOGY);

        return String.format(
            "{\"l\":%d,\"c\":%d,\"h\":%d,\"t\":%d,\"v\":%d,\"tech\":\"%s\"}",
            batteryPct, isCharging ? 1 : 0, health, temperature / 10, voltage, technology != null ? technology : ""
        );
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
