
package com.alaaeltaweel.thikrallah.Notification;

import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.alaaeltaweel.thikrallah.MainActivity;
import com.alaaeltaweel.thikrallah.R;
import com.alaaeltaweel.thikrallah.Utilities.PrayTime;

import java.util.Calendar;
import java.util.Date;

public class MyAlarmsManager {
    String TAG = "MyAlarmsManager";
    public static final int requestCodeMorningAlarm = 8;
    public static final int requestCodeMulkAlarm = 26;
    public static final int requestCodeNightAlarm = 20;
    public static final int requestCodeRandomAlarm = 1;
    public static final int requestCodeKahfAlarm = 25;
    public static final int requestCodeAthan1 = 100;
    public static final int requestCodeAthan2 = 101;
    public static final int requestCodeAthan3 = 102;
    public static final int requestCodeAthan4 = 103;
    public static final int requestCodeAthan5 = 104;

    // ✅ request codes للتنبيه قبل الصلاة بـ 15 دقيقة
    public static final int requestCodePreAthan1 = 200;
    public static final int requestCodePreAthan2 = 201;
    public static final int requestCodePreAthan3 = 202;
    public static final int requestCodePreAthan4 = 203;
    public static final int requestCodePreAthan5 = 204;

    // ✅ رمضان
    public static final int requestCodeCannon = 300;
    public static final int requestCodeMesaharaty = 301;

    // ✅ الـ datatype للتنبيه قبل الصلاة
    public static final String DATA_TYPE_PRE_ATHAN = "pre_athan";

    boolean isPermissionRequested = false;
    AlarmManager alarmMgr;
    Context context;
    private SharedPreferences sharedPrefs;

    public MyAlarmsManager(Context icontext) {
        context = icontext;
    }

