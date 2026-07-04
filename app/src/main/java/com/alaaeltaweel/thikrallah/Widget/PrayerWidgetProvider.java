package com.alaaeltaweel.thikrallah.Widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.alaaeltaweel.thikrallah.R;
import com.alaaeltaweel.thikrallah.Utilities.PrayTime;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class PrayerWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "PrayerWidget";
    public static final String ACTION_UPDATE_WIDGET = "com.alaaeltaweel.thikrallah.WIDGET_UPDATE";
    private static final String WEATHER_API_KEY = "ee5c1d0597ed9dd634b05d5daeed6cc8";

    // أسماء الصلوات
    private static final String[] PRAYER_NAMES = {"الفجر", "الظهر", "العصر", "المغرب", "العشاء"};
    // positions في getPrayerTimes: 0=fajr,1=sunrise,2=dhuhr,3=asr,4=maghrib,5=isha
    private static final int[] PRAYER_POSITIONS = {0, 2, 3, 4, 5};

    // صور الأب بالتناوب
    private static final int[] FATHER_IMAGES = {
        R.drawable.father_bg,
        R.drawable.father_bg2,
        R.drawable.father_bg3,
        R.drawable.father_bg4,
        R.drawable.father_bg5,
        R.drawable.father_bg6,
        R.drawable.father_bg7
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
        scheduleNextUpdate(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE_WIDGET.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName component = new ComponentName(context, PrayerWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(component);
            for (int id : ids) {
                updateWidget(context, manager, id);
            }
            scheduleNextUpdate(context);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_prayer_layout);

        // صورة الأب بالتناوب حسب الدقيقة
        int imageIndex = (Calendar.getInstance().get(Calendar.MINUTE) / 10) % FATHER_IMAGES.length;
        views.setImageViewResource(R.id.widget_bg_image, FATHER_IMAGES[imageIndex]);

        // Intent لفتح التطبيق عند الضغط
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launchIntent != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_bg_image, pendingIntent);
        }

        // أوقات الصلاة
        try {
            PrayTime prayTime = PrayTime.instancePrayTime(context);
            prayTime.setTimeFormat(PrayTime.TIME_FORMAT_Time12);
            String[] times = prayTime.getPrayerTimes(context);

            if (times != null && times.length >= 6) {
                // عرض أوقات الصلوات الخمس
                views.setTextViewText(R.id.widget_fajr_time, formatTime(times[0]));
                views.setTextViewText(R.id.widget_dhuhr_time, formatTime(times[2]));
                views.setTextViewText(R.id.widget_asr_time, formatTime(times[3]));
                views.setTextViewText(R.id.widget_maghrib_time, formatTime(times[4]));
                views.setTextViewText(R.id.widget_isha_time, formatTime(times[5]));

                // الصلاة القادمة والعداد
                setNextPrayerInfo(context, views, times);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting prayer times", e);
            views.setTextViewText(R.id.widget_next_prayer_name, "خطأ في الأوقات");
        }

        // الطقس
        fetchWeatherAsync(context, appWidgetManager, widgetId, views);

        appWidgetManager.updateAppWidget(widgetId, views);
    }

    private void setNextPrayerInfo(Context context, RemoteViews views, String[] times) {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int currentTotalMinutes = currentHour * 60 + currentMinute;

        // أوقات الصلوات الخمس بالدقائق
        PrayTime prayTime = PrayTime.instancePrayTime(context);
        prayTime.setTimeFormat(PrayTime.TIME_FORMAT_Time24);
        String[] times24 = prayTime.getPrayerTimes(context);

        String nextPrayerName = PRAYER_NAMES[0];
        String nextPrayerTime = times[0];
        long minutesUntilNext = 0;

        // إيجاد الصلاة القادمة
        for (int i = 0; i < PRAYER_POSITIONS.length; i++) {
            int pos = PRAYER_POSITIONS[i];
            if (times24 == null || pos >= times24.length) continue;
            String t = times24[pos];
            if (t == null || t.equals("-----")) continue;
            try {
                String[] parts = t.split(":");
                int prayerTotal = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
                if (prayerTotal > currentTotalMinutes) {
                    nextPrayerName = PRAYER_NAMES[i];
                    nextPrayerTime = times[pos];
                    minutesUntilNext = prayerTotal - currentTotalMinutes;
                    break;
                }
                // لو كل الصلوات فاتت، الصلاة القادمة هي فجر الغد
                nextPrayerName = PRAYER_NAMES[0];
                nextPrayerTime = times[0];
                String[] fajrParts = times24[0].split(":");
                int fajrTotal = Integer.parseInt(fajrParts[0]) * 60 + Integer.parseInt(fajrParts[1]);
                minutesUntilNext = (24 * 60 - currentTotalMinutes) + fajrTotal;
            } catch (Exception ignored) {}
        }

        views.setTextViewText(R.id.widget_next_prayer_name, nextPrayerName);
        views.setTextViewText(R.id.widget_next_prayer_time, nextPrayerTime);

        // تنسيق العداد
        long hours = minutesUntilNext / 60;
        long mins = minutesUntilNext % 60;
        String countdown = String.format("⏱ %d:%02d ساعة", hours, mins);
        views.setTextViewText(R.id.widget_countdown, countdown);
    }

    private String formatTime(String time) {
        if (time == null || time.equals("-----")) return "--:--";
        return time;
    }

    private void fetchWeatherAsync(Context context, AppWidgetManager appWidgetManager,
                                    int widgetId, RemoteViews views) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lat = prefs.getString("latitude", "");
        String lon = prefs.getString("longitude", "");

        // لو في إحداثيات مخصصة
        if (prefs.getBoolean("isCustomLocation", false)) {
            lat = prefs.getString("c_latitude", lat);
            lon = prefs.getString("c_longitude", lon);
        }

        if (lat.isEmpty() || lon.isEmpty()) {
            views.setTextViewText(R.id.widget_weather_text, "الموقع غير متاح");
            return;
        }

        final String finalLat = lat;
        final String finalLon = lon;

        new AsyncTask<Void, Void, String[]>() {
            @Override
            protected String[] doInBackground(Void... voids) {
                try {
                    String urlStr = "https://api.openweathermap.org/data/2.5/weather?lat=" + finalLat
                            + "&lon=" + finalLon
                            + "&appid=" + WEATHER_API_KEY
                            + "&units=metric&lang=ar";
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    double temp = json.getJSONObject("main").getDouble("temp");
                    String description = json.getJSONArray("weather")
                            .getJSONObject(0).getString("description");
                    String iconCode = json.getJSONArray("weather")
                            .getJSONObject(0).getString("icon");

                    return new String[]{
                            String.format("%.0f°م | %s", temp, description),
                            getWeatherEmoji(iconCode)
                    };
                } catch (Exception e) {
                    Log.e(TAG, "Weather fetch error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String[] result) {
                if (result != null) {
                    views.setTextViewText(R.id.widget_weather_text, result[0]);
                    views.setTextViewText(R.id.widget_weather_icon, result[1]);
                } else {
                    views.setTextViewText(R.id.widget_weather_text, "الطقس غير متاح");
                }
                appWidgetManager.updateAppWidget(widgetId, views);
            }
        }.execute();
    }

    private String getWeatherEmoji(String iconCode) {
        if (iconCode == null) return "🌤";
        switch (iconCode) {
            case "01d": return "☀️";
            case "01n": return "🌙";
            case "02d": case "02n": return "⛅";
            case "03d": case "03n": return "🌥";
            case "04d": case "04n": return "☁️";
            case "09d": case "09n": return "🌧";
            case "10d": return "🌦";
            case "10n": return "🌧";
            case "11d": case "11n": return "⛈";
            case "13d": case "13n": return "❄️";
            case "50d": case "50n": return "🌫";
            default: return "🌤";
        }
    }

    private void scheduleNextUpdate(Context context) {
        Intent intent = new Intent(context, PrayerWidgetProvider.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // تحديث كل دقيقة
            long nextMinute = SystemClock.elapsedRealtime() + 60 * 1000;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextMinute, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextMinute, pendingIntent);
            }
        }
    }
}
