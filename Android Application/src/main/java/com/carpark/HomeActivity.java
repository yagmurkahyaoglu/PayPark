package com.carpark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private Chronometer chronometer;
    boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        chronometer = findViewById(R.id.chronometer);
//        chronometer.setFormat("Time: %s");


    }

    public void startTimer(long millis) {
        chronometer.setBase(millis);
        chronometer.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        running = prefs.getBoolean("timerRunning", false);

        if (running) {
            long endTime = prefs.getLong("endTime", 0);
            long lastTime = prefs.getLong("lastTime", 0);
            long timePassed = System.currentTimeMillis() - endTime + lastTime;
            startTimer(SystemClock.elapsedRealtime() - timePassed);
        } else {
            startTimer(SystemClock.elapsedRealtime());
            running = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("timerRunning", running);
        editor.putLong("endTime", System.currentTimeMillis());
        editor.putLong("lastTime", getTimeInMillis());

        editor.apply();

        if (chronometer != null) {
            chronometer.stop();
        }

    }

    public void stopChronometer(View view) {

        running = false;

        Intent intent = new Intent(this, QrCodeScannerActivity.class);
        intent.putExtra("mode", "close");
        startActivityForResult(intent, 0);
        finish();


    }

    public long getTimeInMillis() {

        long stoppedMilliseconds = 0;

        String chronoText = chronometer.getText().toString();
        String array[] = chronoText.split(":");
        if (array.length == 2) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
                    + Integer.parseInt(array[1]) * 1000;
        } else if (array.length == 3) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000
                    + Integer.parseInt(array[1]) * 60 * 1000
                    + Integer.parseInt(array[2]) * 1000;
        }

        return stoppedMilliseconds;
    }


}
