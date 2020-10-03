package com.example.waterreminderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waterreminderapp.sync.ReminderTasks;
import com.example.waterreminderapp.sync.WaterReminderIntentService;
import com.example.waterreminderapp.utilities.NotificationWorkManagerUtils;
import com.example.waterreminderapp.utilities.PreferenceUtilities;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mWaterCountDisplay;
    private TextView mChargingCountDisplay;
    private ImageView mChargingImageView;

    private Toast mToast;

    /**
     * A new instance variable for ChargingBroadcastReceiver Class.
     * This class in below of this Class.
     */
    ChargingBroadcastReceiver mChargingReceiver;
    IntentFilter mChargingIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWaterCountDisplay = findViewById(R.id.tv_water_count);
        mChargingCountDisplay = findViewById(R.id.tv_charging_reminder_count);
        mChargingImageView = findViewById(R.id.iv_power_increment);

        updateWaterCount();
        updateChargingReminderCount();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        mChargingIntentFilter = new IntentFilter();
        mChargingReceiver = new ChargingBroadcastReceiver();

        mChargingIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        mChargingIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
    }


    /**
     * Updates the TextView to display the new water count from SharedPreferences
     */
    private void updateWaterCount() {
        int waterCount = PreferenceUtilities.getWaterCount(this);
        mWaterCountDisplay.setText(waterCount + "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleChargingReminder();

        /** Determine the current charging state **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            showCharging(batteryManager.isCharging());
        } else {
            /**
             * A new intent filter with the action ACTION_BATTERY_CHANGED. This is a
             * sticky broadcast that contains a lot of information about the battery state.
             */
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            /**
             *  Set a new Intent object equal to what is returned by registerReceiver, passing in null
             *  for the receiver. Pass in our intent filter as well. Passing in null means that we
             *  getting the current state of a sticky broadcast - the intent returned will contain the
             *  battery information you need.
             */
            Intent currentBatteryStatusIntent = registerReceiver(null, iFilter);

            int batteryStatus = currentBatteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            boolean isCharging;
            if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING || batteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
                isCharging = true;
            } else {
                isCharging = false;
            }

            showCharging(isCharging);
        }

        registerReceiver(mChargingReceiver, mChargingIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mChargingReceiver);
    }

    /**
     * Updates the TextView to display the new charging reminder count from SharedPreferences
     */
    private void updateChargingReminderCount() {
        int chargingReminders = PreferenceUtilities.getChargingReminderCount(this);
        String formattedChargingReminders = getResources().getQuantityString(R.plurals.charge_notification_count, chargingReminders, chargingReminders);
        mChargingCountDisplay.setText(formattedChargingReminders);
    }

    public void incrementWater(View view) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, R.string.water_chug_toast, Toast.LENGTH_SHORT);
        mToast.show();

        Intent incrementWaterCountIntent = new Intent(this, WaterReminderIntentService.class);
        incrementWaterCountIntent.setAction(ReminderTasks.ACTION_INCREMENT_WATER_COUNT);
        startService(incrementWaterCountIntent);
    }

//    public void testNotification(View view) {
//        NotificationUtils.remindUserBecauseCharging(this);
//    }

    private void scheduleChargingReminder() {
//        Constraints constraints = new Constraints.Builder()
//                .setRequiresCharging(true)
//                .build();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                NotificationWorkManagerUtils.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueue(periodicWorkRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceUtilities.KEY_WATER_COUNT.equals(key)) {
            updateWaterCount();
        } else if (PreferenceUtilities.KEY_CHARGING_REMINDER_COUNT.equals(key)) {
            updateChargingReminderCount();
        }
    }

    private void showCharging(boolean isCharging) {
        if (isCharging) {
            mChargingImageView.setImageResource(R.drawable.ic_power_connected_24dp);
        } else {
            mChargingImageView.setImageResource(R.drawable.ic_power_ash_24dp);
        }
    }

    /**
     * Here is our BroadcastReceiver Class which is a core component of Android.
     */
    private class ChargingBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean isCharging = (action.equals(Intent.ACTION_POWER_CONNECTED));
            showCharging(isCharging);
        }
    }

}
