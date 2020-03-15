package com.carpark;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import static android.Manifest.permission.CAMERA;

public class QrCodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView mScannerView;
    private String mode;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Log.e("onCreate", "onCreate");
        mode = getIntent().getStringExtra("mode");
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
            //    Toast.makeText(getApplicationContext(), "Permission already granted", Toast.LENGTH_LONG).show();

            } else {
                requestPermission();
            }
        }
    }


    private boolean checkPermission() {
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(QrCodeScannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(mScannerView == null) {
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {

        final String result = rawResult.getText();
        final int qrId = Integer.parseInt(result);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("QR", qrId);
        jsonObject.addProperty("mode", this.mode);
        Gson g = new Gson();
        String request = g.toJson(jsonObject);
        Client client = new Client(request, new Client.AsyncResponse(){
            @Override
            public void processFinish(String output){
                Gson g = new Gson();
                Response response = g.fromJson(output, Response.class);
                Intent intent;
                try {
                    if(response.status.equals("success")) {
                        if(response.fare == 0) {
                            intent = new Intent(getApplicationContext(), HomeActivity.class);
                        } else {
                            intent = new Intent(getApplicationContext(), PaymentActivity.class);
                            intent.putExtra("fare", response.fare);
                            intent.putExtra("QR", qrId);
                        }

                    } else {
                        if(mode.equals("open")) {
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                        } else {
                            intent = new Intent(getApplicationContext(), HomeActivity.class);
                        }
                    }
                }
                catch(Exception e) {
                    Log.d("exception", e.toString());
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                }

                startActivity(intent);
                finish();
            }
        });
        client.execute();

    }

    @Override
    public void onBackPressed() {
        if(mode.equals("open")) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("timerRunning", true);
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void toast(String message){
        String toast = message;
        Toast.makeText(getApplicationContext(),toast, Toast.LENGTH_SHORT).show();
        finish();
    }

}
