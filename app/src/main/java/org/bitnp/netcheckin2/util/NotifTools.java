package org.bitnp.netcheckin2.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.bitnp.netcheckin2.R;
import org.bitnp.netcheckin2.broadcast.NotifDeletedReceiver;
import org.bitnp.netcheckin2.service.LoginService;
import org.bitnp.netcheckin2.ui.MainActivity;

/**
 * Created by ental_000 on 2015/3/18.
 */
public class NotifTools {
    private static NotificationManager mNotificationManager;
    private static NotifTools instance;
    private static boolean notif0Cleared = false;
    private static Bitmap logoBM;

    private NotifTools(){}

    public static NotifTools getInstance(Context context){
        if(instance == null){
            instance = new NotifTools();
            mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return instance;
    }

    public static Bitmap getlogoBM(Context context){
        if(logoBM == null){
            logoBM = BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo);
        }
        return logoBM;
    }

    public static void setNotif0Cleared(boolean b){
        notif0Cleared = b;
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
                    .setLargeIcon(getlogoBM(context))
                    .setSmallIcon(R.mipmap.notification_icon);
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



    // This is showing balance
    public void sendQuietNotification(Context context, String title, String content, boolean update){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(context, MainActivity.class);

            if(update){
                if(notif0Cleared){
                   // Don't continue
                   return;
                }
            }else{
                notif0Cleared = false;
            }

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pd = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent dIntent = new Intent(context, NotifDeletedReceiver.class);
            PendingIntent dPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, dIntent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setTicker(title)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setLocalOnly(true)
                    .setContentIntent(pd)
                    .setDeleteIntent(dPendingIntent)
                    .setLargeIcon(getlogoBM(context))
                    .setSmallIcon(R.mipmap.notification_icon);

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
                    .setLargeIcon(getlogoBM(context))
                    .setSmallIcon(R.mipmap.notification_icon);
            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelNotification() {
        mNotificationManager.cancel(0);
    }
}
