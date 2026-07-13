package com.chernegasergiy.battery.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class BatteryTileService extends TileService {

    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTileState();
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();
        IntentFilter filter = new IntentFilter();
        filter.addAction(com.chernegasergiy.battery.ui.ServerStatusObserver.ACTION_SERVER_STATUS);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(statusReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(statusReceiver, filter);
        }
        
        // Request actual status from the service just in case
        sendBroadcast(new Intent(com.chernegasergiy.battery.ui.ServerStatusObserver.ACTION_REQUEST_STATUS).setPackage(getPackageName()));
        
        updateTileState();
    }

    @Override
    public void onStopListening() {
        unregisterReceiver(statusReceiver);
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        boolean isRunning = BatteryService.isServiceRunning;
        
        Intent serviceIntent = new Intent(this, BatteryService.class);
        if (isRunning) {
            stopService(serviceIntent);
        } else {
            com.chernegasergiy.battery.data.SettingsRepository settings = new com.chernegasergiy.battery.data.SettingsRepository(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && settings.isForegroundEnabled()) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
        
        // Optimistically update the tile state
        BatteryService.isServiceRunning = !isRunning;
        updateTileState();
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile != null) {
            boolean isRunning = BatteryService.isServiceRunning;
            tile.setState(isRunning ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            
            // On newer versions, we can update the subtitle to show the port
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (isRunning) {
                    com.chernegasergiy.battery.data.SettingsRepository settings = new com.chernegasergiy.battery.data.SettingsRepository(this);
                    tile.setSubtitle(String.valueOf(settings.getPort()));
                } else {
                    tile.setSubtitle(null);
                }
            }
            
            tile.updateTile();
        }
    }
}
