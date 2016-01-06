package org.bitnp.netcheckin2.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";
    private static boolean notif0_cleared = false;

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
                    .setLocalOnly(true)
                    .setContentIntent(pd)
                    .setSmallIcon(R.mipmap.logo);
            //.setContentIntent(PendingIntent.getActivity(context,1,new Intent(context, MainActivity.class),Intent.));

            if(content.getBytes().length > 32){
                mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content));
                // Make it collapsible
            }

            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notif0_cleared = true; // Do what you want here
            context.unregisterReceiver(this);
        }
    };

    // This is showing balance
    public void sendQuietNotification(Context context, String title, String content, boolean update){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(context, MainActivity.class);

            if(update){
                if(notif0_cleared){
                   // don't continue
                    return;
                }
            }else{
                notif0_cleared = false;
            }

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pd = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent dIntent = new Intent(NOTIFICATION_DELETED_ACTION);
            PendingIntent dPendingIntent = PendingIntent.getBroadcast(context, 0, dIntent, 0);
            context.registerReceiver(receiver, new IntentFilter(NOTIFICATION_DELETED_ACTION));

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setTicker(title)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setLocalOnly(true)
                    .setContentIntent(pd)
                    .setDeleteIntent(dPendingIntent)
                    .setSmallIcon(R.mipmap.logo);

            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }

    // Send notification that starts relogin service
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
                    .setLocalOnly(true)
                    .setContentIntent(pProIntent)
                    .setSmallIcon(R.mipmap.logo);
            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelNotification() {
        mNotificationManager.cancel(0);
    }
}
