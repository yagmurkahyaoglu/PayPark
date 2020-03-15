package com.carpark;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private static final long START_TIME_IN_MILLIS = 1000*60*5;
    private CountDownTimer countdown;
    private TextView countdownText;
    private boolean running;
    private long timeLeft;
    private long endTime;
    private int fare;
    private boolean isPaid = false;
    private int qrId;
    boolean confirm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        countdownText = findViewById(R.id.countdown);
        qrId = getIntent().getIntExtra("QR",0);
        fare = getIntent().getIntExtra("fare",0);
        TextView fareText = findViewById(R.id.payment);
        String text = this.fare + ".00 TL";
        fareText.setText(text);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("QR", qrId);
        editor.putInt("fare", fare);
        editor.apply();
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeft;

        countdown = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                if(!confirm) {
                    updateCountDownText();
                }

            }

            @Override
            public void onFinish() {
                running = false;
                if(!confirm) {
                    cancelPayment();
                }

            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeft / 1000) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        countdownText.setText(timeLeftFormatted);
    }


    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", timeLeft);
        editor.putBoolean("countDownRunning", running);
        editor.putLong("countDownEndTime", endTime);

        editor.apply();

        if (countdown != null) {
            countdown.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        timeLeft = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        running = prefs.getBoolean("countDownRunning", false);

        updateCountDownText();

        if (running) {
            endTime = prefs.getLong("countDownEndTime", 0);
            timeLeft = endTime - System.currentTimeMillis();

            if (timeLeft < 0) {
                timeLeft = 0;
                running = false;
                updateCountDownText();
                cancelPayment();
            } else {
                startTimer();
            }
        } else {
            timeLeft = START_TIME_IN_MILLIS;
            running = true;
            startTimer();
        }
    }

    public void cancelPayment() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        if(isPaid) {
            editor.putBoolean("timerRunning", false);
        } else {
            editor.putBoolean("timerRunning", true);
        }

        editor.apply();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("QR", qrId);
        jsonObject.addProperty("mode", "open");
        Gson g = new Gson();
        String request = g.toJson(jsonObject);
        Client client = new Client(request, new Client.AsyncResponse(){
            @Override
            public void processFinish(String output){
                Gson g = new Gson();
                Response response = g.fromJson(output, Response.class);
                Intent intent;
                if(response.status.equals("success")) {
                    intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
        client.execute();
    }

    public void confirmPayment(final View view) {

        TextInputEditText name = findViewById(R.id.name_text);
        TextInputEditText number = findViewById(R.id.card_number_text);
        TextInputEditText date = findViewById(R.id.card_date_text);
        TextInputEditText cvv = findViewById(R.id.cvv_text);

        if(name.getText().toString().isEmpty()|| number.getText().toString().isEmpty()|| date.getText().toString().isEmpty() || cvv.getText().toString().isEmpty()) {
            toast("Please fill all fields.");
            return;
        }

        final LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_window, null);

        final int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        final int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name.getText().toString());
        jsonObject.addProperty("number", number.getText().toString());
        jsonObject.addProperty("date", date.getText().toString());
        jsonObject.addProperty("cvv", cvv.getText().toString());
        jsonObject.addProperty("fareAmount", this.fare);

        Gson g = new Gson();
        String request = g.toJson(jsonObject);
        Client client = new Client(request, new Client.AsyncResponse(){
            @Override
            public void processFinish(String output){
                Gson g = new Gson();
                Response response = g.fromJson(output, Response.class);
                if(response.status.equals("success")) {
                    isPaid = true;
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, false);
                    countdownText.setVisibility(View.GONE);
                    countdownText = popupView.findViewById(R.id.text_view_countdown);
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                    View container = popupWindow.getContentView().getRootView();
                    Context context = popupWindow.getContentView().getContext();
                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
                    p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                    p.dimAmount = 0.7f;
                    wm.updateViewLayout(container, p);
                }
            }
        });
        client.execute();


        Button openGate = popupView.findViewById(R.id.gate_button);
        openGate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm = true;
                userPrompt();
            }
        });

    }

    public void openGate(){
        timeLeft = START_TIME_IN_MILLIS;
        running = false;
        toast("The gate will open in 3 seconds!");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("gate", 1);

        Gson g = new Gson();
        String request = g.toJson(jsonObject);
        Client client = new Client(request, new Client.AsyncResponse(){
            @Override
            public void processFinish(String output){
                Gson g = new Gson();
                Response response = g.fromJson(output, Response.class);
                Intent intent;
                if(response.status.equals("success")) {
                    toast("The gate is opening!");
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    toast("Someone else is passing through the gate. Wait for your turn!");
                }
            }
        });
        client.execute();
    }

    public void userPrompt(){
        AlertDialog ad = new AlertDialog.Builder(this).setMessage(
                "The gate will open immediately after you press OK. Make sure you are in front of the gate with your car.").setTitle(
                "Be ready!").setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                openGate();
                            }
                        }).setNeutralButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        }).show();
    }

    public void toast(String message){
        String toast = message;
        Toast.makeText(getApplicationContext(),toast, Toast.LENGTH_SHORT).show();
    }

}
