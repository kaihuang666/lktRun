package com.kai.lktMode.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.kai.lktMode.R;
import com.kai.lktMode.service.AutoService;
import com.kai.lktMode.service.CommandService;
import com.kai.lktMode.service.CustomCommandService;
import com.kai.lktMode.tool.Preference;

public class StartReceiver extends BroadcastReceiver {
    private String[] modes={"省电模式","均衡模式","游戏模式","极限模式"};
    @Override
    public void onReceive(Context context, Intent intent) {

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            if ( Preference.getBoolean(context,"autoBoot")) {
                Intent serviceIntent;
                if ( Preference.getBoolean(context,"custom")){
                    serviceIntent = new Intent(context, CustomCommandService.class);
                }else
                    serviceIntent=new Intent(context, CommandService.class);
                int mode =  Preference.getInt(context, "default");
                serviceIntent.putExtra("mode", mode + 1);
                serviceIntent.putExtra("isShow", false);
                context.startService(serviceIntent);
                requestNotification(context, mode);
            }
            if ((Boolean)Preference.getBoolean(context,"autoLock")){
                Intent intent1=new Intent(context, AutoService.class);
                context.startService(intent1);
            }
        }
    }
    private void requestNotification(Context context,int mode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "lkt_boot";
            String channelName = "开机启动";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
            showNotification(context,mode);
        }else {
            showNotificationApi23(context,mode);
        }

    }
    private void showNotification(Context context, int mode) {
        String channelId = "lkt_boot";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("已切换到默认模式:"+modes[mode])
                .build();
        notificationManager.notify(1, notification);

    }
    private void showNotificationApi23(Context context,int mode){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("已切换到默认模式:"+modes[mode])
                .build();
        notificationManager.notify(1, notification);

    }

}
