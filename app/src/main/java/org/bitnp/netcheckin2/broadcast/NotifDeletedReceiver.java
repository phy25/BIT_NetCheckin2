package org.bitnp.netcheckin2.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.bitnp.netcheckin2.service.LoginService;
import org.bitnp.netcheckin2.util.NotifTools;

/**
 * Created by Phy25 on 7/1/2016.
 */
public class NotifDeletedReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        NotifTools.setNotif0Cleared(true);
    }
};
