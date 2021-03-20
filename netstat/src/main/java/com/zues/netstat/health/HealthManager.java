package com.zues.netstat.health;

import androidx.annotation.NonNull;

import com.zues.netstat.dm.HealthOptions;
import com.zues.netstat.ping.PingManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HealthManager {

    public static final int UNIT_SECOND = 1000;

    private final String url;
    private boolean cancelled = false;
    private final HealthOptions healthOptions = new HealthOptions();
    private Executor healthExecutor = Executors.newSingleThreadExecutor();

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

    /**
     * Set the Executor for execution of ping.
     * Note:: Please don't pass MainThread Executor, as it may block the UI Thread.
     *
     * @param healthExecutor - Executor for job execution
     * @return this object to allow chaining
     */
    public HealthManager setHealthExecutor(@NonNull Executor healthExecutor) {
        this.healthExecutor = healthExecutor;
        return this;
    }

    public HealthManager checkHealth(@NonNull HealthListener listener) {
        cancelled = false;
        if (healthExecutor == null) {
            throw new IllegalArgumentException("healthExecutor must not be null ");
        }
        healthExecutor.execute(() -> {
            HealthStats healthStats = HealthTools.startCheckingHealth(url, healthOptions);
            if (listener != null) {
                listener.onResult(healthStats);
            }
        });
        return this;
    }

    /**
     * Cancel a running call
     */
    public void cancel() {
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
