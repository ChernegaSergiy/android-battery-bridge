package com.chernegasergiy.battery.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

public class ServerStatusObserver {
    public static final String ACTION_SERVER_STATUS = "com.chernegasergiy.battery.SERVER_STATUS";
    public static final String EXTRA_STATUS = "status";
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";

    public interface OnStatusChangedListener {
        void onServerStatusChanged(String status);
    }

    private final Context context;
    private final OnStatusChangedListener listener;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SERVER_STATUS.equals(intent.getAction())) {
                String status = intent.getStringExtra(EXTRA_STATUS);
                if (listener != null) {
                    listener.onServerStatusChanged(status);
                }
            }
        }
    };

    public ServerStatusObserver(Context context, OnStatusChangedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void register() {
        IntentFilter filter = new IntentFilter(ACTION_SERVER_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver, filter);
        }
    }

    public void unregister() {
        context.unregisterReceiver(receiver);
    }
}
