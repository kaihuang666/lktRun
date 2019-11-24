package com.kai.lktMode.tool;

import android.os.Environment;

import com.kai.lktMode.bean.Sdcard;
import com.kai.lktMode.tool.util.local.ShellUtil;

import java.io.File;

public class LKTCommand {
    public static String[] lktModes=new String[]{"battery","balanced","performance","turbo"};
    public static String LKT_magisk_path="/system/xbin/lkt";//lkt的magisk框架path
    public static boolean isLKTInstalled(){
        boolean installed=false;
        if (ShellUtil.isInstalled("lkt")){
            installed=true;
        }else if (new File(LKT_magisk_path).exists()){
            installed=true;
        }
        return installed;
    }
    public static String getLKTCommand(){
        if (ShellUtil.isInstalled("lkt")){
            return "lkt";
        }else if (new File(LKT_magisk_path).exists()){
            return "sh "+LKT_magisk_path;
        }
        return "";
    }
}
