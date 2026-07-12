package com.chernegasergiy.battery.data;

public class BatteryInfo {
    public final int percent;
    public final boolean isCharging;
    public final float temperatureCelsius;

    public BatteryInfo(int percent, boolean isCharging, float temperatureCelsius) {
        this.percent = percent;
        this.isCharging = isCharging;
        this.temperatureCelsius = temperatureCelsius;
    }
}
