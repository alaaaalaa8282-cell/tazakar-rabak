Enterpackage com.alaaeltaweel.thikrallah.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.alaaeltaweel.thikrallah.PrayerTrackerActivity;
import com.alaaeltaweel.thikrallah.R;

public class PrayerReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_PRAYER_NAME = "prayer_name";
    public static final String EXTRA_PRAYER_KEY = "prayer_key";
    public static final String CHANNEL_ID = "prayer_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME);
        String prayerKey = intent.getStringExtra(EXTRA_PRAYER_KEY);

        // تحقق هل صلى المستخدم الصلاة دي النهارده
        String todayKey = new java.text.SimpleDateFormat(
            "yyyy-MM-dd", java.util.Locale.getDefault())
            .format(new java.util.Date());

        android.content.SharedPreferences prefs =
            context.getSharedPreferences("prayer_tracker",
                Context.MODE_PRIVATE);

        boolean alreadyPrayed = prefs.getBoolean(
            todayKey + "_" + prayerKey, false);

        // لو لسه مصلاش، ابعت إشعار
        if (!alreadyPrayed) {
            sendNotification(context, prayerName, prayerKey);
        }
    }

    private void sendNotification(Context context,
            String prayerName, String prayerKey) {

        NotificationManager manager = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);

        // إنشاء Channel للأندرويد 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "تذكير الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("تذكيرات للصلوات اليومية");
            manager.createNotificationChannel(channel);
        }

        // Intent يفتح متتبع الصلاة
        Intent openIntent = new Intent(context,
            PrayerTrackerActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, prayerKey.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🕌 هل صليت " + prayerName + "؟")
                .setContentText("لا تنسَ صلاة " + prayerName + " - اضغط للتسجيل")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(prayerKey.hashCode(), builder.build());
    }
              }
