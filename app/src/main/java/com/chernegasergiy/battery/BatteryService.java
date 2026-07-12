package com.chernegasergiy.battery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BatteryService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "BatteryService";
    private static final int PORT = 8765;
    private static final int NOTIF_ID = 1;
    private static final String CHANNEL_ID = "battery_service_channel";

    private Thread listenerThread;
    private ServerSocket activeServer;
    private boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("pref_port".equals(key) || "pref_network_all".equals(key)) {
            Log.d(TAG, "Network preferences changed, restarting listener...");
            startListener();
        } else if ("pref_foreground".equals(key)) {
            updateForegroundState();
        }
    }

    private void updateForegroundState() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("pref_foreground", false)) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_LOW);
                nm.createNotificationChannel(channel);
            }
            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(this);
            }
            
            Notification notification = builder
                    .setContentTitle(getString(R.string.notif_title))
                    .setContentText(getString(R.string.notif_text))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            
            startForeground(NOTIF_ID, notification);
        } else {
            stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        updateForegroundState();
        startListener();
        return Service.START_STICKY;
    }

    private void stopListener() {
        if (activeServer != null) {
            try {
                activeServer.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing server socket", e);
            }
            activeServer = null;
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
    }

    private void startListener() {
        stopListener();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int port = PORT;
        try {
            port = Integer.parseInt(prefs.getString("pref_port", String.valueOf(PORT)));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid port format, using default", e);
        }

        final int finalPort = port;
        final boolean allInterfaces = prefs.getBoolean("pref_network_all", false);
        
        listenerThread = new Thread(() -> {
            try {
                InetAddress bindAddress = InetAddress.getByName(allInterfaces ? "0.0.0.0" : "127.0.0.1");
                Log.d(TAG, "Opening ServerSocket on " + bindAddress.getHostAddress() + ":" + finalPort);
                try (ServerSocket server = new ServerSocket(finalPort, 50, bindAddress)) {
                    activeServer = server;
                    while (running && !Thread.currentThread().isInterrupted()) {
                        try (Socket client = server.accept()) {
                            Log.d(TAG, "Client connected: " + client.getInetAddress().getHostAddress());
                            
                            if (prefs.getBoolean("pref_debug_toasts", false)) {
                                final String clientIp = client.getInetAddress().getHostAddress();
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                    android.widget.Toast.makeText(BatteryService.this, getString(R.string.toast_client_connected, clientIp), android.widget.Toast.LENGTH_SHORT).show();
                                });
                            }

                            String batteryData = getBatteryData();
                            PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
                            pw.print(batteryData);
                            Log.d(TAG, "Sent: " + batteryData);
                        }
                    }
                }
            } catch (Exception e) {
                if (running) {
                    Log.e(TAG, "Error in listener", e);
                }
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
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        stopListener();
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
