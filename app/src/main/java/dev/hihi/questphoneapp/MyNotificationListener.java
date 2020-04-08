package dev.hihi.questphoneapp;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class MyNotificationListener extends NotificationListenerService {

    private static final String TAG = "MyNotificationListener";
    public static BluetoothChatService sBluetoothChatService = null;

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.i(TAG, "STATE_CONNECTED");
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Log.i(TAG, "STATE_CONNECTING");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Log.i(TAG, "STATE_LISTEN or STATE_NONE");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.i(TAG, "writeMessage: " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.i(TAG, "readMessage: " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Log.i(TAG, "MESSAGE_DEVICE_NAME: " + deviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(MyNotificationListener.this, msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (sBluetoothChatService == null) {
            sBluetoothChatService = new BluetoothChatService(this, mHandler);
            new Thread() {
                public void run() {
                    while (true) {
                        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                            sBluetoothChatService.start();
                            return;
                        }
                        SystemClock.sleep(3000);
                    }
                }
            }.start();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String title = getTitle(sbn.getNotification());
        String text = getText(sbn.getNotification());
        Log.d(TAG, "extra: " + sbn.getNotification().extras);
        int id = sbn.getId();
        String tag = sbn.getTag();

        if ((sbn.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) != 0) {
            Log.d(TAG, "Ignore the notification FLAG_GROUP_SUMMARY");
            return;
        }

        Log.i(TAG, "onNotificationPosted: " + packageName);
        Set<String> blacklist = BlacklistUtils.getBlacklist(this);
        if (blacklist.contains(packageName)) {
            Log.i(TAG, "Skip package " + packageName + " in blacklist");
            return;
        }
        if (BlacklistUtils.hasBlacklistKeywords(text)) {
            Log.i(TAG, "Skip text " + text + " in blacklist");
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("action", "post");
            json.put("package_name", packageName);
            json.put("title", title);
            json.put("text", text);
            json.put("id", id);
            json.put("tag", tag);
            Log.i(TAG, json.toString());
            sendMessage(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getTitle(Notification notification) {
        return notification.extras.getString(Notification.EXTRA_TITLE);
    }

    private String getText(Notification notification) {
        CharSequence message = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (message == null)
            message = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        if (message == null)
            message = notification.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
        return (message == null) ? "" : message.toString();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationRemoved: " + sbn.getPackageName());
        String packageName = sbn.getPackageName();
        int id = sbn.getId();
        String tag = sbn.getTag();

        JSONObject json = new JSONObject();
        try {
            json.put("action", "remove");
            json.put("package_name", packageName);
            json.put("id", id);
            json.put("tag", tag);
            Log.i(TAG, json.toString());
            sendMessage(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (sBluetoothChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Log.i(TAG, "no connected, skip send msg: "+ message);
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            sBluetoothChatService.write(send);
            Log.i(TAG, "Msg sent: " + send);
        }
    }

}