package com.chernegasergiy.battery.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.chernegasergiy.battery.R;
import com.chernegasergiy.battery.BatteryService;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BatteryService.class));
    }

    private android.content.BroadcastReceiver statusReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String status = intent.getStringExtra("status");
            android.widget.TextView tvTitle = findViewById(R.id.tvTitle);
            if ("OK".equals(status)) {
                tvTitle.setText(R.string.main_title_active);
                tvTitle.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if ("ERROR".equals(status)) {
                tvTitle.setText("Error: Port is busy!");
                tvTitle.setTextColor(android.graphics.Color.RED);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            registerReceiver(statusReceiver, new android.content.IntentFilter("com.chernegasergiy.battery.SERVER_STATUS"), android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(statusReceiver, new android.content.IntentFilter("com.chernegasergiy.battery.SERVER_STATUS"));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(statusReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        int port = 8765;
        try {
            port = Integer.parseInt(prefs.getString("pref_port", "8765"));
        } catch (NumberFormatException e) {
            // ignore
        }
        
        boolean listenAll = prefs.getBoolean("pref_network_all", false);
        String ip = listenAll ? com.chernegasergiy.battery.utils.NetworkUtils.getLocalIpAddress() : "127.0.0.1";
        
        android.widget.TextView tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText(getString(R.string.main_info_server, ip, port));
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
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
