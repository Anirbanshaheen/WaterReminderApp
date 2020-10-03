package com.example.waterreminderapp.utilities;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorkManagerUtils extends Worker {


    public NotificationWorkManagerUtils(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationUtils.remindUserBecauseCharging(getApplicationContext());
        return Result.success();
    }


}
