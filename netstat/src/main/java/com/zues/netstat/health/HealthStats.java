
package com.zues.netstat.health;

import okhttp3.Response;

public class HealthStats {

    private final String url;

    private boolean isReachable = false;
    private long responseTimeInMillis;
    private String error = null;
    private Response response = null;

    public HealthStats(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public boolean isReachable() {
        return isReachable;
    }

    public void setReachable(boolean reachable) {
        isReachable = reachable;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public long getResponseTimeInMillis() {
        return responseTimeInMillis;
    }

    public void setResponseTimeInMillis(long responseTimeInMillis) {
        this.responseTimeInMillis = responseTimeInMillis;
    }


    @Override
    public String toString() {
        return "HealthStats{" +
                "url='" + url + '\'' +
                ", isReachable=" + isReachable +
                ", responseTimeInMillis=" + responseTimeInMillis +
                ", error='" + error + '\'' +
                ", response=" + response +
                '}';
    }
}
