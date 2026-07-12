package com.chernegasergiy.battery.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.chernegasergiy.battery.R;
import com.chernegasergiy.battery.service.BatteryService;

public class MainActivity extends Activity implements ServerStatusObserver.OnStatusChangedListener {
    
    private ServerStatusObserver statusObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        statusObserver = new ServerStatusObserver(this, this);

        startService(new Intent(this, BatteryService.class));

        android.widget.Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(v -> {
            startService(new Intent(this, BatteryService.class));
            android.widget.Toast.makeText(this, "Restarting...", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onServerStatusChanged(String status) {
        android.widget.TextView tvTitle = findViewById(R.id.tvTitle);
        if (ServerStatusObserver.STATUS_OK.equals(status)) {
            tvTitle.setText(R.string.main_title_active);
            tvTitle.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        } else if (ServerStatusObserver.STATUS_ERROR.equals(status)) {
            tvTitle.setText(R.string.main_title_stopped);
            tvTitle.setTextColor(android.graphics.Color.RED);
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
        boolean listenAll = settings.isListenAllInterfaces();
        
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
