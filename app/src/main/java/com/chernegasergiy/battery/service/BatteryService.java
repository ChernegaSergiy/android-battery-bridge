package com.chernegasergiy.battery.service;

import android.app.Service;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.chernegasergiy.battery.R;
import com.chernegasergiy.battery.network.TcpServer;
import com.chernegasergiy.battery.ui.NotificationHelper;
import com.chernegasergiy.battery.ui.SettingsActivity;

public class BatteryService extends Service implements TcpServer.Listener {
    private static final String TAG = "BatteryService";

    private com.chernegasergiy.battery.data.SettingsRepository settings;
    private com.chernegasergiy.battery.data.BatteryDataProvider batteryDataProvider;
    private TcpServer tcpServer;
    private NotificationHelper notificationHelper;

    private final BroadcastReceiver settingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SettingsActivity.ACTION_SETTINGS_CHANGED.equals(intent.getAction())) {
                String key = intent.getStringExtra(SettingsActivity.EXTRA_SETTING_KEY);
                if ("pref_port".equals(key) || "pref_network_all".equals(key)) {
                    Log.d(TAG, "Network preferences changed, restarting listener...");
                    startListener();
                } else if ("pref_foreground".equals(key)) {
                    updateForegroundState();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        settings = new com.chernegasergiy.battery.data.SettingsRepository(this);
        batteryDataProvider = new com.chernegasergiy.battery.data.BatteryDataProvider(this);
        notificationHelper = new NotificationHelper(this);
        
        IntentFilter filter = new IntentFilter(SettingsActivity.ACTION_SETTINGS_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(settingsReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(settingsReceiver, filter);
        }
    }

    private void updateForegroundState() {
        if (settings.isForegroundEnabled()) {
            startForeground(NotificationHelper.NOTIF_ID, notificationHelper.buildForegroundNotification());
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
        unregisterReceiver(settingsReceiver);
        stopListener();
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
