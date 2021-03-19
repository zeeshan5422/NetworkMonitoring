package com.zues.netstat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.zues.netstat.sm.INetworkStrength;
import com.zues.netstat.sm.NetworkStrengthManager;

public class NetworkStrengthService extends Service {

    public static final String TAG = NetworkStrengthService.class.getName();

    public static final String ACTION_SIGNAL_STATS = "SignalStats";
    public static final String KEY_SIGNAL_STATS = "stats";
    public static final String KEY_INTERVAL = "KEY_interval_command";


    public static final String SERVICE_STATUS_BROADCAST_ACTION = "service_status_action";
    private static final String MESSAGE_KEY = "message_key";

    public static final String COMMAND_KEY = "command";
    public static final int COMMAND_START = 1;
    public static final int COMMAND_STOP = 2;

    // TODO :: update toggle state in on application on create
    public static boolean mServiceIsStarted = false;

    private INetworkStrength networkStrength;

    @Override
    public void onCreate() {
        super.onCreate();
        networkStrength = new NetworkStrengthManager(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        routeIntentCommand(intent);
        return START_STICKY;
    }

    private boolean containsCommand(Intent intent) {
        return intent.getExtras() != null && intent.getExtras().containsKey(COMMAND_KEY);
    }


    private void routeIntentCommand(Intent intent) {
        if (intent != null) {
            if (containsCommand(intent)) {
                processCommand(intent.getExtras().getInt(COMMAND_KEY));
            }
        }
    }


    private void processCommand(int command) {
        if (command == COMMAND_START) {
            commandStart();
        } else if (command == COMMAND_STOP) {
            mServiceIsStarted = false;
            stopSelf();
        }
    }

    private void commandStart() {
        mServiceIsStarted = true;

        // TODO :: get interval and send in start params.
        networkStrength.start();
        broadcastServiceStartMessage();
    }

    private void commandStop() {

    }

    private void broadcastServiceStopMessage() {
        Intent intent = new Intent();
        intent.setAction(SERVICE_STATUS_BROADCAST_ACTION);
        intent.putExtra(MESSAGE_KEY, COMMAND_STOP);
        sendBroadcast(intent);
    }

    private void broadcastServiceStartMessage() {
        Intent intent = new Intent();
        intent.setAction(SERVICE_STATUS_BROADCAST_ACTION);
        intent.putExtra(MESSAGE_KEY, COMMAND_START);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        commandStop();
        networkStrength.stop();
        broadcastServiceStopMessage();
        Log.d(TAG, "service onDestroy");
    }
}
