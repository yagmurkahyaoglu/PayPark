package com.carpark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean chronometerRunning = prefs.getBoolean("timerRunning", false);
        boolean countdownRunning = prefs.getBoolean("countDownRunning", false);
        if(chronometerRunning) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        if(countdownRunning) {
            int qr = prefs.getInt("QR", 0);
            int fare = prefs.getInt("fare", 0);
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("fare", fare);
            intent.putExtra("QR", qr);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
    }

    public void scanQR(View view) {
        try {

            Intent intent = new Intent(this, QrCodeScannerActivity.class);
            intent.putExtra("mode", "open");
            startActivityForResult(intent, 0);
            finish();

        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
    }

}
