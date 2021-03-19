package com.zues.netstat.dm;

import com.zues.netstat.health.DialogExecutor;
import com.zues.netstat.health.HealthCallExecutor;
import com.zues.netstat.health.HealthCallExecutorOkHttpImpl;
import com.zues.netstat.health.HealthManager;

public class HealthOptions {
    private int timeoutMillis;
    private int timeToLive;
    private DialogExecutor progressExecutor;

    private HealthCallExecutor healthCallExecutor = new HealthCallExecutorOkHttpImpl();

    public HealthOptions() {
        timeToLive = 128;
        timeoutMillis = HealthManager.UNIT_SECOND * 60;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = Math.max(timeoutMillis, HealthManager.UNIT_SECOND);
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = Math.max(timeToLive, 1);
    }


    public HealthCallExecutor getHealthCallExecutor() {
        if (healthCallExecutor == null) {
            healthCallExecutor = new HealthCallExecutorOkHttpImpl();
        }
        return healthCallExecutor;
    }

    public<T extends HealthCallExecutor>  void setCallExecutor(T healthCallExecutor) {
        this.healthCallExecutor = healthCallExecutor;
    }

    public DialogExecutor getProgressExecutor() {
        return progressExecutor;
    }

    public void setProgressExecutor(DialogExecutor progressExecutor) {
        this.progressExecutor = progressExecutor;
    }
}
