package com.zues.netstat.health;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HealthCallExecutorOkHttpImpl implements HealthCallExecutor {
    public static final String TAG = HealthCallExecutorOkHttpImpl.class.getName();

    @Override
    public Response execute(@NonNull String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Log.d(TAG, " execute >> start");

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response result = client.newCall(request).execute();
        Log.d(TAG, " execute >> end");

        return result;
    }
}
