package com.kai.lktMode.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.base.MyApplication;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.ServiceStatusUtils;
import com.kai.lktMode.fragment.MainFragment;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.Settings;
import com.kai.lktMode.tool.TransTool;
import com.kai.lktMode.tool.util.TopApp;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;
import com.xdandroid.hellodaemon.AbsWorkService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoService extends AbsWorkService {
    private ScreenReceiver receiver;
    private OrientationReciver orientationReciver;
    private StringBuilder builder=new StringBuilder();
    private TopApp topApp;
    public AutoService() {
    }

    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return false;
    }

    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        return null;
    }

    @Override
    protected int onStart(Intent intent, int flags, int startId) {
        return super.onStart(intent, flags, startId);
    }

    @Override
    protected void onEnd(Intent rootIntent) {
        super.onEnd(rootIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent, Void alwaysNull) {
        return null;
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        String action=intent==null?null:intent.getAction();
        if (action==null){
            if ((Boolean) Preference.getBoolean(AutoService.this,"autoLock"))
                registerScreenActionReceiver();
            if ((Boolean)Preference.getBoolean(AutoService.this,"gameMode"))
                regisrerOrentationReceiver();
        }else
            switch (action){
                case "gameOn":regisrerOrentationReceiver();break;
                case "gameOff":unregisrerOrentationReceiver();break;
                case "lockOn":registerScreenActionReceiver();break;
                case "lockOff":unregisterScreenActionReceiver();break;
                case "reset":
                    if ((Boolean)Preference.getBoolean(AutoService.this,"autoLock")) {
                        unregisterScreenActionReceiver();
                        registerScreenActionReceiver();
                    }
                    if ((Boolean)Preference.getBoolean(AutoService.this,"gameMode")){
                        unregisrerOrentationReceiver();
                        regisrerOrentationReceiver();
                    }

                    break;
            }
        try {
            topApp=TopApp.getInstance(AutoService.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stopWork(Intent intent, int flags, int startId) {
        unregisrerOrentationReceiver();
        unregisterScreenActionReceiver();
        Intent intent1 = new Intent("com.kai.lktMode.restart");
        // 兼容安卓8.0  参数为（应用包名，广播路径）,但是只局限于特定的应用能收到广播
        intent1.setComponent(new ComponentName(getApplication().getPackageName(),
                "com.kai.lktMode.receiver.AutoReceiver"));
        sendBroadcast(intent1);
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        Intent intent = new Intent("com.kai.lktMode.restart");
        // 兼容安卓8.0  参数为（应用包名，广播路径）,但是只局限于特定的应用能收到广播
        intent.setComponent(new ComponentName(getApplication().getPackageName(),
                "com.kai.lktMode.receiver.AutoReceiver"));
        sendBroadcast(intent);
    }


    public static Handler msgHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
        }
    };


    @Override
    public void onDestroy() {
        unregisrerOrentationReceiver();
        unregisterScreenActionReceiver();
        Intent intent1 = new Intent("com.kai.lktMode.restart");
        // 兼容安卓8.0  参数为（应用包名，广播路径）,但是只局限于特定的应用能收到广播
        intent1.setComponent(new ComponentName(getApplication().getPackageName(),
                "com.kai.lktMode.receiver.AutoReceiver"));
        sendBroadcast(intent1);
        super.onDestroy();

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
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
            if (!(Boolean)Preference.getBoolean(context,"gameMode"))
                return;
            Configuration mConfiguration = context.getResources().getConfiguration(); //获取设置的配置信息
            MyApplication myApplication=(MyApplication)getApplication();
            int ori = mConfiguration.orientation; //获取屏幕方向
            //Toast.makeText(context,ori+"",Toast.LENGTH_LONG).show();
            if (ori == 2) {
                //Toast.makeText(getApplicationContext(),getTopApp(context),Toast.LENGTH_LONG).show();
                if (topApp.enterGameMode()){
                    Toast.makeText(getApplicationContext(),"游戏加速开启",Toast.LENGTH_SHORT).show();
                    TransTool.run(getApplicationContext());
                    runWithDelay(new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            if (((String)Preference.getString(getApplicationContext(),"code6")).isEmpty()){
                                readMode(3,getApplicationContext());
                            }else {
                                readMode(6,getApplicationContext());
                            }
                            Log.d("game","on");
                            super.handleMessage(msg);
                        }
                    },(int)Preference.getInt(getApplicationContext(),"openGameDelay",5)*1000);
                }else {
                    Preference.saveBoolean(context,"inGameMode",false);
                }
            }
            if (ori == 1) {
                if (topApp.getTopAppByUsage(context).isEmpty())
                    return;
                Toast.makeText(getApplicationContext(),"游戏加速关闭",Toast.LENGTH_SHORT).show();
                TransTool.restore(AutoService.this);
                runWithDelay(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.d("game","off");
                        readMode((int) Preference.getInt(context, "default")+1,getApplicationContext());
                        super.handleMessage(msg);
                    }
                },(int)Preference.getInt(getApplicationContext(),"closeGameDelay",15)*1000);
            }
        }
    }
    public static void runWithDelay(Handler handler,long l){
        msgHandler.removeMessages(2);
        msgHandler = handler;
        msgHandler.sendEmptyMessageDelayed(2,l);
    }


    class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (!(Boolean)Preference.getBoolean(context,"autoLock"))
                return;
            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                runWithDelay(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.d("解锁","success");
                        readMode((int) Preference.getInt(getApplicationContext(), "default")+1,getApplicationContext());
                        setMode(getApplicationContext(),Preference.getInt(getApplicationContext(), "default")+1);
                        Settings settings=Settings.getInstance(AutoService.this,false);
                        for (Settings.Setting setting:settings.getItems()){
                            setting.command();
                        }
                        super.handleMessage(msg);
                    }
                },5000);

            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                runWithDelay(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {

                        Log.d("上锁","success");
                        MyApplication application=(MyApplication) getApplication();
                        if (application.getMainActivity()!=null){
                            //application.getMainActivity().finish();
                        }
                        if (((String)Preference.getString(getApplicationContext(),"code5")).isEmpty()){
                            readMode(1,getApplicationContext());

                        }else {
                            readMode(5,getApplicationContext());
                        }
                        builder=new StringBuilder();
                        for (String s:Preference.getSoftwares(getApplicationContext())){
                            addCommand("am force-stop "+s+"\n");
                            if (s.contains("com.tencent.mobileqq")){
                                Log.d("QQ","服务保活");
                                try {
                                    Thread.sleep(1000);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                addCommand("am startservice -n com.tencent.mobileqq/.msf.service.MsfService\n");
                            }
                            if (s.contains("com.tencent.tim")){
                                Log.d("TIM","服务保活");
                                try {
                                    Thread.sleep(1000);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                addCommand("am startservice -n com.tencent.tim/.msf.service.MsfService\n");
                            }
                            if (s.contains("com.tencent.mm")){
                                Log.d("微信","服务保活");
                                try {
                                    Thread.sleep(1000);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                addCommand("am startservice -n com.tencent.mm/.booter.CoreService\n");
                            }
                        }
                        Settings settings=Settings.getInstance(AutoService.this,true);
                        for (Settings.Setting setting:settings.getItems()){
                            setting.command();
                        }
                        try {
                            RootUtils.runCommand(builder.toString());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        super.handleMessage(msg);
                    }
                },(int)Preference.getInt(getApplicationContext(),"sleepDelay",200)*1000);

            }
        }




    }
    private void addCommand(String addStr){
        builder.append(addStr);
    }
    private void setMode(Context c,int i){
        if (i==5){
            Preference.saveInt(c,"customMode",1);
        }else if(i==6){
            Preference.saveInt(c,"customMode",3);
        }
        else {
            Preference.saveInt(c, "customMode", i);
        }
    }
    public static void protectService(Context context){
        if (!ServiceStatusUtils.isServiceRunning(context, AutoService.class)){
            Intent intent=new Intent(context,AutoService.class);
            context.startService(intent);
        }
    }
    private void readMode(final int i,final Context c){
        if ((Boolean) Preference.getBoolean(c,"custom")){
            int now=(int)Preference.getInt(this,"customMode");
            if (now==i){
                return;
            }
            setMode(c,i);
            Intent serviceIntent;
            serviceIntent = new Intent(c, CustomCommandService.class);
            serviceIntent.putExtra("mode", i);
            serviceIntent.putExtra("isShow", false);
            c.startService(serviceIntent);
            return;
        }
        try{
            //首先获取lkt当前的模式
            int now=cutMode(RootUtils.runCommand("grep PROFILE /data/LKT.prop"));
            //判断是否使用了游戏专用调度
            if (!Preference.getString(AutoService.this,"code6").isEmpty()){
                Intent gameIntent=new Intent(c,CustomCommandService.class);
                gameIntent.putExtra("mode", i);
                gameIntent.putExtra("isShow", false);
                c.startService(gameIntent);
                setMode(c,i);
                return;
            }
            if (now == i){
                return;
            }
            if (i == -1){
                Intent serviceIntent = new Intent(c, CommandService.class);
                int mode =  Preference.getInt(c, "default");
                setMode(c,mode+1);
                serviceIntent.putExtra("mode", mode+1);
                serviceIntent.putExtra("isShow", false);
                c.startService(serviceIntent);
                return;
            }
            setMode(c,i);
            Intent serviceIntent = new Intent(c, CommandService.class);
            serviceIntent.putExtra("mode", i);
            serviceIntent.putExtra("isShow", false);
            c.startService(serviceIntent);

        }catch (Exception e){
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
    public void RemoveTask(int taskId,Context context){
        ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            Class<?> activityManagerClass=Class.forName("android.app.ActivityManager");
            Method removeTask=activityManagerClass.getDeclaredMethod("removeTask",int.class);
            removeTask.setAccessible(true);
            removeTask.invoke(am,taskId);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}