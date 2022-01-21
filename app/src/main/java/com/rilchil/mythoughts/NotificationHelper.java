package com.rilchil.mythoughts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private Context mContext;

    NotificationHelper(Context context) {
        mContext = context;
    }

    public void createNotification(){

        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(mContext, SettingsFragment.NOTIFICATION_CHANNEL);
        builder.setContentTitle("Reminder");
        builder.setContentText("Don't forget today's entry!");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(SettingsFragment.NOTIFICATION_CHANNEL,
                    "Reminder", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        assert notificationManager != null;
        notificationManager.notify(1, builder.build());

    }
}
