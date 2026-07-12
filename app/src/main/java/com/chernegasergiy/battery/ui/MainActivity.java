package com.chernegasergiy.battery.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.chernegasergiy.battery.R;
import com.chernegasergiy.battery.service.BatteryService;

public class MainActivity extends Activity {
    private ServerStatusObserver statusObserver;
    private ToggleButton btnToggleServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvTitle = findViewById(R.id.tvTitle);
        btnToggleServer = findViewById(R.id.btnToggleServer);

        statusObserver = new ServerStatusObserver(this, status -> {
            boolean isOk = (status == ServerStatusObserver.STATUS_OK);
            tvTitle.setText(isOk ? R.string.main_title_active : R.string.main_title_stopped);
            tvTitle.setTextColor(isOk ? 0xFF33B5E5 : 0xFFFF4444);
            btnToggleServer.setChecked(isOk);
        });

        btnToggleServer.setOnClickListener(v -> {
            if (btnToggleServer.isChecked()) {
                startService(new Intent(this, BatteryService.class));
                android.widget.Toast.makeText(this, getString(R.string.toast_restarting), android.widget.Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(this, BatteryService.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(ServerStatusObserver.ACTION_REQUEST_STATUS));
        updateUI();
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
        tvInfo.setText(getString(R.string.main_info_server, ip, port) + "\nСкрипт моніторингу: nohup");
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
