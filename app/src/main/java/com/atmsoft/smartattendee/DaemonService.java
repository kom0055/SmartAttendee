package com.atmsoft.smartattendee;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class DaemonService extends Service {

    private String TAG = getClass().getName();

    private String Process_Name = "com.atmsoft.smartattendee.ScheduleService:serviceSchedule";
    private static final String RESTART_SCHEDULE_SERVICE = "Restart Schedule Service";


    private StrongService startScheduleService = new StrongService.Stub() {
        @Override
        public void startService() {
            Intent i = new Intent(getBaseContext(), ScheduleService.class);
            getBaseContext().startService(i);
        }

        @Override
        public void stopService() {
            Intent i = new Intent(getBaseContext(), ScheduleService.class);
            getBaseContext().stopService(i);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void keepScheduleService() {
        Log.d(TAG, "keepScheduleService");
        boolean isRun = Utils.isProcessRunning(DaemonService.this, Process_Name);
        if (!isRun) {
            try {
                Toast.makeText(getBaseContext(), RESTART_SCHEDULE_SERVICE, Toast.LENGTH_SHORT).show();
                startScheduleService.startService();
                Log.d(TAG, "Restart schedule: ");
            } catch (Exception e) {

            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return (IBinder) startScheduleService;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory");
        Toast.makeText(getBaseContext(), "Daemon Service onTrimMemory..." + level, Toast.LENGTH_SHORT).show();
        keepScheduleService();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        Toast.makeText(DaemonService.this, "DaemonService Start", Toast.LENGTH_SHORT).show();
        keepScheduleService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
