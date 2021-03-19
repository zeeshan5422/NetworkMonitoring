package com.zues.netstat.health;

import androidx.annotation.NonNull;

import com.zues.netstat.dm.HealthOptions;

public class HealthManager {

    public static final int UNIT_SECOND = 1000;

    private final String url;
    private boolean cancelled = false;
    private final HealthOptions healthOptions = new HealthOptions();
    Thread thread = null;

    private HealthManager(@NonNull String url) {
        this.url = url;
    }

    public static HealthManager onUrl(@NonNull String url) {
        return new HealthManager(url);
    }

    /**
     * Set the timeout
     *
     * @param timeOutMillis - the timeout for each request in milliseconds
     * @return this object to allow chaining
     */
    public HealthManager setTimeOutMillis(int timeOutMillis) {
        if (timeOutMillis < UNIT_SECOND)
            throw new IllegalArgumentException("Times cannot be less than " + UNIT_SECOND);
        healthOptions.setTimeoutMillis(timeOutMillis);
        return this;
    }

    public HealthManager checkHealth(@NonNull HealthListener listener) {
        cancelled = false;

        thread = new Thread(() -> {
            HealthStats healthStats = HealthTools.startCheckingHealth(url, healthOptions);
            if (listener != null) {
                listener.onResult(healthStats);
            }
        });
        thread.start();
        return this;
    }

    /**
     * Cancel a running call
     */
    public void cancel() {
        if (thread != null) {
            thread.interrupt();
        }
        this.cancelled = true;
    }

    /**
     * enable the progress dialog
     */
    public <T extends DialogExecutor> HealthManager setProgressExecutor(@NonNull T progressExecutor) {
        healthOptions.setProgressExecutor(progressExecutor);
        return this;
    }

    /**
     * Set customer Health Api Caller.
     *
     * @param healthCallExecutor - override base api caller, and call api by provided interface
     * @return this object to allow chaining
     */
    public <T extends HealthCallExecutor> HealthManager setCallExecutor(@NonNull T healthCallExecutor) {
        healthOptions.setCallExecutor(healthCallExecutor);
        return this;
    }

    public interface HealthListener {
        void onResult(HealthStats stats);
    }

}
