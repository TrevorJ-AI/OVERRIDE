package com.override.wakeup;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private TimePicker timePicker;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        statusText = (TextView) findViewById(R.id.statusText);

        Button setButton = (Button) findViewById(R.id.setButton);
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        Button testButton = (Button) findViewById(R.id.testButton);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = getPickerHour();
                int minute = getPickerMinute();
                AlarmScheduler.scheduleDaily(MainActivity.this, hour, minute);
                refreshStatus();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmScheduler.cancel(MainActivity.this);
                refreshStatus();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, 10);
                AlarmScheduler.scheduleAt(MainActivity.this, cal.getTimeInMillis());
                statusText.setText("Test alarm firing in 10 seconds...");
            }
        });

        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private int getPickerHour() {
        return Build.VERSION.SDK_INT >= 23 ? timePicker.getHour() : timePicker.getCurrentHour();
    }

    private int getPickerMinute() {
        return Build.VERSION.SDK_INT >= 23 ? timePicker.getMinute() : timePicker.getCurrentMinute();
    }

    private void refreshStatus() {
        SharedPreferences prefs = getSharedPreferences(AlarmScheduler.PREFS, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(AlarmScheduler.KEY_ENABLED, false);
        if (enabled) {
            int hour = prefs.getInt(AlarmScheduler.KEY_HOUR, 0);
            int minute = prefs.getInt(AlarmScheduler.KEY_MINUTE, 0);
            statusText.setText(String.format(Locale.US, "Alarm armed daily at %02d:%02d", hour, minute));
        } else {
            statusText.setText("No alarm set");
        }
    }
}
