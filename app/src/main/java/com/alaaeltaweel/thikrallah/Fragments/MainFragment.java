package com.alaaeltaweel.thikrallah.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.alaaeltaweel.thikrallah.MainActivity;
import com.alaaeltaweel.thikrallah.PreferenceActivity;
import com.alaaeltaweel.thikrallah.R;
import com.alaaeltaweel.thikrallah.Utilities.MainInterface;
import com.alaaeltaweel.thikrallah.hisnulmuslim.DuaGroupActivity;
import com.alaaeltaweel.thikrallah.quran.labs.androidquran.QuranDataActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainFragment extends Fragment {
    private MainInterface mCallback;
    private Context mContext;
    SharedPreferences mPrefs;
    String TAG = "MainFragment";

    public MainFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("MainFragment","onattach called");

        MainActivity.setLocale(context);

        mContext = context;
        try {
            mCallback = (MainInterface) mContext;
        } catch (ClassCastException e) {
            throw new ClassCastException(mContext.toString()
                    + " must implement MainInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        MainActivity.setLocale(this.getContext());
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button button_remind_me_settings = (Button) view.findViewById(R.id.button_settings);
        Button button_morning_thikr = (Button) view.findViewById(R.id.button_morning_thikr);
        Button button_night_thikr = (Button) view.findViewById(R.id.button_night_thikr);
        Button button_my_athkar = (Button) view.findViewById(R.id.button_my_athkar);
        Button button_sadaqa = (Button) view.findViewById(R.id.button_sadaqa);
        Button button_quran = (Button) view.findViewById(R.id.button_quran);
        Button button_hisn_almuslim = (Button) view.findViewById(R.id.hisn_almuslim);
        Button button_athan = (Button) view.findViewById(R.id.button_athan);
        Button button_qibla = (Button) view.findViewById(R.id.button_qibla);

        // ✅ التاريخ والرمضان
        TextView textGregorianDate = view.findViewById(R.id.text_gregorian_date);
        TextView textHijriDate = view.findViewById(R.id.text_hijri_date);
        TextView textRamadanInfo = view.findViewById(R.id.text_ramadan_info);
        TextView textRamadanCountdown = view.findViewById(R.id.text_ramadan_countdown);

        updateDateAndRamadan(textGregorianDate, textHijriDate, textRamadanInfo, textRamadanCountdown);

        button_athan.setOnClickListener(v -> mCallback.launchFragment(new AthanFragment(), new Bundle(), "AthanFragment"));
        button_qibla.setOnClickListener(v -> mCallback.launchFragment(new QiblaFragment(), new Bundle(), "QiblaFragment"));
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        button_quran.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(v.getContext(), QuranDataActivity.class);
            startActivityForResult(intent, 0);
        });
        button_sadaqa.setOnClickListener(v -> mCallback.share());
        button_remind_me_settings.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(v.getContext(), PreferenceActivity.class);
            startActivityForResult(intent, 0);
        });
        button_hisn_almuslim.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(v.getContext(), DuaGroupActivity.class);
            startActivityForResult(intent, 0);
        });
        button_morning_thikr.setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putString("DataType", MainActivity.DATA_TYPE_DAY_THIKR);
            mCallback.launchFragment(new ThikrFragment(), data, "ThikrFragment");
        });
        button_night_thikr.setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putString("DataType", MainActivity.DATA_TYPE_NIGHT_THIKR);
            mCallback.launchFragment(new ThikrFragment(), data, "ThikrFragment");
        });
        button_my_athkar.setOnClickListener(v -> {
            Bundle data = new Bundle();
            mCallback.launchFragment(new MyAthkarFragment(), data, "MyAthkarFragment");
        });
   button_prayer_tracker.setOnClickListener(v -> {
    Intent intent = new Intent();
    intent.setClass(v.getContext(), PrayerTrackerActivity.class);
    startActivityForResult(intent, 0);
});
        Log.d(TAG,"requestBatteryExclusion");
        requestBatteryExclusion(mContext);
        return view;
    }

    // ===================== التاريخ والرمضان =====================
    private void updateDateAndRamadan(TextView gregorianView, TextView hijriView,
                                       TextView ramadanInfoView, TextView ramadanCountdownView) {
        try {
            // التاريخ الميلادي
            SimpleDateFormat gregorianFormat = new SimpleDateFormat("EEEE، d MMMM yyyy", new Locale("ar"));
            String gregorianDate = gregorianFormat.format(new Date());
            gregorianView.setText(gregorianDate);

            // التاريخ الهجري
            android.icu.util.IslamicCalendar islamicCalendar = new android.icu.util.IslamicCalendar();
            int hijriDay = islamicCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH);
            int hijriMonth = islamicCalendar.get(android.icu.util.Calendar.MONTH);
            int hijriYear = islamicCalendar.get(android.icu.util.Calendar.YEAR);

            String[] hijriMonths = {
                "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
                "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
                "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
            };

            String hijriMonthName = hijriMonths[hijriMonth];
            hijriView.setText(hijriDay + " " + hijriMonthName + " " + hijriYear);

            // هل دلوقتي رمضان؟
            boolean isRamadan = (hijriMonth == 8); // رمضان = الشهر 9 (index 8)

            if (isRamadan) {
                ramadanInfoView.setText("🌙 رمضان كريم");
                // عدد الأيام المتبقية في رمضان
                int daysLeft = 30 - hijriDay;
                if (daysLeft > 0) {
                    ramadanCountdownView.setText("باقي على نهاية رمضان " + daysLeft + " يوم");
                } else {
                    ramadanCountdownView.setText("آخر يوم في رمضان");
                }
            } else {
                // كم يوم فاضل على رمضان
                android.icu.util.IslamicCalendar nextRamadan = new android.icu.util.IslamicCalendar();
                nextRamadan.set(android.icu.util.Calendar.MONTH, 8);
                nextRamadan.set(android.icu.util.Calendar.DAY_OF_MONTH, 1);
                if (hijriMonth >= 8) {
                    nextRamadan.set(android.icu.util.Calendar.YEAR, hijriYear + 1);
                } else {
                    nextRamadan.set(android.icu.util.Calendar.YEAR, hijriYear);
                }

                long diffMs = nextRamadan.getTimeInMillis() - System.currentTimeMillis();
                long daysToRamadan = diffMs / (1000 * 60 * 60 * 24);

                ramadanInfoView.setText("🌙 " + hijriMonthName);
                ramadanCountdownView.setText("باقي على رمضان " + daysToRamadan + " يوم");
            }

        } catch (Exception e) {
            Log.d(TAG, "Error updating date: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        logScreen();
    }

    private void logScreen() {
    }

    private void requestBatteryExclusion(Context mContext) {
        if (!mPrefs.getBoolean("isFirstLaunch", true)
                && !mPrefs.getBoolean("permissionsRequested", false)) {

            mPrefs.edit().putBoolean("permissionsRequested", true).apply();

            mCallback.requestOverLayPermission();
            mCallback.requestNotificationPermission();
            mCallback.requestBatteryExclusion();
            mCallback.requestExactAlarmPermission();
            mCallback.requestLocationPermission();
            if (mPrefs.getBoolean("isMediaPermissionNeeded", false)){
                mCallback.requestMediaOrStoragePermission();
            }
        }
    }
}
