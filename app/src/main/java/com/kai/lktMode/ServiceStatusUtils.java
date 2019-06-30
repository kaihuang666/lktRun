package com.kai.lktMode;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class ServiceStatusUtils {
    public static boolean isServiceRunning(Context context , Class<?> cls){
        //相当于电脑上的进程管理器
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(300);
        for(ActivityManager.RunningServiceInfo info : infos){
            //获取到正在运行的服务的名字
            String service= info.service.getClassName();
            //判断两个类名是否一致
            if(service.equals(cls.getName())){
                return true;
            }
        }
        return false;
    }

}
