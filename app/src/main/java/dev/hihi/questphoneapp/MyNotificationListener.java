package dev.hihi.questphoneapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

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
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                sBluetoothChatService.start();
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationPosted: " + sbn.getPackageName());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationRemoved: " + sbn.getPackageName());
    }
}