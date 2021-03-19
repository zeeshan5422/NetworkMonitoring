package com.zues.netstat.health;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogExecutorImpl2 implements DialogExecutor {

    public static final String TAG = DialogExecutorImpl2.class.getName();

    private Dialog dialog;

    public DialogExecutorImpl2(Dialog dialog) {
        if (this.dialog == null) {
            this.dialog = dialog;
        }
    }

    @Override
    public void show() {
        if (dialog != null) {
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    @Override
    public void hide() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
