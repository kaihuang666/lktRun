package com.kai.lktMode.tool.util.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;

import com.kai.lktMode.tool.Preference;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

public class CpuUtil {
    public static HashMap<String, Class<?>> prefs = new HashMap<String, Class<?>>();
    private static boolean READWITHROOT=true;
    private static boolean READ=false;

    static {
        prefs.put("update_interval", String.class);
        prefs.put("position", String.class);
        prefs.put("temperature_file", String.class);
        prefs.put("temperature_divider", String.class);
        prefs.put("measurement", String.class);
        prefs.put("manual_color", Boolean.class);
        prefs.put("color_mode", String.class);
        prefs.put("configured_color", Integer.class);
        prefs.put("color_low", Integer.class);
        prefs.put("color_middle", Integer.class);
        prefs.put("color_high", Integer.class);
        prefs.put("temp_middle", String.class);
        prefs.put("temp_high", String.class);
    }
    public CpuUtil(){
        //获取具有root权限的shell，用来读取被限制的文件
        try {
            Shell shell=RootTools.getShell(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        //定义cpu文件路径，每部手机必然存在
        File file=new File(" /sys/devices/system/cpu");
        //获取cpu核心的文件夹
        File[] cpus=file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                Matcher matcher= Pattern.compile("cpu[0-9]+").matcher(file.getName());
                return matcher.matches();
            }
        });

    }
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
    public static float getCpuTemp(Context context,File file){
        float temp=0;
        if (file==null){
            Preference.save(context,"tempReadable",true);
            return temp;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            StringBuffer sbTemp = new StringBuffer("");

            // read temperature
            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                sbTemp.append(new String(buffer));
            }
            fis.close();

            // parse temp
            String sTemp = sbTemp.toString().replaceAll("[^0-9.]+", "");
            temp = Float.valueOf(sTemp);
        }catch (Exception e){
            e.printStackTrace();
        }

        return temp;
    }

    public static int getCpuAmount(){
        int amount=0;
        try {
            String result=ShellUtil.getStringFromFile(new File("/sys/devices/system/cpu/present")).trim();
            String[] cpus=result.split("-");
            amount=Integer.valueOf(cpus[1])+1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return amount;
    }
    public static int getCurrent(Context context){
        boolean readable=(boolean)Preference.get(context,"currentReadable",READ);
        if (readable){
            return -1;
        }
        return getCurrentOnAll(context);
    }
    private static int getCurrentOnAll(Context context){
        if (Build.VERSION.SDK_INT!=Build.VERSION_CODES.P){
            BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            int current_int=manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)/1000;
            if (current_int==0){
                current_int=manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            }
            return current_int;
        }else {
            Preference.save(context,"currentReadable",READWITHROOT);
            return -1;
        }
    }
    public static String[] getTemperatureFiles() {
        ArrayList<String> result = new ArrayList<String>();
        result.add("AUTO");

        try {
            InputStream in = Runtime.getRuntime()
                    .exec("busybox find /sys -type f -name *temp*")
                    .getInputStream();
            BufferedReader inBuffered = new BufferedReader(
                    new InputStreamReader(in));

            String line = null;
            while ((line = inBuffered.readLine()) != null) {
                if(line.indexOf("trip_point")>=0)
                    continue;
                if(line.indexOf("_crit")>=0)
                    continue;

                result.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toArray(new String[]{});
    }

    private static String[] tempFiles = {
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/kernel/debug/tegra_thermal/temp_tj",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/devices/platform/s5p-tmu/curr_temp",
            "/sys/devices/system/cpu/cpufreq/cput_attributes/cur_temp",
            "/sys/devices/platform/s5p-tmu/temperature",
    };

    public static File getTempFile(Context context, String fileName) {
        File ret = null;
        String tempFile=(String)Preference.get(context,"temp","String");
        if (!tempFile.isEmpty()){
            return new File(tempFile);
        }
        if ((boolean)Preference.get(context,"tempReadable","Boolean")){
            return null;
        }
        if(fileName!=null) {
            ret = new File(fileName);
            if(!ret.exists() || !ret.canRead())
                ret = null;
        }
        //在一些特殊平台上的特定温度
        if(ret==null || fileName.equals("AUTO")) {
            for(String tempFileName : tempFiles) {
                ret = new File(tempFileName);
                if(!ret.exists() || !ret.canRead()) {
                    ret = null;
                    continue;
                }
                else break;
            }
        }
        //使用遍历的方法获取所有传感器的温度
        if(ret==null) {
            File file=new File("/sys/class/thermal/");

            File[] files=file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    Matcher matcher= Pattern.compile("thermal_zone\\d?").matcher(s);
                    return matcher.matches();
                }
            });
            if (files==null){
                return null;
            }
            for (File f:files){
                File temp=new File(f.getAbsolutePath()+"/temp");
                File type=new File(f.getAbsolutePath()+"/type");
                if (ShellUtil.getIntFromFile(temp)/1000>30&&!ShellUtil.getStringFromFile(type).equals("battery")){
                    ret=temp;
                    break;
                }
            }
        }
        if (ret!=null)
            Preference.save(context,"temp",ret.getAbsolutePath());
        return ret;
    }
    public class Cpu{
        private File cpu;
        private String type;
    }
}