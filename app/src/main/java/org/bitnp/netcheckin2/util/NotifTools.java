package org.bitnp.netcheckin2.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.bitnp.netcheckin2.R;
import org.bitnp.netcheckin2.service.LoginService;
import org.bitnp.netcheckin2.ui.MainActivity;

/**
 * Created by ental_000 on 2015/3/18.
 */
public class NotifTools {
    private static NotificationManager mNotificationManager;
    private static NotifTools instance;

    private NotifTools(){}

    public static NotifTools getInstance(Context context){
        if(instance == null){
            instance = new NotifTools();
            mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return instance;
    }

    // open MainAcitivity when clicked
    public void sendSimpleNotification(Context context, String title, String content){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(context, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pd = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setTicker(title)
                    .setContentIntent(pd)
                    .setSmallIcon(R.mipmap.logo);
            //.setContentIntent(PendingIntent.getActivity(context,1,new Intent(context, MainActivity.class),Intent.));
            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * isToService param is replaced by the new function name
     * */
    public void sendSimpleNotificationAndReLogin(Context context, String title, String content){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent proIntent = new Intent(context, LoginService.class);
            proIntent.putExtra("command", LoginService.COMMAND_RE_LOGIN);
            PendingIntent pProIntent = PendingIntent.getService(context, 0, proIntent, 0);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setTicker(title)
                    .setContentIntent(pProIntent)
                    .setSmallIcon(R.mipmap.logo);
            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }


    public void sendButtonNotification(Context context, String title, String content){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            Intent proIntent = new Intent(context, LoginService.class);
            Intent conIntent = new Intent(context, MainActivity.class);

            proIntent.putExtra("command", LoginService.COMMAND_RE_LOGIN);
            PendingIntent pProIntent = PendingIntent.getService(context, 0, proIntent, 0);
            PendingIntent pConIntent = PendingIntent.getActivity(context, 0, conIntent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.logo)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setTicker(content)
                    .addAction(R.drawable.ic_action_ok, "好", pProIntent)
                    .addAction(R.drawable.ic_action_no, "不", pConIntent);

            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationManager.cancel(0);
        } else {
            // As toast will close itself, forget it
        }
    }
}
