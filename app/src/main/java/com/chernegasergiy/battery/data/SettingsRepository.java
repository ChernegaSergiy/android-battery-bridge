package com.chernegasergiy.battery.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsRepository {
    private final SharedPreferences prefs;

    public SettingsRepository(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getPort() {
        try {
            return Integer.parseInt(prefs.getString("pref_port", "8765"));
        } catch (NumberFormatException e) {
            return 8765;
        }
    }

    public boolean isListenAllInterfaces() {
        return prefs.getBoolean("pref_network_all", false);
    }

    public boolean isWakeLockEnabled() {
        return prefs.getBoolean("pref_wakelock", false);
    }

    public boolean isDebugToastsEnabled() {
        return prefs.getBoolean("pref_debug_toasts", false);
    }
    
    public boolean isAutostartEnabled() {
        return prefs.getBoolean("pref_autostart", false);
    }
    
    public boolean isForegroundEnabled() {
        return prefs.getBoolean("pref_foreground", false);
    }
    
    public boolean isAutoStartOnAppLaunchEnabled() {
        return prefs.getBoolean("pref_autostart_app", false);
    }

    public java.util.List<String> getAllowedIps() {
        String whitelist = prefs.getString("pref_whitelist", "").trim();
        if (whitelist.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        String[] ips = whitelist.split(",");
        java.util.List<String> validIps = new java.util.ArrayList<>();
        for (String ip : ips) {
            String trimmed = ip.trim();
            if (!trimmed.isEmpty()) {
                validIps.add(trimmed);
            }
        }
        return validIps;
    }
}
