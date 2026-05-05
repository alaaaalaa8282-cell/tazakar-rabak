package com.alaaeltaweel.thikrallah.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.IslamicCalendar;
import android.icu.util.ULocale;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import net.time4j.*;
import net.time4j.calendar.HijriCalendar;
import net.time4j.format.expert.ChronoFormatter;
import net.time4j.format.expert.PatternType;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.alaaeltaweel.thikrallah.MainActivity;
import com.alaaeltaweel.thikrallah.Models.Prayer;
import com.alaaeltaweel.thikrallah.Notification.MyAlarmsManager;
import com.alaaeltaweel.thikrallah.R;
import com.alaaeltaweel.thikrallah.Utilities.CustomLocation;
import com.alaaeltaweel.thikrallah.Utilities.MainInterface;
import com.alaaeltaweel.thikrallah.Utilities.PrayTime;

import java.util.Calendar;


public class AthanFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener, DialogInterface.OnDismissListener {

    private Prayer[] prayers;

    private MainInterface mCallback;
    private TextView prayer1_time;
    private TextView HijriDate;
    private TextView prayer2_time;
    private TextView prayer3_time;
    private TextView prayer4_time;
    private TextView prayer5_time;
    private TextView sunrise_time;
    private SwitchCompat fajr_switch;
    private SwitchCompat duhr_switch;
    private SwitchCompat asr_switch;
    private SwitchCompat maghrib_switch;
    private SwitchCompat ishaa_switch;
    private SharedPreferences mPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private CheckBox is_Manual_Location;
    private TextView currentLocation;

    // ── العداد التنازلي ──
    private TextView countdownTimerView;
    private CountDownTimer countdownTimer;

    // ── الخط الديجيتال ──
    private Typeface digitalFont;

