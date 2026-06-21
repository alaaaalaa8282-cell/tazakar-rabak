package com.alaaeltaweel.thikrallah.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class BatteryStateReceiver extends BroadcastReceiver {

    private static final String TAG = "BatteryStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        Log.d(TAG, "Received: " + action);

        // ✅ شبكة أمان: نتأكد إن الـ alarms لسه مجدولة صح
        // كل ما حالة البطارية أو الشحن تتغير بشكل ملحوظ
        // (ده بيغطي حالة الخروج من وضع توفير الطاقة الفائق
        // اللي مفيش له إشارة رسمية مباشرة في أندرويد)
        switch (action) {
            case Intent.ACTION_POWER_CONNECTED:
            case Intent.ACTION_POWER_DISCONNECTED:
            case PowerManager.ACTION_POWER_SAVE_MODE_CHANGED:
                Log.d(TAG, "Re-checking alarms after power state change");
                new MyAlarmsManager(context.getApplicationContext()).UpdateAllApplicableAlarms();
                break;
        }
    }
}
