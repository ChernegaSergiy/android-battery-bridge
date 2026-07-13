package com.chernegasergiy.battery.service;

import android.app.Service;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.net.wifi.WifiManager;
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
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

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
                } else if ("pref_wakelock".equals(key)) {
                    updateLocksState();
                }
            } else if (com.chernegasergiy.battery.ui.ServerStatusObserver.ACTION_REQUEST_STATUS.equals(intent.getAction())) {
                if (tcpServer != null && tcpServer.isRunning()) {
                    onServerStarted();
                } else {
                    onServerError(new Exception("Server is not running"));
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
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(SettingsActivity.ACTION_SETTINGS_CHANGED);
        filter.addAction(com.chernegasergiy.battery.ui.ServerStatusObserver.ACTION_REQUEST_STATUS);

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

    private void updateLocksState() {
        if (settings.isWakeLockEnabled()) {
            acquireLocks();
        } else {
            releaseLocks();
        }
    }

    private void acquireLocks() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BatteryBridge::WakeLock");
                wakeLock.acquire();
                Log.d(TAG, "WakeLock acquired");
            }
        }
        if (wifiLock == null) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "BatteryBridge::WifiLock");
                wifiLock.acquire();
                Log.d(TAG, "WifiLock acquired");
            }
        }
    }

    private void releaseLocks() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
            Log.d(TAG, "WakeLock released");
        }
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
            wifiLock = null;
            Log.d(TAG, "WifiLock released");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        updateForegroundState();
        updateLocksState();
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
        final int finalPort = settings.getPort();
        final boolean allInterfaces = settings.isListenAllInterfaces();
        
        if (tcpServer == null) {
            tcpServer = new TcpServer(finalPort, allInterfaces, this);
        } else {
            tcpServer.updateConfig(finalPort, allInterfaces);
        }
        tcpServer.start();
    }

    @Override
    public void onServerStarted() {
        Intent statusIntent = new Intent(com.chernegasergiy.battery.ui.ServerStatusObserver.ACTION_SERVER_STATUS);
        statusIntent.putExtra(com.chernegasergiy.battery.ui.ServerStatusObserver.EXTRA_STATUS, com.chernegasergiy.battery.ui.ServerStatusObserver.STATUS_OK);
        sendBroadcast(statusIntent);
    }

    @Override
    public void onServerError(Exception e) {
        Intent errIntent = new Intent(com.chernegasergiy.battery.ui.ServerStatusObserver.ACTION_SERVER_STATUS);
        errIntent.putExtra(com.chernegasergiy.battery.ui.ServerStatusObserver.EXTRA_STATUS, com.chernegasergiy.battery.ui.ServerStatusObserver.STATUS_ERROR);
        sendBroadcast(errIntent);
    }

    @Override
    public void onClientConnected(String clientIp) {
        String time = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date());
        android.content.SharedPreferences.Editor editor = getSharedPreferences("logs", Context.MODE_PRIVATE).edit();
        editor.putString("last_log_time", time);
        editor.putString("last_log_ip", clientIp);
        editor.apply();
        
        Intent intent = new Intent("com.chernegasergiy.battery.ACTION_NEW_LOG");
        sendBroadcast(intent);

        if (settings.isDebugToastsEnabled()) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                android.widget.Toast.makeText(BatteryService.this, getString(R.string.toast_client_connected, clientIp), android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public String onRequestData() {
        com.chernegasergiy.battery.data.BatteryInfo info = batteryDataProvider.getBatteryInfo();
        return String.format(java.util.Locale.US,
            "{\"l\":%d,\"c\":%d,\"h\":%d,\"t\":%.0f,\"v\":%d,\"tech\":\"%s\"}",
            info.percent, info.isCharging ? 1 : 0, info.health, info.temperatureCelsius * 10.0f, info.voltage, info.technology != null ? info.technology : ""
        );
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(settingsReceiver);
        stopListener();
        releaseLocks();
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