    public AthanFragment() {
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("latitude") || key.equalsIgnoreCase("longitude")
                || key.equalsIgnoreCase("isCustomLocation") || key.equalsIgnoreCase("c_latitude")
                || key.equalsIgnoreCase("c_longitude") || key.equalsIgnoreCase("city")) {
            if (this.getView() != null) {
                updateprayerTimes();
                boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
                is_Manual_Location.setChecked(isLocationManual);
                currentLocation.setText(MainActivity.getCityCountryLocation(this.getContext()));
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity.setLocale(context);
        try {
            prefListener = this;
            mCallback = (MainInterface) context;
            mCallback.requestLocationUpdate();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MainInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity.setLocale(this.getContext());

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());

        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.athan_fragment, container, false);

        // ── تحميل الخط الديجيتال ──
        try {
            digitalFont = Typeface.createFromAsset(
                    getActivity().getAssets(),
                    "fonts_2/DSEG7Classic-Regular.ttf"
            );
        } catch (Exception e) {
            Log.e("AthanFragment", "Failed to load digital font: " + e.getMessage());
            digitalFont = null;
        }

        HijriDate = view.findViewById(R.id.Hijri_date);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IslamicCalendar islamic_cal = (IslamicCalendar) IslamicCalendar
                    .getInstance(new ULocale("ar_SA@calendar=islamic"));
            islamic_cal.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC);
            HijriDate.setText(getHijriDate());
        }

        countdownTimerView = view.findViewById(R.id.countdown_timer);

        prayer1_time = view.findViewById(R.id.athan_timing1);
        prayer2_time = view.findViewById(R.id.athan_timing2);
        prayer3_time = view.findViewById(R.id.athan_timing3);
        prayer4_time = view.findViewById(R.id.athan_timing4);
        prayer5_time = view.findViewById(R.id.athan_timing5);
        sunrise_time = view.findViewById(R.id.sunrise_timing1);

        // ── تطبيق الخط + اللون الأحمر المضيء ──
        applyDigitalStyle(prayer1_time);
        applyDigitalStyle(prayer2_time);
        applyDigitalStyle(prayer3_time);
        applyDigitalStyle(prayer4_time);
        applyDigitalStyle(prayer5_time);
        applyDigitalStyle(sunrise_time);
        applyDigitalStyle(countdownTimerView);

        is_Manual_Location = view.findViewById(R.id.is_manual_location);
        currentLocation    = view.findViewById(R.id.current_location);

        boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
        is_Manual_Location.setChecked(isLocationManual);
        currentLocation.setText(MainActivity.getCityCountryLocation(this.getContext()));

        currentLocation.setOnClickListener(this);
        is_Manual_Location.setOnClickListener(this);

        fajr_switch    = view.findViewById(R.id.switch1);
        duhr_switch    = view.findViewById(R.id.switch2);
        asr_switch     = view.findViewById(R.id.switch3);
        maghrib_switch = view.findViewById(R.id.switch4);
        ishaa_switch   = view.findViewById(R.id.switch5);

        fajr_switch.setChecked(mPrefs.getBoolean("isFajrReminder", true));
        duhr_switch.setChecked(mPrefs.getBoolean("isDuhrReminder", true));
        asr_switch.setChecked(mPrefs.getBoolean("isAsrReminder", true));
        maghrib_switch.setChecked(mPrefs.getBoolean("isMaghribReminder", true));
        ishaa_switch.setChecked(mPrefs.getBoolean("isIshaaReminder", true));

        fajr_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.edit().putBoolean("isFajrReminder", isChecked).apply();
            updateAthanAlarms();
        });
        duhr_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.edit().putBoolean("isDuhrReminder", isChecked).apply();
            updateAthanAlarms();
        });
        asr_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.edit().putBoolean("isAsrReminder", isChecked).apply();
            updateAthanAlarms();
        });
        maghrib_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.edit().putBoolean("isMaghribReminder", isChecked).apply();
            updateAthanAlarms();
        });
        ishaa_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.edit().putBoolean("isIshaaReminder", isChecked).apply();
            updateAthanAlarms();
        });

        PreferenceManager.getDefaultSharedPreferences(this.getContext()).registerOnSharedPreferenceChangeListener(prefListener);
        this.updateprayerTimes();
        return view;
    }

    // ── تطبيق الخط + اللون الأحمر + الـ Glow ──
    private void applyDigitalStyle(TextView tv) {
        if (tv == null) return;
        if (digitalFont != null) tv.setTypeface(digitalFont);
        tv.setTextColor(Color.RED);
        tv.setShadowLayer(18f, 0f, 0f, Color.RED);
        tv.setBackgroundColor(Color.BLACK);
        tv.setPadding(8, 4, 8, 4);
    }

    private void updateprayerTimes() {
        double latitude  = Double.parseDouble(MainActivity.getLatitude(this.getContext()));
        double longitude = Double.parseDouble(MainActivity.getLongitude(this.getContext()));
        if (latitude == 0.0 && longitude == 0.0) {
            prayer1_time.setText("--:--");
            sunrise_time.setText("--:--");
            prayer2_time.setText("--:--");
            prayer3_time.setText("--:--");
            prayer4_time.setText("--:--");
            prayer5_time.setText("--:--");
            return;
        }
        prayers = getPrayersArray();
        try {
            prayer1_time.setText(prayers[0].getTime());
            sunrise_time.setText(prayers[1].getTime());
            prayer2_time.setText(prayers[2].getTime());
            prayer3_time.setText(prayers[3].getTime());
            prayer4_time.setText(prayers[5].getTime());
            prayer5_time.setText(prayers[6].getTime());
        } catch (NullPointerException e) {
            // ignore
        }

        updateAthanAlarms();
        startCountdown();
    }

    private void updateAthanAlarms() {
        new MyAlarmsManager(this.getActivity().getApplicationContext()).UpdateAllApplicableAlarms();
    }

    // ══════════════════════════════════════════
    // العداد التنازلي
    // ══════════════════════════════════════════

    private void startCountdown() {
        if (prayers == null) return;
        if (countdownTimer != null) countdownTimer.cancel();

        int[] indices = {0, 2, 3, 5, 6};
        long now = System.currentTimeMillis();
        long millisLeft = -1;
        String nextName = "";

        for (int i : indices) {
            long t = parseTimeToMillis(prayers[i].getTime());
            if (t > now) {
                millisLeft = t - now;
                nextName   = prayers[i].getName();
                break;
            }
        }

        if (millisLeft < 0) {
            millisLeft = parseTimeToMillis(prayers[0].getTime()) + 86400000L - now;
            nextName   = prayers[0].getName();
        }

        final String finalName = nextName;

        countdownTimer = new CountDownTimer(millisLeft, 1000) {
            @Override
            public void onTick(long ms) {
                long h = ms / 3600000;
                long m = (ms % 3600000) / 60000;
                long s = (ms % 60000) / 1000;
                if (countdownTimerView != null) {
                    countdownTimerView.setText(
                            finalName + "  |  " + String.format("%02d:%02d:%02d", h, m, s));
                }
            }
            @Override
            public void onFinish() {
                if (getContext() != null) startCountdown();
            }
        }.start();
    }

    private long parseTimeToMillis(String t) {
        try {
            t = t.trim().toLowerCase();
            boolean pm = t.contains("pm");
            t = t.replace("am", "").replace("pm", "").trim();
            String[] parts = t.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            if (pm && h != 12) h += 12;
            if (!pm && h == 12) h = 0;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, m);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    // ══════════════════════════════════════════

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.getActivity().onBackPressed();
            return true;
        }
        return false;
    }

    private Prayer[] getPrayersArray() {
        PrayTime prayersObject = PrayTime.instancePrayTime(this.getActivity().getApplicationContext());
        String[] times  = prayersObject.getPrayerTimes(this.getActivity().getApplicationContext());
        String[] names  = prayersObject.getTimeNames();
        Prayer[] prayers = new Prayer[7];
        for (int i = 0; i < 7; i++) {
            prayers[i] = new Prayer(names[i], times[i]);
        }
        return prayers;
    }

    @Override
    public void onPause() {
        if (countdownTimer != null) countdownTimer.cancel();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this.getContext()).registerOnSharedPreferenceChangeListener(prefListener);
        this.updateprayerTimes();
    }

    @Override
    public void onDestroy() {
        if (countdownTimer != null) countdownTimer.cancel();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.is_manual_location:
                if (is_Manual_Location.isChecked()) {
                    CustomLocation Customlocation = new CustomLocation(this.getActivity());
                    Customlocation.setCanceledOnTouchOutside(true);
                    Customlocation.setOnDismissListener(this);
                    Customlocation.show();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit().putBoolean("isCustomLocation", false).apply();
                    mCallback.requestLocationUpdate();
                }
                updateprayerTimes();
                updateAthanAlarms();
                break;
            case R.id.current_location:
                boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
                if (isLocationManual) {
                    CustomLocation Customlocation = new CustomLocation(this.getActivity());
                    Customlocation.setCanceledOnTouchOutside(true);
                    Customlocation.setOnDismissListener(this);
                    Customlocation.show();
                }
                break;
        }
    }

    private String getHijriDate() {
        ChronoFormatter<HijriCalendar> hijriFormat =
                ChronoFormatter.setUp(HijriCalendar.family(), this.getResources().getConfiguration().locale)
                        .addPattern(" dd MMMM yyyy", PatternType.CLDR)
                        .build()
                        .withCalendarVariant(HijriCalendar.VARIANT_UMALQURA);

        HijriCalendar today =
                SystemClock.inLocalView().today().transform(
                        HijriCalendar.class,
                        HijriCalendar.VARIANT_UMALQURA
                );
        return hijriFormat.format(today);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        Log.d("AthanFragment", "onDismiss called.");
        if (this.getContext() != null) {
            boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
            is_Manual_Location.setChecked(isLocationManual);
        }
    }
}
