package com.chernegasergiy.battery.data;

public class BatteryInfo {
    public final int percent;
    public final boolean isCharging;
    public final int health;
    public final float temperatureCelsius;
    public final int voltage;
    public final String technology;

    public BatteryInfo(int percent, boolean isCharging, int health, float temperatureCelsius, int voltage, String technology) {
        this.percent = percent;
        this.isCharging = isCharging;
        this.health = health;
        this.temperatureCelsius = temperatureCelsius;
        this.voltage = voltage;
        this.technology = technology;
    }
}
