package com.google.firebase.codelab.friendlychat;

import android.app.job.JobParameters;
import android.util.Log;

import com.firebase.jobdispatcher.JobService;

public class PushNotificationJobService extends JobService {

    private static final String TAG = "MyFMService";

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        Log.d(TAG, "PushNotificationJobService:  Performing long running task in scheduled job");
        // TODO(developer): add long running task here.
        return false;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false;
    }
}
