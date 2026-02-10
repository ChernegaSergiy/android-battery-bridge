package com.battery.jni;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("battery-jni");
    }

    public native String getBatteryData();
    public native String getBatteryLevel();
    public native String getBatteryStatus();
    public native String getBatteryHealth();
    public native String getBatteryTemperature();
    public native String getBatteryVoltage();
    public native String getBatteryTechnology();

    private TextView batteryInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryInfoText = findViewById(R.id.battery_info_text);
        updateBatteryInfo();
    }

    private void updateBatteryInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Battery Data: ").append(getBatteryData()).append("\n\n");
        info.append("Level: ").append(getBatteryLevel()).append("\n");
        info.append("Status: ").append(getBatteryStatus()).append("\n");
        info.append("Health: ").append(getBatteryHealth()).append("\n");
        info.append("Temperature: ").append(getBatteryTemperature()).append("\n");
        info.append("Voltage: ").append(getBatteryVoltage()).append("\n");
        info.append("Technology: ").append(getBatteryTechnology()).append("\n");

        batteryInfoText.setText(info.toString());
    }
}
