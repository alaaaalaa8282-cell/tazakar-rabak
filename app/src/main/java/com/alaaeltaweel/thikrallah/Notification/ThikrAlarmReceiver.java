package com.alaaeltaweel.thikrallah.Notification;


import android.app.NotificationChannel;

import android.app.NotificationManager;

import android.app.PendingIntent;

import android.content.BroadcastReceiver;

import android.content.Context;

import android.content.Intent;

import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import java.util.Calendar;

import android.os.Build;

import android.os.Bundle;

import android.telephony.TelephonyManager;

import android.util.Log;

import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import android.media.MediaPlayer;

import com.alaaeltaweel.thikrallah.MainActivity;

import com.alaaeltaweel.thikrallah.WakeUpActivity;

import com.alaaeltaweel.thikrallah.R;

import android.media.AudioManager;

public class ThikrAlarmReceiver extends BroadcastReceiver {
    String TAG = "ThikrAlarmReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onrecieve called");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
    PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tazakar:ThikrReceiverWakeLock");
        wakeLock.acquire(60 * 1000L);

        Bundle data = intent.getExtras();

        if (data == null) return;

        String dataType = data.getString("com.alaaeltaweel.thikrallah.datatype");

        // ✅ تنبيه قبل الصلاة بـ 15 دقيقة
        if (dataType != null && dataType.startsWith(MyAlarmsManager.DATA_TYPE_PRE_ATHAN)) {

            String prayerName = data.getString("prayer_name", "fajr");
           Log.d("ThikrAlarmReceiver", "pre-athan prayer_name: " + prayerName);

            showPreAthanNotification(context, prayerName);

            return;

        }
    // ✅ الإقامة
 if ("iqama".equals(dataType)) {
    String prayerName = data.getString("prayer_name", "fajr");
    int iqamaSound = data.getInt("iqama_sound", 1);
    showIqamaNotification(context, prayerName, iqamaSound);
    if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
    return;
}
        // لو الأذان افتح شاشة الأذان
        if (isAthanType(dataType)) {

            // ✅ منع تكرار الأذان في نفس اليوم
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            long lastAthanTime = prefs.getLong("last_athan_time_" + dataType, 0);
            long nowMs = System.currentTimeMillis();
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTimeInMillis(lastAthanTime);
            Calendar nowCal = Calendar.getInstance();
            if (lastAthanTime > 0 &&
                lastCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) &&
                lastCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
                Log.d(TAG, "Athan already played today, skipping: " + dataType);
                return;
            }
            prefs.edit().putLong("last_athan_time_" + dataType, nowMs).apply();

            // ✅ تحقق من وجود مكالمة وابعت الحالة للشاشة
            boolean isInCall = false;
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null && tm.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                    isInCall = true;
                }
            } catch (SecurityException e) {
                Log.d(TAG, "Cannot check call state");
            }

            Intent athanIntent = new Intent(context, AthanScreenActivity.class);
            athanIntent.putExtras(data);
            athanIntent.putExtra("isCallInProgress", isInCall);
            athanIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            
            context.startActivity(athanIntent);
            boolean isDuaEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("isDuaAfterAthan", false);
if (isDuaEnabled) {
    MediaPlayer mp = MediaPlayer.create(context, R.raw.dua_after_athan);
    if (mp != null) {
        mp.setOnCompletionListener(MediaPlayer::release);
        mp.start();
    }
}
} else {

            // ✅ الأذكار العادية — لا تشتغل أثناء المكالمات (فحص المكالمة الأول)
            boolean isInCallForThikr = false;
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null && tm.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                    isInCallForThikr = true;
                }
            } catch (SecurityException e) {
                Log.d(TAG, "Cannot check call state");
            }
            if (isInCallForThikr) {
                Log.d(TAG, "Call in progress, scheduling thikr after 15 min");
                android.app.AlarmManager alarmManager =
                    (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    dataType.hashCode() + 9999,
                    new Intent(context, ThikrAlarmReceiver.class).putExtras(data),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT |
                    android.app.PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + (15 * 60 * 1000),
                    pendingIntent);
                return;
            }

            
            // باقي التنبيهات تشتغل عادي
            data.putBoolean("isUserAction", false);
            Intent intent2 = new Intent(context, ThikrService.class).putExtras(data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "starting foreground service ThikrService");
                context.startForegroundService(intent2);
            } else {
                Log.d(TAG, "starting background service ThikrService");
                context.startService(intent2);
            }
        }

        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
    }
    
