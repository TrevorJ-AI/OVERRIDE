package com.override.wakeup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class AlarmScheduler {

    public static final String PREFS = "override_prefs";
    public static final String KEY_HOUR = "hour";
    public static final String KEY_MINUTE = "minute";
    public static final String KEY_ENABLED = "enabled";

    private static PendingIntent buildPendingIntent(Context ctx) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        return PendingIntent.getBroadcast(ctx, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent buildShowIntent(Context ctx) {
        Intent showIntent = new Intent(ctx, MainActivity.class);
        return PendingIntent.getActivity(ctx, 1002, showIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleAt(Context ctx, long triggerAtMillis) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPendingIntent(ctx);
        AlarmManager.AlarmClockInfo info =
                new AlarmManager.AlarmClockInfo(triggerAtMillis, buildShowIntent(ctx));
        am.setAlarmClock(info, pi);
    }

    public static void scheduleDaily(Context ctx, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        scheduleAt(ctx, cal.getTimeInMillis());

        SharedPreferences.Editor e = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        e.putInt(KEY_HOUR, hour);
        e.putInt(KEY_MINUTE, minute);
        e.putBoolean(KEY_ENABLED, true);
        e.apply();
    }

    public static void cancel(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(buildPendingIntent(ctx));

        SharedPreferences.Editor e = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        e.putBoolean(KEY_ENABLED, false);
        e.apply();
    }
}
