package com.example.waterreminderapp.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * A subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WaterReminderIntentService extends IntentService {

    public WaterReminderIntentService() {
        super("WaterReminderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        ReminderTasks.executeTask(this, action);
    }
}
