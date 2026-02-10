#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/battery.h>

#define LOG_TAG "BatteryJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_battery_jni_MainActivity_getBatteryData(JNIEnv *env, jobject /* this */) {
    return env->NewStringUTF("Battery JNI Native Library Loaded");
}

JNIEXPORT jstring JNICALL
Java_com_battery_jni_MainActivity_getBatteryLevel(JNIEnv *env, jobject /* this */) {
    int level = -1;
    int scale = -1;

    android_batteryinfo_get_capacity(ANDROID_BATTERY_PLUGGED_AC, &level, &scale);

    char result[64];
    snprintf(result, sizeof(result), "%d / %d (%d%%)", level, scale, scale > 0 ? (level * 100 / scale) : -1);

    return env->NewStringUTF(result);
}

JNIEXPORT jstring JNICALL
Java_com_battery_jni_MainActivity_getBatteryStatus(JNIEnv *env, jobject /* this */) {
    int status = -1;
    android_batteryinfo_get_status(ANDROID_BATTERY_PLUGGED_AC, &status);

    const char *status_str;
    switch (status) {
        case 1: status_str = "Charging"; break;
        case 2: status_str = "Discharging"; break;
        case 3: status_str = "Not Charging"; break;
        case 4: status_str = "Full"; break;
        default: status_str = "Unknown"; break;
    }

    return env->NewStringUTF(status_str);
}

JNIEXPORT jstring JNICALL
Java_com_battery_jni_MainActivity_getBatteryHealth(JNIEnv *env, jobject /* this */) {
    int health = -1;
    android_batteryinfo_get_health(ANDROID_BATTERY_PLUGGED_AC, &health);

    const char *health_str;
    switch (health) {
        case 1: health_str = "Good"; break;
        case 2: health_str = "Overheat"; break;
        case 3: health_str = "Dead"; break;
        case 4: health_str = "Over Voltage"; break;
        case 5: health_str = "Unspecified Failure"; break;
        case 6: health_str = "Cold"; break;
        default: health_str = "Unknown"; break;
    }

    return env->NewStringUTF(health_str);
}

JNIEXPORT jstring JNICALL
Java_com_battery_jni_MainActivity_getBatteryTemperature(JNIEnv *env, jobject /* this */) {
    int temp_tenths = -1;
    android_batteryinfo_get_temperature(ANDROID_BATTERY_PLUGGED_AC, &temp_tenths);

    char result[64];
    if (temp_tenths >= 0) {
        snprintf(result, sizeof(result), "%.1f°C", temp_tenths / 10.0);
    } else {
        snprintf(result, sizeof(result), "N/A");
    }

    return env->NewStringUTF(result);
}

JNIEXPORT jstring JNICALL
Java_com_battery_jni_MainActivity_getBatteryVoltage(JNIEnv *env, jobject /* this */) {
    int voltage_mv = -1;
    android_batteryinfo_get_voltage(ANDROID_BATTERY_PLUGGED_AC, &voltage_mv);

    char result[64];
    if (voltage_mv >= 0) {
        snprintf(result, sizeof(result), "%d mV (%.2f V)", voltage_mv, voltage_mv / 1000.0);
    } else {
        snprintf(result, sizeof(result), "N/A");
    }

    return env->NewStringUTF(result);
}

JNIEXPORT jstring JNICALL
Java_com_battery_jni_MainActivity_getBatteryTechnology(JNIEnv *env, jobject /* this */) {
    const char *technology = "Unknown";
    android_batteryinfo_get_technology(ANDROID_BATTERY_PLUGGED_AC, &technology);

    return env->NewStringUTF(technology ? technology : "Unknown");
}

} // extern "C"
