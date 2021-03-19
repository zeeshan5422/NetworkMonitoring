package com.zues.netstat.health;

import android.util.Log;

import androidx.annotation.NonNull;

import com.zues.netstat.BuildConfig;
import com.zues.netstat.dm.HealthOptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Response;

public class HealthTools {
    public static final String TAG = HealthTools.class.getName();

    // This class is not to be instantiated
    private HealthTools() {
    }

    /**
     * Perform a server Heath Check using the Okhttp
     *
     * @param url           - full url to call
     * @param healthOptions - ping command options
     * @return - the health result
     */
    @NotNull
    public static HealthStats startCheckingHealth(@NonNull String url, @NonNull HealthOptions healthOptions) {
        try {
            if (healthOptions.getProgressExecutor() != null) {
                healthOptions.getProgressExecutor().show();
            }
            return HealthTools.check(url, healthOptions);
        } catch (IOException e) {
            HealthStats stats = new HealthStats(url);
            if (e instanceof SocketTimeoutException) {
                stats.setError("Time out | Server is unreachable");
            } else {
                stats.setError(e.getLocalizedMessage());
            }
            stats.setReachable(false);
            return stats;
        } catch (Exception e) {
            HealthStats stats = new HealthStats(url);
            stats.setReachable(false);
            stats.setError(e.getMessage());
            return stats;
        } finally {
            // hide loading
            Log.d(TAG, " startCheckingHealth >> finally");
            if (healthOptions.getProgressExecutor() != null) {
                healthOptions.getProgressExecutor().hide();
            }
        }
    }

    @NotNull
    private static HealthStats check(String url, @NotNull HealthOptions healthOptions) throws IOException, InterruptedException {

        Response response = healthOptions.getHealthCallExecutor().execute(url);
        long estimatedTime = calculateDifference(response.sentRequestAtMillis(), response.receivedResponseAtMillis());
        HealthStats stats = new HealthStats(url);
        stats.setReachable(response.isSuccessful());
        stats.setResponse(response);
        stats.setResponseTimeInMillis(estimatedTime);
        return stats;
    }

    private static long calculateDifference(long startTime, long endTime) {
        return endTime - startTime;
    }
}
