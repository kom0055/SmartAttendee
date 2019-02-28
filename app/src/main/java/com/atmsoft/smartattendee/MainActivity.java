package com.atmsoft.smartattendee;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView mTextMessage;

    private WifiManager wifiManager = null;

    private Button btnPunchOut = null;

    private Button btnPunchIn = null;

    private Button btnModify = null;

    private EditText txtSSId = null;

    public static final String HW_SSID = "\"Huawei-Employee\"";
    public static String TARGET_SSID = null;
    private static final String MODIFY_SUCCESS = "You Modify Successfully";
    private static final String MODIFY_FAILED = "You Modify Failed";
    private static final String PUNCH_IN_SUCCESS = "You Punch In Successfully";
    private static final String PUNCH_IN_FAILED = "You Punch In Failed";
    private static final String PUNCH_OUT_SUCCESS = "You Punch Out Successfully";
    private static final String PUNCH_OUT_FAILED = "You Punch Out Failed";

    private WifiConfiguration configuration = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mTextMessage = (TextView) findViewById(R.id.message);
        btnPunchOut = (Button) findViewById(R.id.btnPunchOut);
        btnPunchIn = (Button) findViewById(R.id.btnPunchIn);
        btnModify = (Button) findViewById(R.id.btnModify);
        txtSSId = (EditText) findViewById(R.id.txtSSId);
        if (null == wifiManager) {
            return;
        }

        int wifiState = wifiManager.getWifiState();
        if (WifiManager.WIFI_STATE_ENABLED != wifiState && WifiManager.WIFI_STATE_ENABLING != wifiState) {
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();


        startService(new Intent(this, DaemonService.class));
        btnPunchOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.setWifiEnabled(false);
                Toast.makeText(MainActivity.this, PUNCH_OUT_SUCCESS, Toast.LENGTH_SHORT).show();
            }
        });

        btnPunchIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.setWifiEnabled(true);
                if (null == configuration) {
                    Toast.makeText(MainActivity.this, PUNCH_IN_FAILED, Toast.LENGTH_SHORT).show();
                    return;
                }
                wifiManager.enableNetwork(configuration.networkId, true);
                Toast.makeText(MainActivity.this, PUNCH_IN_SUCCESS, Toast.LENGTH_SHORT).show();
            }
        });

        btnModify.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                // stopService(new Intent(MainActivity.this, DaemonService.class));
                List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
                if (null == wifiConfigurations || 0 == wifiConfigurations.size()) {
                    return;
                }
                String input = txtSSId.getText().toString();
                if ("".equals(input)) {
                    TARGET_SSID = HW_SSID;
                } else {
                    TARGET_SSID = "\"" + input + "\"";
                }
                ScheduleService.TARGET_SSID = TARGET_SSID;
                configuration = null;
                if (1 == wifiConfigurations.size()) {
                    configuration = wifiConfigurations.get(0);
                } else {
                    configuration = wifiConfigurations.stream().filter(p -> {
                        if (null == p || null == p.SSID) {
                            return false;
                        }
                        return TARGET_SSID.equals(p.SSID);
                    }).findFirst().orElse(null);
                }
                if (null == configuration) {
                    Toast.makeText(MainActivity.this, MODIFY_FAILED, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, MODIFY_SUCCESS, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


}
