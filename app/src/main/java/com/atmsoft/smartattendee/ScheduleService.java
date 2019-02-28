package com.atmsoft.smartattendee;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class ScheduleService extends Service {
    private static WifiManager wifiManager = null;
    public static String TARGET_SSID = MainActivity.HW_SSID;


    private String TAG = getClass().getName();
    private static String TaskTag = "TaskTag";

    private String Process_Name = "com.atmsoft.smartattendee.DaemonService:serviceDaemon";
    private Timestamp left = new Timestamp(119, 1, 3, 22, 0, 0, 0);
    private Timestamp right = new Timestamp(119, 1, 11, 6, 0, 0, 0);
    private static PunchInStatus status = PunchInStatus.NEVER;
    private static TimerTask taskPunchIn = new TimerTask() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            Log.d(TaskTag, "Timer Connect Wifi ");
            Timestamp now = new Timestamp(System.currentTimeMillis());
//                if (now.after(left) && now.before(right)) {
//                    return;
//                }
            Log.d(TaskTag, "getTimeNow: " + new java.sql.Timestamp(System.currentTimeMillis()).toString());

            String time = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
            Log.d(TaskTag, "now time: " + time);
            if (null == time || "".equals(time)) {
                return;
            }
            int timeInt = Integer.parseInt(time);
            Log.d(TaskTag, "timeInt: " + timeInt);


            switch (status) {

                case PUNCH_IN:
                    if (timeInt < 18) {
                        return;
                    }
                    if (Utils.SendPunch(wifiManager, TARGET_SSID)) {
                        status = PunchInStatus.PUNCH_OUT;
                    }
                    return;
                case PUNCH_OUT:
                    if (timeInt == 7) {
                        status = PunchInStatus.NEVER;
                        return;
                    }

                    return;
                case NEVER:
                default:
                    if (Utils.SendPunch(wifiManager, TARGET_SSID)) {
                        status = PunchInStatus.PUNCH_IN;
                    }
                    return;

            }


        }
    };


    static {
        Timer timerPunchIn = new Timer(true);

        timerPunchIn.schedule(taskPunchIn, 10 * 1000, 60 * 1000);


    }

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