    public void UpdateAllApplicableAlarms() {
        if (context == null) {
            return;
        }
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        Long diff = timestamp - sharedPrefs.getLong("lastAlarmsUpdate", 0);
        if (diff < 3000) {
            Log.d(TAG, "last AlarmsUpdate less than 3 seconds: " + diff);
            return;
        }
        sharedPrefs.edit().putLong("lastAlarmsUpdate", timestamp).commit();
        alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Log.d("MyAlarmsManager", "UpdateAllApplicableAlarms called");
        setPeriodicAlarmManagerUpdates(alarmMgr);
        
        String[] MorningReminderTime = sharedPrefs.getString("daytReminderTime", "08:00").split(":", 3);
        String[] NightReminderTime = sharedPrefs.getString("nightReminderTime", "20:00").split(":", 3);
        String[] kahfReminderTime = sharedPrefs.getString("kahfReminderTime", "10:00").split(":", 3);
        String[] mulkReminderTime = sharedPrefs.getString("mulkReminderTime", "22:00").split(":", 3);
        String RandomReminderInterval = sharedPrefs.getString("RemindMeEvery", "60");
        
        boolean remindMeMorningThikr = sharedPrefs.getBoolean("remindMeMorningThikr", true);
        boolean remindMeNightThikr = sharedPrefs.getBoolean("remindMeNightThikr", true);
        boolean RemindmeThroughTheDay = sharedPrefs.getBoolean("RemindmeThroughTheDay", true);
        boolean Remindmekahf = sharedPrefs.getBoolean("remindMekahf", true);
        boolean Remindmemulk = sharedPrefs.getBoolean("remindMemulk", true);

        // ✅ 1. منبه سورة الملك (Intent منفصل ومستقل)
        Intent intentMulk = new Intent(context, ThikrAlarmReceiver.class);
        intentMulk.putExtra("com.alaaeltaweel.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_MULK);
        PendingIntent pendingIntentMulk = PendingIntent.getBroadcast(context, requestCodeMulkAlarm, intentMulk, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Remindmemulk) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(mulkReminderTime[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(mulkReminderTime[1]));
            calendar.set(Calendar.SECOND, 0);
            setAlarm(calendar, pendingIntentMulk);
        } else {
            alarmMgr.cancel(pendingIntentMulk);
        }

        // ✅ 2. أذكار الصباح (Intent منفصل ومستقل)
        Intent intentMorning = new Intent(context, ThikrAlarmReceiver.class);
        intentMorning.putExtra("com.alaaeltaweel.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR);
        PendingIntent pendingIntentMorningThikr = PendingIntent.getBroadcast(context, requestCodeMorningAlarm, intentMorning, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (remindMeMorningThikr) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(MorningReminderTime[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(MorningReminderTime[1]));
            calendar.set(Calendar.SECOND, 0);
            setAlarm(calendar, pendingIntentMorningThikr);
        } else {
            alarmMgr.cancel(pendingIntentMorningThikr);
        }

        // ✅ 3. أذكار المساء (Intent منفصل ومستقل)
        Intent intentNight = new Intent(context, ThikrAlarmReceiver.class);
        intentNight.putExtra("com.alaaeltaweel.thikrallah.datatype", MainActivity.DATA_TYPE_NIGHT_THIKR);
        PendingIntent pendingIntentNightThikr = PendingIntent.getBroadcast(context, requestCodeNightAlarm, intentNight, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (remindMeNightThikr) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(NightReminderTime[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(NightReminderTime[1]));
            calendar.set(Calendar.SECOND, 0);
            setAlarm(calendar, pendingIntentNightThikr);
        } else {
            alarmMgr.cancel(pendingIntentNightThikr);
        }

        // ✅ 4. التذكير التلقائي/العشوائي خلال اليوم (Intent منفصل ومستقل)
        Intent intentGeneral = new Intent(context, ThikrAlarmReceiver.class);
        intentGeneral.putExtra("com.alaaeltaweel.thikrallah.datatype", MainActivity.DATA_TYPE_GENERAL_THIKR);
        PendingIntent pendingIntentGeneral = PendingIntent.getBroadcast(context, requestCodeRandomAlarm, intentGeneral, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (RemindmeThroughTheDay) {
            alarmMgr.cancel(pendingIntentGeneral);
            Calendar calendar = Calendar.getInstance();
            long lastFired = sharedPrefs.getLong("last_general_thikr_time", System.currentTimeMillis());
            calendar.setTimeInMillis(lastFired + (Integer.parseInt(RandomReminderInterval) * 60 * 1000L));
            setAlarm(calendar, pendingIntentGeneral);
        } else {
            alarmMgr.cancel(pendingIntentGeneral);
        }

        // ✅ 5. سورة الكهف يوم الجمعة (Intent منفصل ومستقل)
        Intent intentKahf = new Intent(context, ThikrAlarmReceiver.class);
        intentKahf.putExtra("com.alaaeltaweel.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_KAHF);
        PendingIntent pendingIntentKahf = PendingIntent.getBroadcast(context, requestCodeKahfAlarm, intentKahf, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Remindmekahf && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) != sharedPrefs.getInt("lastKahfPlayed", -1)) {
            alarmMgr.cancel(pendingIntentKahf);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(kahfReminderTime[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(kahfReminderTime[1]));
            calendar.set(Calendar.SECOND, 0);
            setAlarm(calendar, pendingIntentKahf);
        } else {
            alarmMgr.cancel(pendingIntentKahf);
        }

        updateAllPrayerAlarms();

        // ✅ جدولة المدفع والمسحراتي في رمضان
        updateRamadanAlarms();
    }

    // ===================== ✅ رمضان: مدفع ومسحراتي =====================
    private void updateRamadanAlarms() {
        if (context == null || alarmMgr == null) return;

        android.icu.util.IslamicCalendar islamicCalendar = new android.icu.util.IslamicCalendar();
        int hijriMonth = islamicCalendar.get(android.icu.util.Calendar.MONTH);
        boolean isRamadan = (hijriMonth == 8);

        Intent cannonIntent = new Intent(context, RamadanAlarmReceiver.class);
        cannonIntent.setAction(RamadanSoundService.ACTION_CANNON);
        PendingIntent pendingCannon = PendingIntent.getBroadcast(context, requestCodeCannon,
                cannonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent mesaharatyIntent = new Intent(context, RamadanAlarmReceiver.class);
        mesaharatyIntent.setAction(RamadanSoundService.ACTION_MESAHARATY);
        PendingIntent pendingMesaharaty = PendingIntent.getBroadcast(context, requestCodeMesaharaty,
                mesaharatyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmMgr.cancel(pendingCannon);
        alarmMgr.cancel(pendingMesaharaty);

        if (!isRamadan) {
            Log.d(TAG, "Not Ramadan, Ramadan alarms cancelled");
            return;
        }

        boolean cannonEnabled = sharedPrefs.getBoolean("ramadan_cannon_enabled", true);
        boolean mesaharatyEnabled = sharedPrefs.getBoolean("ramadan_mesaharaty_enabled", true);

        // ✅ المدفع — وقت المغرب
        if (cannonEnabled) {
            PrayTime prayers = PrayTime.instancePrayTime(context);
            String[] times = prayers.getPrayerTimes(context);
            if (times != null && times.length >= 5) {
                try {
                    String[] maghribTime = times[4].split(":", 3);
                    Calendar cannonCal = Calendar.getInstance();
                    cannonCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(maghribTime[0]));
                    cannonCal.set(Calendar.MINUTE, Integer.parseInt(maghribTime[1]));
                    cannonCal.set(Calendar.SECOND, 0);
                    setAlarm(cannonCal, pendingCannon);
                    Log.d(TAG, "Cannon alarm scheduled: " + cannonCal.getTime());
                } catch (Exception e) {
                    Log.e(TAG, "Error setting cannon alarm: " + e.getMessage());
                }
            }
        }

        // ✅ المسحراتي
        if (mesaharatyEnabled) {
            String mesaharatyTimeStr = sharedPrefs.getString("mesaharaty_time", "03:00");
            try {
                String[] mesaharatyTimeParts = mesaharatyTimeStr.split(":", 3);
                Calendar mesaharatyCal = Calendar.getInstance();
                mesaharatyCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(mesaharatyTimeParts[0]));
                mesaharatyCal.set(Calendar.MINUTE, Integer.parseInt(mesaharatyTimeParts[1]));
                mesaharatyCal.set(Calendar.SECOND, 0);
                setAlarm(mesaharatyCal, pendingMesaharaty);
                Log.d(TAG, "Mesaharaty alarm scheduled: " + mesaharatyCal.getTime());
            } catch (Exception e) {
                Log.e(TAG, "Error setting mesaharaty alarm: " + e.getMessage());
            }
        }
    }

    @SuppressLint("NewApi")
    private void setAlarm(Calendar time, PendingIntent pendingIntent) {
        if (context == null || alarmMgr == null) return;

        // ✅ معالجة الوقت لضمان جدولة صحيحة في المستقبل دائماً (حل مشكلة التكرار المزدوج)
        long timeInMilliseconds = time.getTimeInMillis();
        if (timeInMilliseconds <= System.currentTimeMillis()) {
            timeInMilliseconds += 24 * 60 * 60 * 1000L; // إضافة 24 ساعة لليوم التالي
        }

        Log.d(TAG, "Setting Alarm to: " + new Date(timeInMilliseconds).toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // أندرويد 12 فما فوق
            if (alarmMgr.canScheduleExactAlarms()) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);
            } else {
                // ⚠️ حل حرج: إذا لم تتوفر الصلاحية الدقيقة، جدول المنبه كعادي بدلاً من تجميده تماماً!
                alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);
                requestExactAlarmPermission(); 
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // أندرويد 6 إلى 11
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // أندرويد 4.4 إلى 5
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);
        }
    }

    private boolean requestExactAlarmPermission() {
        Log.d(TAG, "requestExactAlarmPermission");
        if (!(context instanceof Activity)) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }

        AlarmManager alarmManager = (AlarmManager) this.context.getSystemService(ALARM_SERVICE);
        if (alarmManager.canScheduleExactAlarms()) {
            return true;
        }

        if (isPermissionRequested) {
            return false;
        }

        isPermissionRequested = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle(this.context.getResources().getString(R.string.exact_alarm_title))
                .setMessage(this.context.getResources().getString(R.string.exact_alarm_message))
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    }
                })
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, null)
                .create().show();
        
        return false;
    }

    void setPeriodicAlarmManagerUpdates(AlarmManager alarmmnager) {
        if (context == null) {
            return;
        }
        Intent launchIntent = new Intent(context, ThikrBootReceiver.class);
        launchIntent.setAction("com.alaaeltaweel.thikrallah.Notification.ThikrBootReceiver.android.action.broadcast");
        
        PendingIntent intent = PendingIntent.getBroadcast(context, 100, launchIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, 1);
        calendar1.set(Calendar.MINUTE, 15);
        calendar1.set(Calendar.SECOND, 0);

        long triggerTime = calendar1.getTimeInMillis();
        if (triggerTime <= System.currentTimeMillis()) {
            triggerTime += 24 * 60 * 60 * 1000L;
        }

        alarmmnager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, 12 * 60 * 60 * 1000, intent);
    }

    private void updateAllPrayerAlarms() {
        if (context == null) {
            return;
        }
        double latitude = Double.parseDouble(MainActivity.getLatitude(context));
        double longitude = Double.parseDouble(MainActivity.getLongitude(context));
        if (latitude == 0 && longitude == 0) {
            return;
        }
        updatePrayerAlarms(requestCodeAthan1, requestCodePreAthan1, "isFajrReminder", 0, MainActivity.DATA_TYPE_ATHAN1, "الفجر");
        updatePrayerAlarms(requestCodeAthan2, requestCodePreAthan2, "isDuhrReminder", 2, MainActivity.DATA_TYPE_ATHAN2, "الظهر");
        updatePrayerAlarms(requestCodeAthan3, requestCodePreAthan3, "isAsrReminder", 3, MainActivity.DATA_TYPE_ATHAN3, "العصر");
        updatePrayerAlarms(requestCodeAthan4, requestCodePreAthan4, "isMaghribReminder", 5, MainActivity.DATA_TYPE_ATHAN4, "المغرب");
        updatePrayerAlarms(requestCodeAthan5, requestCodePreAthan5, "isIshaaReminder", 6, MainActivity.DATA_TYPE_ATHAN5, "العشاء");
    }

    private void updatePrayerAlarms(int requestCode, int preRequestCode, String isReminderPreference, int prayerPosition, String datatype, String prayerName) {
        if (context == null) {
            return;
        }
        PrayTime prayers = PrayTime.instancePrayTime(context);
        prayers.setTimeFormat(PrayTime.TIME_FORMAT_Time24);
        String[] prayerTimes = prayers.getPrayerTimes(context);

        if (prayerTimes[prayerPosition].equalsIgnoreCase(prayers.getInvalidTime())) {
            return;
        }
        boolean isAthanReminder = sharedPrefs.getBoolean(isReminderPreference, true);
        boolean isPreAthanReminder = sharedPrefs.getBoolean("isPreAthanReminder", true);

        // ✅ الأذان (كل صلاة بـ Intent مستقل نظيف لعدم خلط البيانات)
        Intent athanIntent = new Intent(context, ThikrAlarmReceiver.class);
        athanIntent.putExtra("com.alaaeltaweel.thikrallah.datatype", datatype);
        
        PendingIntent pendingIntentAthan = PendingIntent.getBroadcast(context, requestCode, athanIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmMgr.cancel(pendingIntentAthan);
        
        if (isAthanReminder) {
            Calendar calendar0 = Calendar.getInstance();
            calendar0.set(Calendar.HOUR_OF_DAY, Integer.parseInt(prayerTimes[prayerPosition].split(":", 3)[0]));
            calendar0.set(Calendar.MINUTE, Integer.parseInt(prayerTimes[prayerPosition].split(":", 3)[1]));
            calendar0.set(Calendar.SECOND, 0);
            setAlarm(calendar0, pendingIntentAthan);
        }

        // ✅ تنبيه قبل الصلاة بـ 15 دقيقة (Intent مستقل)
        Intent preAthanIntent = new Intent(context, ThikrAlarmReceiver.class);
        preAthanIntent.putExtra("com.alaaeltaweel.thikrallah.datatype", DATA_TYPE_PRE_ATHAN);
        preAthanIntent.putExtra("prayer_name", prayerName);

        PendingIntent pendingIntentPreAthan = PendingIntent.getBroadcast(context, preRequestCode, preAthanIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmMgr.cancel(pendingIntentPreAthan);

        if (isAthanReminder && isPreAthanReminder) {
            Calendar calendarPre = Calendar.getInstance();
            calendarPre.set(Calendar.HOUR_OF_DAY, Integer.parseInt(prayerTimes[prayerPosition].split(":", 3)[0]));
            calendarPre.set(Calendar.MINUTE, Integer.parseInt(prayerTimes[prayerPosition].split(":", 3)[1]));
            calendarPre.set(Calendar.SECOND, 0);
            calendarPre.add(Calendar.MINUTE, -15); 

            setAlarm(calendarPre, pendingIntentPreAthan);
        }
    }
}



