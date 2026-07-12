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
    private Button btnRestartServer;
    private TextView tvTelemetry;
    private TextView tvLog;
    private BatteryDataProvider batteryDataProvider;

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

        TextView tvTitle = findViewById(R.id.tvTitle);
        btnRestartServer = findViewById(R.id.btnRestartServer);
        tvTelemetry = findViewById(R.id.tvTelemetry);
        tvLog = findViewById(R.id.tvLog);
        batteryDataProvider = new BatteryDataProvider(this);

        statusObserver = new ServerStatusObserver(this, status -> {
            boolean isOk = (status == ServerStatusObserver.STATUS_OK);
            tvTitle.setText(isOk ? R.string.main_title_active : R.string.main_title_stopped);
            tvTitle.setTextColor(getResources().getColor(isOk ? android.R.color.holo_blue_light : android.R.color.holo_red_light));
            btnRestartServer.setEnabled(true);
        });

        btnRestartServer.setOnClickListener(v -> {
            startService(new Intent(this, BatteryService.class));
            android.widget.Toast.makeText(this, getString(R.string.toast_restarting), android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        statusObserver.register();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(ServerStatusObserver.ACTION_REQUEST_STATUS));
        
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
        
        TextView tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText(getString(R.string.main_info_server, ip, port));
    }

    private void updateTelemetry() {
        BatteryInfo info = batteryDataProvider.getBatteryInfo();
        String chargeState = getString(info.isCharging ? R.string.telemetry_charging : R.string.telemetry_discharging);
        String acPowerState = getString(info.isCharging ? R.string.telemetry_ac_connected : R.string.telemetry_ac_disconnected);
        
        String telemetry = getString(R.string.telemetry_format, info.percent, chargeState, info.temperatureCelsius, acPowerState);
        tvTelemetry.setText(telemetry);
    }

    private void updateLog() {
        android.content.SharedPreferences prefs = getSharedPreferences("logs", Context.MODE_PRIVATE);
        String time = prefs.getString("last_log_time", null);
        String ip = prefs.getString("last_log_ip", null);
        
        if (time == null || ip == null) {
            tvLog.setText(R.string.log_no_data);
        } else {
            tvLog.setText(getString(R.string.log_format, time, ip));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
