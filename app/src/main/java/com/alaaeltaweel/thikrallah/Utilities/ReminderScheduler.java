package com.alaaeltaweel.thikrallah.Utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.alaaeltaweel.thikrallah.Notification.AthkarReminderReceiver;
import com.alaaeltaweel.thikrallah.Notification.PrayerReminderReceiver;
import java.util.Calendar;

public class ReminderScheduler {

    public static void scheduleAllReminders(Context context) {
        schedulePrayerReminders(context);
        scheduleAthkarReminders(context);
    }

    private static void schedulePrayerReminders(Context context) {
        // الفجر - 5:00 ص
        schedulePrayer(context, "fajr", "الفجر", 5, 0);
        // الظهر - 1:00 م
        schedulePrayer(context, "dhuhr", "الظهر", 13, 0);
        // العصر - 4:30 م
        schedulePrayer(context, "asr", "العصر", 16, 30);
        // المغرب - 7:00 م
        schedulePrayer(context, "maghrib", "المغرب", 19, 0);
        // العشاء - 9:00 م
        schedulePrayer(context, "isha", "العشاء", 21, 0);
    }

    private static void schedulePrayer(Context context,
            String key, String name, int hour, int minute) {

        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context,
            PrayerReminderReceiver.class);
        intent.putExtra(PrayerReminderReceiver.EXTRA_PRAYER_KEY, key);
        intent.putExtra(PrayerReminderReceiver.EXTRA_PRAYER_NAME, name);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, key.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // لو الوقت عدى، ابدأ من بكره
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent);
    }

    private static void scheduleAthkarReminders(Context context) {
        // أذكار الصباح - 7:00 ص
        scheduleAthkar(context, "morning", 7, 0);
        // أذكار المساء - 5:00 م
        scheduleAthkar(context, "evening", 17, 0);
    }

    private static void scheduleAthkar(Context context,
            String type, int hour, int minute) {

        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context,
            AthkarReminderReceiver.class);
        intent.putExtra(AthkarReminderReceiver.EXTRA_ATHKAR_TYPE, type);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, type.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent);
    }
                                       }
