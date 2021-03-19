package com.zues.netstat.health;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogExecutorImpl implements DialogExecutor, Application.ActivityLifecycleCallbacks {

    public static final String TAG = DialogExecutorImpl.class.getName();

    private Activity activity;
    private Dialog dialog;
//    private static boolean isShowing = false;

    public DialogExecutorImpl(Activity activity) {
        if (this.activity == null) {
            this.activity = activity;
            activity.getApplication().registerActivityLifecycleCallbacks(this);
        }
    }

    @Override
    public void show() {
        if (activity != null) {
            activity.runOnUiThread(() -> {
//                isShowing = true;
                dialog = new ProgressDialog(activity);
                dialog.setCancelable(false);
                dialog.show();
            });
        }
    }

    @Override
    public void hide() {
//        isShowing = false;
//        if (activity != null) {
//            activity.runOnUiThread(() -> {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
//            });
//        }
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.d(TAG, "onActivityStarted");
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.d(TAG, "onActivityResumed");
        this.activity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.d(TAG, "onActivityPaused");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.d(TAG, "onActivityStopped");
        activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        this.activity = null;
        dialog = null;

//        isShowing = false;
//        if (dialog != null) {
//            dialog.dismiss();
//            dialog = null;
//        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        Log.d(TAG, "onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.d(TAG, "onActivityDestroyed");
        activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        this.activity = null;
    }
}
