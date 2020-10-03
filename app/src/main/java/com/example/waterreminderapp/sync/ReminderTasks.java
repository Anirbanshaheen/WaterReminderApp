package com.example.waterreminderapp.sync;

import android.content.Context;
import com.example.waterreminderapp.utilities.NotificationUtils;
import com.example.waterreminderapp.utilities.PreferenceUtilities;

/**
 * All type of Reminder operation call goes here.
 */
public class ReminderTasks {

    public static final String ACTION_INCREMENT_WATER_COUNT = "increment-water-count";
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    public static final String ACTION_CHARGING_REMINDER = "charging-reminder";


    public static void executeTask(Context context, String action) {
        if (ACTION_INCREMENT_WATER_COUNT.equals(action)) {
            incrementWaterCount(context);
        } else if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            NotificationUtils.clearAllNotifications(context);
        }else if (ACTION_CHARGING_REMINDER.equals(action)){
            issueChargingReminder(context);
        }
    }

    private static void issueChargingReminder(Context context) {
        PreferenceUtilities.incrementChargingReminderCount(context);
        NotificationUtils.remindUserBecauseCharging(context);
    }



    private static void incrementWaterCount(Context context) {
        PreferenceUtilities.incrementWaterCount(context);
        /**
         * If the water count was incremented, clear any notifications
         */
        NotificationUtils.clearAllNotifications(context);
    }
}
