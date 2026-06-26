package com.alaaeltaweel.thikrallah.Notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class SilentModeReceiver extends BroadcastReceiver {
    private static final String TAG = "SilentModeReceiver";
    public static final String ACTION_SILENT_ON = "com.alaaeltaweel.thikrallah.SILENT_ON";
    public static final String ACTION_SILENT_OFF = "com.alaaeltaweel.thikrallah.SILENT_OFF";
    public static final String PREF_PREVIOUS_RINGER_MODE = "previousRingerModeBeforeSilent";

    @Override
public void onReceive(Context context, Intent intent) {
    if (context == null || intent == null || intent.getAction() == null) return;

    AudioManager audioManager =
        (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    if (audioManager == null) return;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    if (ACTION_SILENT_ON.equals(intent.getAction())) {
        int currentMode = audioManager.getRingerMode();
        prefs.edit().putInt(PREF_PREVIOUS_RINGER_MODE, currentMode).apply();
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        Log.d(TAG, "Silent mode ON, previous mode was " + currentMode);
    } else if (ACTION_SILENT_OFF.equals(intent.getAction())) {
        int previousMode = prefs.getInt(PREF_PREVIOUS_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL);
        audioManager.setRingerMode(previousMode);
        Log.d(TAG, "Silent mode OFF, restored mode " + previousMode);
    }
}
    }
