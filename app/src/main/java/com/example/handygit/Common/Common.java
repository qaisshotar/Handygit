package com.example.handygit.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.ArraySet;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.example.handygit.Model.UserModel;
import com.example.handygit.Model.WorkerGeoModel;
import com.example.handygit.R;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Common {
    public static final String USER_INFO_REFERENCE ="Users" ;
    public static final String TOKEN_REFRENCE ="Token" ;
    public static final String WORKERS_LOCATION_REFERENCE = "WorkersLocation"; // same as worker app
    public static final String WORKER_INFO_REFERENCE ="WorkerInfo " ;
    public static UserModel currentUser;

    public static final String NOTI_TITLE="title";
    public static final String NOTI_CONTENT="body";
    public static Set<WorkerGeoModel> workersFound=new HashSet<WorkerGeoModel>();
    public static HashMap<String, Marker> markerList = new HashMap<>();

    public static String buildWelcomeMessage(){
        if (Common.currentUser !=null)
        {
            return new StringBuilder("Welcome")
                    .append(Common.currentUser.getFirstName())
                    .append("")
                    .append(Common.currentUser.getLastName()).toString();



        }
        else
            return "";

    }

    public static void showNotification(Context context, int id, String title, String body , Intent intent) {

        PendingIntent pendingIntent = null;
        if (intent !=null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID ="handy man bulid";
        NotificationManager notificationManager =(NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Handy Man",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Handy Man");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);


            notificationManager.createNotificationChannel(notificationChannel);


        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_baseline_directions_car_filled_24)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_baseline_directions_car_filled_24));

        if (pendingIntent != null)
        {
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id,notification);


    }

    public static String buildName(String firstName, String lastName) {
        return new StringBuilder(firstName).append("").append(lastName).toString();
    }


}
