package com.zues.netstat.health;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Response;

public interface HealthCallExecutor {

    Response execute(@NonNull String url) throws IOException;

}
