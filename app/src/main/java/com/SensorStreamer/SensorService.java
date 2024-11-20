package com.SensorStreamer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/**
 * 用于保活的前台通知
 * @author chen
 * @version 1.0
 */
public class SensorService extends Service {
    private static final String CHANNEL_ID = "sensor_streamer_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());

        return START_STICKY;
    }

//    创建通知栏
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Sensor Streamer")
                .setContentText("Prepare for stream sensors data...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        创建通知渠道
        CharSequence name = "Sensor Streamer";
        String description = "Channel for sensor streaming";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
//        渠道
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
//        管理者
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
