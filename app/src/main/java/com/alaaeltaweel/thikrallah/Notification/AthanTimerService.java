package com.alaaeltaweel.thikrallah.Notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.alaaeltaweel.thikrallah.MainActivity;
import com.alaaeltaweel.thikrallah.R;
import com.alaaeltaweel.thikrallah.Utilities.PrayTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;


public class AthanTimerService extends Service {
	NotificationCompat.Builder notificationBuilder;
    String TAG = "AthanTimerService";
    private final static int NOTIFICATION_ID = 54;
	private Context mContext;
	boolean isStarted = false;
	private Timer timer;

    public static final int JOB_ID = 0x01;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        boolean isTimer = sharedPrefs.getBoolean("foreground_athan_timer", true);
		Timber.tag(TAG).d("istimer is " + isTimer);
		initNotification();
		if (isTimer) {
			if (!isStarted) {
				Timber.tag(TAG).d(TAG + "started");
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
				timer = new Timer();
                isStarted = true;
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        initNotification();
					}
				}, 0, 1000);
			}
			scheduleNextAthan();
		} else {
			this.stopSelf();
		}
		return START_STICKY;
	}

	SharedPreferences sharedPrefs;

	@Override
	public void onDestroy() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		isStarted = false;
		super.onDestroy();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
	}

	private void initNotification() {
		Timber.tag(TAG).d("initiNotification started");

		Intent resultIntent = new Intent(mContext, MainActivity.class);
		resultIntent.putExtra("FromNotification", true);
		resultIntent.putExtra("DataType", MainActivity.DATA_TYPE_ATHAN);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(mContext,
				0, resultIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String NOTIFICATION_CHANNEL_ID = "com.alaaeltaweel.thikrallah.Notification.AthanTimerService";
			String channelName = this.getResources().getString(R.string.athan_timer_notifiaction);
			NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
			chan.setSound(null, null);
			chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			assert manager != null;
			manager.createNotificationChannel(chan);
			notificationBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
		} else {
			notificationBuilder = new NotificationCompat.Builder(mContext);
		}

		notificationBuilder
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setContentTitle(getString(R.string.my_app_name))
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setContentText(getNextPrayer())
				.setContentIntent(launchAppPendingIntent);

		notificationBuilder = setVisibilityPublic(notificationBuilder);
		Timber.tag(TAG).d("started forground");
		Timber.tag(TAG).d("context is " + mContext);

		if (mContext != null) {
			startForeground(NOTIFICATION_ID, notificationBuilder.build());
		}
	}

	private NotificationCompat.Builder setVisibilityPublic(NotificationCompat.Builder inotificationBuilder) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			inotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}
		return inotificationBuilder;
	}

	String getNextPrayer() {
		PrayTime prayers = PrayTime.instancePrayTime(this);
		prayers.setTimeFormat(PrayTime.TIME_FORMAT_Time24);
		String[] prayerTimes = prayers.getPrayerTimes(this);
		ArrayList<String> prayerTimesList = new ArrayList<>();
		prayerTimesList.add(prayerTimes[0]);
		prayerTimesList.add(prayerTimes[1]);
		prayerTimesList.add(prayerTimes[2]);
		prayerTimesList.add(prayerTimes[3]);
		prayerTimesList.add(prayerTimes[5]);
		prayerTimesList.add(prayerTimes[6]);

		Date dat = new Date();
		Calendar now = Calendar.getInstance();
		now.setTime(dat);

		long min = Long.MAX_VALUE;
		int nextPrayer = 12;
		String prayerName = "NA";
		long[] PrayerTimesInMilliseconds = new long[6];

		for (int i = 0; i < prayerTimesList.size(); i++) {
			if (prayerTimesList.get(i).equalsIgnoreCase(prayers.getInvalidTime())) {
				return null;
			} else {
				Calendar prayer_time = Calendar.getInstance();
				prayer_time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(prayerTimesList.get(i).split(":", 3)[0]));
				prayer_time.set(Calendar.MINUTE, Integer.parseInt(prayerTimesList.get(i).split(":", 3)[1]));
				prayer_time.set(Calendar.SECOND, 0);
				if (prayer_time.before(now)) {
					prayer_time.add(Calendar.HOUR, 24);
				}
				PrayerTimesInMilliseconds[i] = prayer_time.getTimeInMillis() - now.getTimeInMillis();
				if (PrayerTimesInMilliseconds[i] < min) {
					min = PrayerTimesInMilliseconds[i];
					nextPrayer = i;
				}
			}
		}

		switch (nextPrayer) {
			case 0:
				prayerName = getResources().getString(R.string.prayer1);
				break;
			case 1:
				prayerName = getResources().getString(R.string.sunrise);
				break;
			case 2:
				prayerName = getResources().getString(R.string.prayer2);
				break;
			case 3:
				prayerName = getResources().getString(R.string.prayer3);
				break;
			case 4:
				prayerName = getResources().getString(R.string.prayer4);
				break;
			case 5:
				prayerName = getResources().getString(R.string.prayer5);
				break;
			default:
				prayerName = "NA";
		}

		min = min / 1000;
		int hours = (int) Math.floor(((double) min) / 3600);
		long minutes = (min - hours * 3600) / 60 + 1;

		String hoursText = "";
		String minutesText = "";

		if (hours == 1) {
			hoursText = hours + " " + getResources().getString(R.string.hour) + " ";
		} else if (hours > 1) {
			hoursText = hours + " " + getResources().getString(R.string.hours) + " ";
		} else {
			hoursText = "";
		}

		minutesText = minutes + " " + getResources().getString(R.string.minute);

		long seconds = (min % 60) / 1 + 1;
		String secondsText = seconds + " " + getResources().getString(R.string.second);
		return hoursText + " " + minutesText + " " + secondsText + " " + getResources().getString(R.string.until) + " " + prayerName;
	}

	private void scheduleNextAthan() {
		PrayTime prayers = PrayTime.instancePrayTime(this);
		prayers.setTimeFormat(PrayTime.TIME_FORMAT_Time24);
		String[] prayerTimes = prayers.getPrayerTimes(this);

		String[] dataTypes = {
			MainActivity.DATA_TYPE_ATHAN1, // فجر
			null,                          // شروق
			MainActivity.DATA_TYPE_ATHAN2, // ظهر
			MainActivity.DATA_TYPE_ATHAN3, // عصر
			MainActivity.DATA_TYPE_ATHAN4, // مغرب
			MainActivity.DATA_TYPE_ATHAN5  // عشاء
		};

		int[] timeIndexes = {0, 1, 2, 3, 5, 6};

		Calendar now = Calendar.getInstance();
		long minDiff = Long.MAX_VALUE;
		String nextDataType = null;
		long nextAthanTime = 0;

		for (int i = 0; i < timeIndexes.length; i++) {
			if (dataTypes[i] == null) continue;
			String timeStr = prayerTimes[timeIndexes[i]];
			if (timeStr == null || timeStr.equalsIgnoreCase(prayers.getInvalidTime())) continue;

			try {
				String[] parts = timeStr.split(":");
				Calendar prayerCal = Calendar.getInstance();
				prayerCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
				prayerCal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
				prayerCal.set(Calendar.SECOND, 0);
				prayerCal.set(Calendar.MILLISECOND, 0);

				if (prayerCal.before(now)) {
					prayerCal.add(Calendar.DAY_OF_MONTH, 1);
				}

				long diff = prayerCal.getTimeInMillis() - now.getTimeInMillis();
				if (diff < minDiff) {
					minDiff = diff;
					nextDataType = dataTypes[i];
					nextAthanTime = prayerCal.getTimeInMillis();
				}
			} catch (Exception e) {
				Log.e(TAG, "Error parsing prayer time: " + timeStr);
			}
		}

		if (nextDataType == null) return;

		boolean isCallInProgress = false;
		try {
			android.telephony.TelephonyManager tm =
				(android.telephony.TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			if (tm != null) {
				isCallInProgress = tm.getCallState() != android.telephony.TelephonyManager.CALL_STATE_IDLE;
			}
		} catch (Exception ignored) {}

		// ✅ بنروح لـ AthanAlarmReceiver مش للـ Activity مباشرة
		Intent athanIntent = new Intent(this, AthanAlarmReceiver.class);
		athanIntent.putExtra("com.alaaeltaweel.thikrallah.datatype", nextDataType);
		athanIntent.putExtra("isCallInProgress", isCallInProgress);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
			this,
			nextDataType.hashCode(),
			athanIntent,
			PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		if (alarmManager != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				alarmManager.setExactAndAllowWhileIdle(
					AlarmManager.RTC_WAKEUP,
					nextAthanTime,
					pendingIntent
				);
			} else {
				alarmManager.setExact(
					AlarmManager.RTC_WAKEUP,
					nextAthanTime,
					pendingIntent
				);
			}
			Log.d(TAG, "Athan alarm set for: " + nextDataType + " at " + new Date(nextAthanTime));
		}
	}
}
