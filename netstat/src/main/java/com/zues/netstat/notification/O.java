package com.zues.netstat.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.zues.netstat.R;
import com.zues.netstat.service.NetworkStrengthService;


public class O {

    private static final int ONGOING_NOTIFICATION_ID = 105;

    private static final int SMALL_ICON = R.drawable.ic_adjust_black_24dp;
    private static final int STOP_ACTION_ICON = R.drawable.ic_stop_black_24dp;

    @TargetApi(Build.VERSION_CODES.O)
    public static void createNotification(Service context) {
        String channelId = createChannel(context);
        Notification notification = buildNotification(context, channelId);
        context.startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static Notification buildNotification(Service context, String channelId) {
        // Create Pending Intents.
        PendingIntent piLaunchMainActivity = getLaunchActivityPI(context);
        PendingIntent piStopService = getStopServicePI(context);

        // Action to stop the service.
        Notification.Action stopAction = new Notification.Action.Builder(STOP_ACTION_ICON, getNotificationStopActionText(context), piStopService).build();

        // Create a notification.
        return new Notification.Builder(context, channelId)
                .setContentTitle(getNotificationTitle(context))
                .setContentText(getNotificationContent(context))
                .setSmallIcon(SMALL_ICON)
                .setContentIntent(piLaunchMainActivity)
                .setActions(stopAction)
                .setStyle(new Notification.BigTextStyle())
                .build();
    }

    private static CharSequence getNotificationContent(Service context) {
        return context.getString(R.string.service_content);
    }

    private static CharSequence getNotificationTitle(Service context) {
        return context.getString(R.string.service_title);
    }

    private static CharSequence getNotificationStopActionText(Service context) {
        return context.getString(R.string.stop_action_txt);
    }

    private static PendingIntent getStopServicePI(Service context) {

        Intent intent = new Intent(context, NetworkStrengthService.class);
        intent.putExtra(NetworkStrengthService.COMMAND_KEY, NetworkStrengthService.COMMAND_STOP);

        return PendingIntent.getService(context, 0, intent, 0);
    }

    private static PendingIntent getLaunchActivityPI(Service context) {

        Intent intent = null; // TODO :: new Intent(context, MainActivity.class);

        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    private static String createChannel(Service ctx) {
        // Create a channel.

        String channelId = ctx.getString(R.string.persistence_notification_channel_id);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence channelName = "Background Ping Service";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);

        notificationManager.createNotificationChannel(notificationChannel);
        return channelId;
    }
}
