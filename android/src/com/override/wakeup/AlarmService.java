package com.override.wakeup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;

public class AlarmService extends Service {

    public static final String ACTION_STOP = "com.override.wakeup.ACTION_STOP";
    private static final long MAX_RING_MS = 15 * 60 * 1000L; // safety auto-stop
    private static final long[] VIBRATE_PATTERN = {0, 800, 400};

    private MediaPlayer player;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    private Runnable timeoutRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        startForeground(42, buildNotification());
        acquireWakeLock();
        launchAlarmActivity();
        startSound();
        startVibration();

        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                stopAlarm();
            }
        };
        handler.postDelayed(timeoutRunnable, MAX_RING_MS);

        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent activityIntent = new Intent(this, AlarmActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("OVERRIDE")
                .setContentText("Solve the math to stop the alarm")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_ALARM)
                .setContentIntent(contentIntent)
                .setFullScreenIntent(contentIntent, true);
        return builder.build();
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE;
        wakeLock = pm.newWakeLock(flags, "override:alarm");
        wakeLock.acquire(MAX_RING_MS + 5000);
    }

    private void launchAlarmActivity() {
        Intent i = new Intent(this, AlarmActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    private void startSound() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);

        try {
            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setDataSource(this, uri);
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception e) {
            // Fall back to the system default alarm ringtone object if MediaPlayer setup fails.
            try {
                Ringtone rt = RingtoneManager.getRingtone(this,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                if (rt != null) {
                    rt.setStreamType(AudioManager.STREAM_ALARM);
                    rt.play();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(VIBRATE_PATTERN, 0);
        }
    }

    private void stopAlarm() {
        if (handler != null && timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
        }
        if (player != null) {
            try {
                if (player.isPlaying()) player.stop();
                player.release();
            } catch (Exception ignored) {
            }
            player = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
