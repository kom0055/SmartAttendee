package com.atmsoft.smartattendee;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private WifiManager wifiManager = null;

    private Button btnPunch = null;

    public static final String HW_SSID = "\"Huawei-Employee\"";


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        btnPunch = findViewById(R.id.btnPunch);

        if (null == wifiManager) {
            return;
        }

        int wifiState = wifiManager.getWifiState();
        if (WifiManager.WIFI_STATE_ENABLED != wifiState && WifiManager.WIFI_STATE_ENABLING != wifiState) {
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();


        startService(new Intent(this, DaemonService.class));


        btnPunch.setOnClickListener(v -> new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                Utils.SendPunch(wifiManager, HW_SSID);
            }
        }.start());

    }


}
