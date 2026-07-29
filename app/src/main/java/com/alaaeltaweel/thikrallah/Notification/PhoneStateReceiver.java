package com.alaaeltaweel.thikrallah.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (!TelephonyManager.EXTRA_STATE_IDLE.equals(state)) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("pending_silent_off", false)) return;

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return;

        int previousMode = prefs.getInt(SilentModeReceiver.PREF_PREVIOUS_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL);
        audioManager.setRingerMode(previousMode);
        prefs.edit().putBoolean("pending_silent_off", false).apply();
    }
}
