package com.chernegasergiy.battery.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Button;
import com.chernegasergiy.battery.R;
import com.chernegasergiy.battery.data.BatteryDataProvider;
import com.chernegasergiy.battery.data.BatteryInfo;
import com.chernegasergiy.battery.service.BatteryService;

public class MainActivity extends Activity {
    private ServerStatusObserver statusObserver;
    private android.widget.Switch switchServer;
    private TextView tvChargeLarge;
    private TextView tvChargeState;
    private TextView tvTemp;
    private TextView tvAcPower;
    private TextView tvLog;
    private BatteryDataProvider batteryDataProvider;
    private boolean isProgrammaticChange = false;

    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                updateTelemetry();
            } else if ("com.chernegasergiy.battery.ACTION_NEW_LOG".equals(intent.getAction())) {
                updateLog();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvChargeLarge = findViewById(R.id.tvChargeLarge);
        tvChargeState = findViewById(R.id.tvChargeState);
        tvTemp = findViewById(R.id.tvTemp);
        tvAcPower = findViewById(R.id.tvAcPower);
        tvLog = findViewById(R.id.tvLog);
        batteryDataProvider = new BatteryDataProvider(this);

        if (getActionBar() != null) {
            getActionBar().setTitle(R.string.app_name);
        }

        statusObserver = new ServerStatusObserver(this, status -> {
            boolean isOk = ServerStatusObserver.STATUS_OK.equals(status);
            if (switchServer != null) {
                isProgrammaticChange = true;
                switchServer.setChecked(isOk);
                isProgrammaticChange = false;
            }
        });

        if (savedInstanceState == null) {
            com.chernegasergiy.battery.data.SettingsRepository settings = new com.chernegasergiy.battery.data.SettingsRepository(this);
            if (settings.isAutoStartOnAppLaunchEnabled()) {
                startService(new Intent(this, BatteryService.class));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        statusObserver.register();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(ServerStatusObserver.ACTION_REQUEST_STATUS).setPackage(getPackageName()));
        
        if (switchServer != null) {
            isProgrammaticChange = true;
            switchServer.setChecked(com.chernegasergiy.battery.service.BatteryService.isServiceRunning);
            isProgrammaticChange = false;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction("com.chernegasergiy.battery.ACTION_NEW_LOG");
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dataReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(dataReceiver, filter);
        }
        
        updateUI();
        updateTelemetry();
        updateLog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dataReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        statusObserver.unregister();
    }

    private void updateUI() {
        com.chernegasergiy.battery.data.SettingsRepository settings = new com.chernegasergiy.battery.data.SettingsRepository(this);
        int port = settings.getPort();
        String ip = settings.isListenAllInterfaces() ? "0.0.0.0" : "127.0.0.1";
        
        if (getActionBar() != null) {
            getActionBar().setSubtitle(ip + ":" + port);
        }
    }

    private void updateTelemetry() {
        BatteryInfo info = batteryDataProvider.getBatteryInfo();
        String chargeState = getString(info.isCharging ? R.string.telemetry_charging : R.string.telemetry_discharging);
        String acPowerState = getString(info.isCharging ? R.string.telemetry_ac_connected : R.string.telemetry_ac_disconnected);
        
        if (tvChargeLarge != null) {
            tvChargeLarge.setText(info.percent + "%");
            tvChargeState.setText(chargeState);
            tvTemp.setText(String.format(java.util.Locale.US, "%.1f°C", info.temperatureCelsius));
            tvAcPower.setText(acPowerState);
        }
    }

    private void updateLog() {
        android.content.SharedPreferences prefs = getSharedPreferences("logs", Context.MODE_PRIVATE);
        String logs = prefs.getString("console_output", "");
        if (tvLog != null) {
            tvLog.setText(logs);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        MenuItem toggleItem = menu.findItem(R.id.action_toggle_server);
        if (toggleItem != null && toggleItem.getActionView() != null) {
            switchServer = toggleItem.getActionView().findViewById(R.id.switchServer);
            if (switchServer != null) {
                isProgrammaticChange = true;
                switchServer.setChecked(com.chernegasergiy.battery.service.BatteryService.isServiceRunning);
                isProgrammaticChange = false;

                switchServer.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isProgrammaticChange) return;
                    
                    if (isChecked) {
                        startService(new Intent(MainActivity.this, BatteryService.class));
                    } else {
                        stopService(new Intent(MainActivity.this, BatteryService.class));
                    }
                });
                // Request current status to set the switch correctly (async fallback)
                sendBroadcast(new Intent(ServerStatusObserver.ACTION_REQUEST_STATUS).setPackage(getPackageName()));
            }
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_about) {
            new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
