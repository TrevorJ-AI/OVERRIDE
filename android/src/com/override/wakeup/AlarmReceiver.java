package com.override.wakeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        Intent svc = new Intent(ctx, AlarmService.class);
        Compat.startForegroundServiceCompat(ctx, svc);

        // Daily alarm: immediately re-arm for the same time tomorrow.
        SharedPreferences prefs = ctx.getSharedPreferences(AlarmScheduler.PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(AlarmScheduler.KEY_ENABLED, false)) {
            int hour = prefs.getInt(AlarmScheduler.KEY_HOUR, -1);
            int minute = prefs.getInt(AlarmScheduler.KEY_MINUTE, -1);
            if (hour >= 0 && minute >= 0) {
                AlarmScheduler.scheduleDaily(ctx, hour, minute);
            }
        }
    }
}
