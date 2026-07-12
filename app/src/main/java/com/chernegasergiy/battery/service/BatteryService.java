package com.chernegasergiy.battery.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.chernegasergiy.battery.R;
import com.chernegasergiy.battery.network.TcpServer;

public class BatteryService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener, TcpServer.Listener {
    private static final String TAG = "BatteryService";
    private static final int NOTIF_ID = 1;
    private static final String CHANNEL_ID = "battery_service_channel";

    private com.chernegasergiy.battery.data.SettingsRepository settings;
    private com.chernegasergiy.battery.data.BatteryDataProvider batteryDataProvider;
    private TcpServer tcpServer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        settings = new com.chernegasergiy.battery.data.SettingsRepository(this);
        batteryDataProvider = new com.chernegasergiy.battery.data.BatteryDataProvider(this);
        settings.registerChangeListener(this);
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
        if (settings.isForegroundEnabled()) {
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
        if (tcpServer != null) {
            tcpServer.stop();
            tcpServer = null;
        }
    }

    private void startListener() {
        stopListener();

        final int finalPort = settings.getPort();
        final boolean allInterfaces = settings.isListenAllInterfaces();
        
        tcpServer = new TcpServer(finalPort, allInterfaces, this);
        tcpServer.start();
    }

    @Override
    public void onServerStarted() {
        Intent statusIntent = new Intent("com.chernegasergiy.battery.SERVER_STATUS");
        statusIntent.putExtra("status", "OK");
        sendBroadcast(statusIntent);
    }

    @Override
    public void onServerError(Exception e) {
        Intent errIntent = new Intent("com.chernegasergiy.battery.SERVER_STATUS");
        errIntent.putExtra("status", "ERROR");
        sendBroadcast(errIntent);
    }

    @Override
    public void onClientConnected(String clientIp) {
        if (settings.isDebugToastsEnabled()) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                android.widget.Toast.makeText(BatteryService.this, getString(R.string.toast_client_connected, clientIp), android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public String onRequestData() {
        return batteryDataProvider.getBatteryDataJson();
    }

    @Override
    public void onDestroy() {
        if (settings != null) {
            settings.unregisterChangeListener(this);
        }
        stopListener();
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