private void showPreAthanNotification(Context context, String prayerKey) {
    // تحويل الـ key لاسم عربي للعرض
    String prayerNameAr;
    int soundRes;
    switch (prayerKey) {
        case "fajr":    prayerNameAr = "الفجر";  soundRes = R.raw.pre_fajr;    break;
        case "dhuhr":   prayerNameAr = "الظهر";  soundRes = R.raw.pre_dhuhr;   break;
        case "asr":     prayerNameAr = "العصر";  soundRes = R.raw.pre_asr;     break;
        case "maghrib": prayerNameAr = "المغرب"; soundRes = R.raw.pre_maghrib; break;
        case "isha":    prayerNameAr = "العشاء"; soundRes = R.raw.pre_isha;    break;
        default:        prayerNameAr = "الصلاة"; soundRes = R.raw.pre_fajr;    break;
    }

    android.net.Uri soundUri = android.net.Uri.parse(
        "android.resource://" + context.getPackageName() + "/" + soundRes);

    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
if (audioManager != null) {
    audioManager.requestAudioFocus(null,
        AudioManager.STREAM_ALARM,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
}
    
    String channelId = "pre_athan_reminder_" + prayerKey;
    NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
                channelId, "تنبيه اقتراب الصلاة", NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 200, 500});
        channel.setSound(soundUri,
            new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(channel);
    
    }

    Intent stopIntent = new Intent("com.alaaeltaweel.thikrallah.STOP_SOUND");
PendingIntent pendingIntent = PendingIntent.getBroadcast(context, prayerKey.hashCode() + 1111,
        stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    Intent wakeIntent = new Intent(context, WakeUpActivity.class);
    wakeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent wakePendingIntent = PendingIntent.getActivity(context, prayerKey.hashCode() + 7777,
            wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("اقترب وقت صلاة " + prayerNameAr)
            .setContentText("تبقى " + PreferenceManager.getDefaultSharedPreferences(context).getString("preAthanMinutes_" + prayerKey, "15") + " دقيقة على صلاة " + prayerNameAr) 
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
           .setTimeoutAfter(3 * 60 * 1000L) 
            .setVibrate(new long[]{0, 500, 200, 500})
            .setSound(soundUri)
            .setContentIntent(pendingIntent) 
            .setFullScreenIntent(wakePendingIntent, true);
    PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong("last_pre_athan_play_time", System.currentTimeMillis()).apply();
        notificationManager.notify(prayerKey.hashCode(), builder.build());
}

    private boolean isAthanType(String dataType) {
        if (dataType == null) return false;
        return dataType.equals(MainActivity.DATA_TYPE_ATHAN1) ||
               dataType.equals(MainActivity.DATA_TYPE_ATHAN2) ||
               dataType.equals(MainActivity.DATA_TYPE_ATHAN3) ||
               dataType.equals(MainActivity.DATA_TYPE_ATHAN4) ||
               dataType.equals(MainActivity.DATA_TYPE_ATHAN5);
    }
    private void showIqamaNotification(Context context, String prayerKey, int soundChoice) {
    String prayerNameAr;
    switch (prayerKey) {
        case "fajr":    prayerNameAr = "الفجر";  break;
        case "dhuhr":   prayerNameAr = "الظهر";  break;
        case "asr":     prayerNameAr = "العصر";  break;
        case "maghrib": prayerNameAr = "المغرب"; break;
        case "isha":    prayerNameAr = "العشاء"; break;
        default:        prayerNameAr = "الصلاة"; break;
    }

    int soundRes;
    switch (soundChoice) {
        case 2:  soundRes = R.raw.iqama_2; break;
        case 3:  soundRes = R.raw.iqama_3; break;
        default: soundRes = R.raw.iqama_1; break;
    }

    android.net.Uri soundUri = android.net.Uri.parse(
        "android.resource://" + context.getPackageName() + "/" + soundRes);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
if (audioManager != null) {
    audioManager.requestAudioFocus(null,
        AudioManager.STREAM_ALARM,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
}
        
    String channelId = "iqama_channel_s" + soundChoice;
    NotificationManager nm =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
            channelId, "إقامة الصلاة", NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(soundUri,
            new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
       channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(channel);
    
    }
        

    Intent stopIntent = new Intent("com.alaaeltaweel.thikrallah.STOP_SOUND");
PendingIntent pi = PendingIntent.getBroadcast(context, prayerKey.hashCode() + 2222,
        stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    Intent wakeIntent = new Intent(context, WakeUpActivity.class);
    wakeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent wakePi = PendingIntent.getActivity(context, prayerKey.hashCode() + 8888,
        wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("إقامة صلاة " + prayerNameAr)
        .setContentText("حان وقت إقامة الصلاة")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setTimeoutAfter(3 * 60 * 1000L) 
        .setFullScreenIntent(wakePi, true) 
        .setSound(soundUri)
        .setContentIntent(pi); 

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong("last_iqama_play_time", System.currentTimeMillis()).apply();
    nm.notify(("iqama_" + prayerKey).hashCode(), builder.build());
    }
}
