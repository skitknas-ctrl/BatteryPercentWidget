package se.dick.batterywidget;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

/** Periodisk uppdatering (var 15:e minut, systemets minimum) utöver widgetens egen 30-minutersintervall. */
public class RefreshJob extends JobService {

    private static final int JOB_ID = 1;

    public static void schedule(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(context, RefreshJob.class))
                .setPeriodic(JobInfo.getMinPeriodMillis())
                .setPersisted(true)
                .build();
        scheduler.schedule(job);
    }

    public static void cancel(Context context) {
        context.getSystemService(JobScheduler.class).cancel(JOB_ID);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        BatteryWidget.updateAll(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
