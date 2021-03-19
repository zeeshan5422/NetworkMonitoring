package com.zues.netstat.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.zues.netstat.R;
import com.zues.netstat.service.NetworkStrengthService;

public class PreO {

    private static final int ONGOING_NOTIFICATION_ID = 100;

    private static final int STOP_ACTION_ICON = R.drawable.ic_stop_black_24dp;
    private static final int SMALL_ICON = R.drawable.ic_adjust_black_24dp;

    public static void createNotification(Service context) {
        // Create Pending Intents.
        PendingIntent piLaunchMainActivity = getLaunchActivityPI(context);
        PendingIntent piStopService = getStopServicePI(context);

        // Action to stop the service.
        NotificationCompat.Action stopAction =
                new NotificationCompat.Action.Builder(
                        STOP_ACTION_ICON,
                        getNotificationStopActionText(context),
                        piStopService)
                        .build();

        // Create a notification.
        Notification mNotification =
                new NotificationCompat.Builder(context)
                        .setContentTitle(getNotificationTitle(context))
                        .setContentText(getNotificationContent(context))
                        .setSmallIcon(SMALL_ICON)
                        .setContentIntent(piLaunchMainActivity)
                        .addAction(stopAction)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .build();

        context.startForeground(
                ONGOING_NOTIFICATION_ID, mNotification);
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

    private static CharSequence getNotificationContent(Service context) {
        return context.getString(R.string.service_content);
    }

    private static CharSequence getNotificationTitle(Service context) {
        return context.getString(R.string.service_title);
    }

    private static CharSequence getNotificationStopActionText(Service context) {
        return context.getString(R.string.stop_action_txt);
    }

}
