package com.chernegasergiy.battery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BatteryService.class));

        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }
}
