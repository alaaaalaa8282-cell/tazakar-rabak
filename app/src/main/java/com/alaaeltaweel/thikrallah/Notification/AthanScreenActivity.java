package com.alaaeltaweel.thikrallah.Notification;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alaaeltaweel.thikrallah.MainActivity;
import com.alaaeltaweel.thikrallah.R;
import com.alaaeltaweel.thikrallah.ThikrMediaPlayerService;

public class AthanScreenActivity extends AppCompatActivity {

    private static final int AUTO_DISMISS_DELAY = 10 * 60 * 1000;
    private Handler autoHandler = new Handler();
    private String dataType;

    private BroadcastReceiver athanCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopAthanAndClose();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }

        setContentView(R.layout.activity_athan_screen);

        dataType = getIntent().getStringExtra("com.alaaeltaweel.thikrallah.datatype");

        TextView prayerNameText = findViewById(R.id.prayer_name_text);
        TextView athanText = findViewById(R.id.athan_text);
        Button stopButton = findViewById(R.id.stop_athan_button);

        String prayerName = getPrayerName(dataType);
        prayerNameText.setText(prayerName);
        athanText.setText("حان وقت صلاة " + prayerName);

        stopButton.setOnClickListener(v -> stopAthanAndClose());

        playAthan();

        autoHandler.postDelayed(this::stopAthanAndClose, AUTO_DISMISS_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(athanCompleteReceiver,
                new IntentFilter("com.alaaeltaweel.thikrallah.ATHAN_COMPLETE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(athanCompleteReceiver);
        } catch (IllegalArgumentException e) {
            // في حالة مش مسجل أصلاً
        }
    }

    private String getPrayerName(String dataType) {
        if (dataType == null) return "الصلاة";
        switch (dataType) {
            case MainActivity.DATA_TYPE_ATHAN1: return "الفجر";
            case MainActivity.DATA_TYPE_ATHAN2: return "الظهر";
            case MainActivity.DATA_TYPE_ATHAN3: return "العصر";
            case MainActivity.DATA_TYPE_ATHAN4: return "المغرب";
            case MainActivity.DATA_TYPE_ATHAN5: return "العشاء";
            default: return "الصلاة";
        }
    }

    private void playAthan() {
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
        data.putString("com.alaaeltaweel.thikrallah.datatype", dataType);
        data.putBoolean("isUserAction", false);

        Intent intent = new Intent(this, ThikrService.class).putExtras(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopAthanAndClose() {
        // وقف الصوت مباشرة عبر ThikrMediaPlayerService
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_STOP);
        data.putString("com.alaaeltaweel.thikrallah.datatype", dataType);
        Intent stopMedia = new Intent(this, ThikrMediaPlayerService.class).putExtras(data);
        startService(stopMedia);

        // وقف ThikrService كمان
        Intent stopThikr = new Intent(this, ThikrService.class);
        stopService(stopThikr);

        autoHandler.removeCallbacksAndMessages(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        autoHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
