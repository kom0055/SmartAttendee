package com.atmsoft.smartattendee;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class ScheduleService extends Service {
    private WifiManager wifiManager = null;
    public static String TARGET_SSID = MainActivity.HW_SSID;


    private String TAG = getClass().getName();

    private String Process_Name = "com.atmsoft.smartattendee.DaemonService:serviceDaemon";
    private Timestamp left = new Timestamp(119, 1, 3, 22, 0, 0, 0);
    private Timestamp right = new Timestamp(119, 1, 11, 6, 0, 0, 0);
    private StrongService startDaemonService = new StrongService.Stub() {
        @Override
        public void startService() {
            Intent i = new Intent(getBaseContext(), DaemonService.class);
            getBaseContext().startService(i);
        }

        @Override
        public void stopService() {
            Intent i = new Intent(getBaseContext(), DaemonService.class);
            getBaseContext().stopService(i);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void keepDaemonService() {
        Log.d(TAG, "keepDaemonService");
        boolean isRun = Utils.isProcessRunning(ScheduleService.this, Process_Name);
        if (!isRun) {
            try {
                Toast.makeText(getBaseContext(), "Restart Daemon Service", Toast.LENGTH_SHORT).show();
                startDaemonService.startService();
                Log.d(TAG, "Restart daemon: ");
            } catch (Exception e) {
                Log.e(TAG, "error " + e.getMessage());
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return (IBinder) startDaemonService;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        Log.d(TAG, "left" + left.toString());
        Log.d(TAG, "right" + right.toString());
        super.onCreate();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Toast.makeText(ScheduleService.this, "Schedule onCreate...", Toast.LENGTH_SHORT).show();
        TimerTask taskPunchIn = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Timer Connect Wifi ");
                Timestamp now = new Timestamp(System.currentTimeMillis());
//                if (now.after(left) && now.before(right)) {
//                    return;
//                }
                Log.d(TAG, "getTimeNow: " + new java.sql.Timestamp(System.currentTimeMillis()).toString());

                String time = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());

                Log.d(TAG, "now time: " + time);
                if (null == time || "".equals(time)) {
                    return;
                }
                int timeInt = Integer.parseInt(time);
                Log.d(TAG, "timeInt: " + timeInt);
                if (timeInt < 7 || timeInt > 22) {
                    return;
                }

                wifiManager.setWifiEnabled(true);
                List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
                if (null == wifiConfigurations || 0 == wifiConfigurations.size()) {
                    return;
                }
                WifiConfiguration configuration;
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

                    return;
                }
                wifiManager.enableNetwork(configuration.networkId, true);
            }
        };

        TimerTask taskPunchOut = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Timer Disconnect Wifi ");
                wifiManager.setWifiEnabled(false);
            }
        };

        Timer timerPunchIn = new Timer(true);

        timerPunchIn.schedule(taskPunchIn, 10 * 1000, 4 * 1000);

        Timer timerPunchOut = new Timer(true);

        timerPunchOut.schedule(taskPunchOut, 10 * 1000, 13 * 1000);

        keepDaemonService();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory");
        Toast.makeText(getBaseContext(), "Schedule onTrimMemory..." + level, Toast.LENGTH_SHORT)
                .show();
        keepDaemonService();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

}
