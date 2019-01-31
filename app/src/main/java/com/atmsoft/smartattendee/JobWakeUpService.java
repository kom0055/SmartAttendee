package com.atmsoft.smartattendee;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.util.Log;

import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobWakeUpService extends JobService {
    private int JobWakeUpId = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        JobInfo.Builder mJobBuilder = new JobInfo.Builder(
                JobWakeUpId, new ComponentName(this, JobWakeUpService.class)
        );
        mJobBuilder.setPeriodic(2000);
        mJobBuilder.setPersisted(true);
        mJobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.schedule(mJobBuilder.build());
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("JobWakeUpService: ", "Step In OnStartJob ");
        boolean messageServiceAlive = serviceAlive(ScheduleService.class.getName());

        if (!messageServiceAlive) {
            Log.d("JobWakeUpService: ", "OnStartJob StartService");
            startService(new Intent(this, ScheduleService.class));
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean serviceAlive(String serviceName) {

        ActivityManager myAM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);

        if (null == myList || myList.isEmpty()) {
            return false;
        }
        List<ActivityManager.RunningServiceInfo> rList = myList.stream().filter(p -> {
            String mName = p.service.getClassName().toString();
            return serviceName.equals(mName);


        }).collect(Collectors.toList());
        return !(null == rList || rList.isEmpty());
    }
}
