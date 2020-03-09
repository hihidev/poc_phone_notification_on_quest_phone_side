package dev.hihi.questphoneapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class MyNotificationListener extends NotificationListenerService {

    private static final String TAG = "MyNotificationListener";

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Log.i(TAG, "onNotificationPosted: " + sbn.getPackageName());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Log.i(TAG, "onNotificationRemoved: " + sbn.getPackageName());
    }
}