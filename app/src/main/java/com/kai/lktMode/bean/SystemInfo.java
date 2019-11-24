package com.kai.lktMode.bean;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import com.kai.lktMode.R;
import com.kai.lktMode.activity.ShortcutActivity;
import com.kai.lktMode.root.RootFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemInfo {
    public static String cpuKernelPath="/sys/devices/system/cpu";
    public static String cpusetPath="/dev/cpuset";
    public static String cpufreqGroupPath=cpuKernelPath+"/cpufreq";
    public static String appPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode";
    public static String powercfg= appPath+"/powercfg/powercfg.sh";
    public static String backupPath=appPath+"/backup";
    public static String restorePath=appPath+"/restore";
    public static String binaryPath=appPath+"/bin";
    public static boolean isDonated=false;
    public static int type_freq=0;
    public static int type_limit=1;
    public static int type_param=2;
    public static int type_boost=3;
    public static int type_stune=4;
    public static int type_gpu=5;
    public static int type_cpuset=0;
    public static int type_selinux=1;
    public static int type_thermal=2;
    public static String[] modes=new String[]{"省电","均衡","游戏" ,"极限"};
    public static String[] modes_Engnish=new String[]{"Battery","Balanced","Performance","Turbo"};
    public static int[] icons=new int[]{R.mipmap.battery_tile,R.mipmap.balance_tile,R.mipmap.performance_tile,R.mipmap.turbo_tile};
    public static String SystemModule$cpuboost="/sys/module/cpu_boost/parameters";
    public static File getChild(File parent,String name){
        String path=parent.getAbsolutePath();
        String childPath=path+"/"+name;
        return new File(childPath);
    }
    public static RootFile getChild(RootFile parent, String name){
        String path=parent.getPath();
        String childPath=path+"/"+name;
        return new RootFile(childPath);
    }

    public static void setIsDonated(boolean isDonated) {
        SystemInfo.isDonated = isDonated;
    }
    public static boolean getIsDonated(){
        return SystemInfo.isDonated;
    }
    public static  String getHardware(){
        String hardware="";
        //针对骁龙系列
        try {
            hardware=getFieldFromCpuinfo("Hardware").trim();
        }catch (Exception e){
            e.printStackTrace();
        }
        //针对联发科、猎户座、麒麟
        if (hardware.isEmpty())
            hardware= Build.HARDWARE;
        return hardware.toLowerCase();
    }
    private static String getFieldFromCpuinfo(String field) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
        Pattern p = Pattern.compile(field + "\\s*:\\s*(.*)");

        try {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    return m.group(1);
                }
            }
        } finally {
            br.close();
        }

        return "";
    }


}
