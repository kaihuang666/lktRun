package com.kai.lktMode;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoService extends Service {
    private ScreenReceiver receiver;
    private OrientationReciver orientationReciver;
    private static int SERVICE_ID = 0;
    public AutoService() {
    }


    public static Handler msgHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, ProtectService.class);
            startService(innerIntent);
            startForeground(SERVICE_ID, new Notification());
        }
        String action=intent==null?null:intent.getAction();
        if (action==null){
            if ((Boolean)Preference.get(AutoService.this,"autoLock","Boolean"))
                registerScreenActionReceiver();
            if ((Boolean)Preference.get(AutoService.this,"gameMode","Boolean"))
                regisrerOrentationReceiver();
        }else
            switch (action){
                case "gameOn":regisrerOrentationReceiver();break;
                case "gameOff":unregisrerOrentationReceiver();break;
                case "lockOn":registerScreenActionReceiver();break;
                case "lockOff":unregisterScreenActionReceiver();break;
                case "reset":
                    if ((Boolean)Preference.get(AutoService.this,"autoLock","Boolean")) {
                        unregisterScreenActionReceiver();
                        registerScreenActionReceiver();
                    }
                    if ((Boolean)Preference.get(AutoService.this,"gameMode","Boolean")){
                        unregisrerOrentationReceiver();
                        regisrerOrentationReceiver();
                    }

                    break;
            }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void regisrerOrentationReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        orientationReciver=new OrientationReciver();
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        registerReceiver(orientationReciver, intentFilter);
        Log.d("游戏监听","On");
    }
    private void unregisrerOrentationReceiver(){
        try {
            unregisterReceiver(orientationReciver);
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d("游戏监听","Off");

    }

    private void registerScreenActionReceiver() {
        final IntentFilter filter = new IntentFilter();
        receiver = new ScreenReceiver();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
        Log.d("锁屏监听","On");
    }

    private void unregisterScreenActionReceiver() {
        Log.d("锁屏监听","Off");
        try {
            unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private class OrientationReciver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (!(Boolean)Preference.get(context,"gameMode","Boolean"))
                return;
            Configuration mConfiguration = context.getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == 2) {
                //为了获取最顶层此处休眠1.5秒
                try{
                    Thread.sleep(1500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (!getTopApp(context,2).isEmpty()){
                    Toast.makeText(context,"游戏加速开启",Toast.LENGTH_SHORT).show();
                    runWithDelay(new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            if (((String)Preference.get(context,"code6","String")).isEmpty()){
                                readMode(3,context);
                                setMode(context,3);
                            }else {
                                readMode(6,context);
                                setMode(context,6);
                            }
                            Log.d("game","on");
                            super.handleMessage(msg);
                        }
                    },10000);
                }
            }
            if (ori == 1) {
                if (getTopApp(context,2).isEmpty())
                    return;
                Toast.makeText(context,"游戏加速关闭",Toast.LENGTH_SHORT).show();
                runWithDelay(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.d("game","off");
                        readMode((int) Preference.get(context, "default", "int")+1,context);
                        setMode(context,(int) Preference.get(context, "default", "int")+1);
                        super.handleMessage(msg);
                    }
                },5000);
            }
        }
    }
    public static void runWithDelay(Handler handler,long l){
        msgHandler.removeMessages(2);
        msgHandler = handler;
        msgHandler.sendEmptyMessageDelayed(2,l);
    }
    private String getTopApp(Context context,int i) {
        Log.d("秒数",String.valueOf(i));
        String packageName = "";
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
                List<ActivityManager.RunningTaskInfo> rti = activityManager.getRunningTasks(1);
                packageName = rti.get(0).topActivity.getPackageName();
            } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
                if (processes.size() == 0) {
                    return packageName;
                }
                for (ActivityManager.RunningAppProcessInfo process : processes) {
                    if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return process.processName;
                    }
                }
            } else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
                final long end = System.currentTimeMillis()+1000;
                final UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService( Context.USAGE_STATS_SERVICE);
                final UsageEvents events = usageStatsManager.queryEvents((end - i * 1000-2000), end);
                UsageEvents.Event usageEvent = new UsageEvents.Event();
                while (events.hasNextEvent()) {
                    events.getNextEvent(usageEvent);
                    Log.d("ac",usageEvent.getPackageName()+"   "+usageEvent.getEventType());
                    if (!Preference.getGames(context).contains(usageEvent.getPackageName())) {
                        continue;
                    }else {
                        return usageEvent.getPackageName();
                    }
                }

            }
        }catch (Exception ignored){
        }
        return "";
    }
    class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {

            if (!(Boolean)Preference.get(context,"autoLock","Boolean"))
                return;
            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                runWithDelay(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.d("解锁","success");
                        readMode((int) Preference.get(context, "default", "int")+1,context);
                        setMode(context,(int) Preference.get(context, "default", "int")+1);
                        super.handleMessage(msg);
                    }
                },5000);

            }
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                runWithDelay(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.d("上锁","success");
                        if (((String)Preference.get(context,"code5","String")).isEmpty()){
                            readMode(1,context);
                            setMode(context,1);
                        }else {
                            readMode(5,context);
                            setMode(context,5);
                        }

                        super.handleMessage(msg);
                    }
                },(int)Preference.get(context,"sleepDelay",200)*1000);

            }
        }




    }
    private void setMode(Context c,int i){
        if (i==5){
            Preference.save(c,"customMode",1);
        }else if(i==6){
            Preference.save(c,"customMode",3);
        }
        else {
            Preference.save(c, "customMode", i);
        }
    }
    private void readMode(final int i,final Context c){
        if ((Boolean) Preference.get(c,"custom","Boolean")){
            int now=(int)Preference.get(this,"customMode","int");
            if (now==i){
                return;
            }

            Intent serviceIntent;
            serviceIntent = new Intent(c, CustomCommandService.class);
            serviceIntent.putExtra("mode", i);
            serviceIntent.putExtra("isShow", false);
            c.startService(serviceIntent);
            return;
        }
        try{
            Shell shell= RootTools.getShell(true);
            shell.add(new Command(1,"grep PROFILE /data/LKT.prop"){
                @Override
                public void commandOutput(int id, String line) {
                    super.commandOutput(id, line);
                    int now=cutMode(line);
                    if (now==i){
                        return;
                    }
                    Intent serviceIntent = new Intent(c, CommandService.class);
                    serviceIntent.putExtra("mode", i);
                    serviceIntent.putExtra("isShow", false);
                    c.startService(serviceIntent);
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    super.commandCompleted(id, exitcode);
                    if (exitcode!=0){
                        Intent serviceIntent = new Intent(c, CommandService.class);
                        int mode = (int) Preference.get(c, "default", "int");
                        serviceIntent.putExtra("mode", mode+1);
                        serviceIntent.putExtra("isShow", false);
                        c.startService(serviceIntent);
                    }
                }
            });

        }catch (IOException | TimeoutException | RootDeniedException e){
            e.printStackTrace();
        }
    }
    private int cutMode(String line){
        String pattern = "PROFILE\\s:\\s(\\S+)";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(line);
        if (m.find( )) {
            //Toast.makeText(getApplicationContext(),m.group(1),Toast.LENGTH_SHORT).show();
            switch (m.group(1)){
                case "Battery":return 1;
                case "Balanced":return 2;
                case "Performance":return 3;
                case "Turbo":return 4;
            }
        }
        return -1;

    }
}