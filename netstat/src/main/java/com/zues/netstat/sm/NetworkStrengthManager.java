package com.zues.netstat.sm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.zues.netstat.dm.SignalStats;
import com.zues.netstat.service.NetworkStrengthService;
import com.zues.netstat.utils.NetworkUtils;

public class NetworkStrengthManager implements INetworkStrength {

    public static final String TAG = NetworkStrengthManager.class.getName();

    private int mInterval = 2000;

    private Handler mHandler;
    private Context mContext;
    private Thread mThread;
    private TelephonyManager mTelephonyManager;

    private volatile boolean mRunning = false;

    public NetworkStrengthManager(Context context) {
        mContext = context;
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void start() {

        mThread = new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                mHandler.post(mSignalRunnable);

                Looper.loop();
            }
        });
        mThread.setName("Thread-StrengthMetering");
        mThread.start();
        mRunning = true;

        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
//        mHandler.post(mSignalRunnable);
    }

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//            Log.d(TAG, TAG + " dbm: " + signalStrength.getDbm(signalStrength) + ",  level: " + signalStrength.getLevel());
        }
    };


    private final Runnable mSignalRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning()) {

//                getStrengthInDecibles(mContext);

                SignalStats stats = NetworkUtils.getConnectionSignalStats(mContext);
                Intent intent = new Intent(NetworkStrengthService.ACTION_SIGNAL_STATS);
                intent.putExtra(NetworkStrengthService.KEY_SIGNAL_STATS, stats);
                mContext.sendBroadcast(intent);

                mHandler.postDelayed(this, mInterval);
            }
        }
    };

    @Override
    public void stop() {
        mRunning = false;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mTelephonyManager != null){
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        mContext = null;
        mTelephonyManager = null;
        mPhoneStateListener = null;
        if (mThread != null) {
            mThread.interrupt();
        }
        mThread = null;
        Log.d(TAG, "service is stop in thread :  " + Thread.currentThread().getName());
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getStrengthInDecibles(Context context) {
        Log.i(TAG, Thread.currentThread().getName() + " getMobileNetworkSpeed : " + NetworkUtils.getMobileNetworkSpeed(mContext));
//        Log.i(TAG, Thread.currentThread().getName() + " Connection Type : " + NetworkUtils.getFormattedConnectionDetails(mContext));
    }

}
