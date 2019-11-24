package com.kai.lktMode.bean;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class Sdcard {
    public static String getPath(Context context){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            return context.getExternalFilesDir("").getAbsolutePath();
        }
        else if (Environment.getExternalStorageDirectory().exists())
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        else if (new File("/sdcard").exists())
            return "/sdcard";
        else if (new File("/mmt/sdcard").exists())
            return "/mnt/sdcard";
        else if (new File("/storage/sdcard0").exists()){
            return "/storage/sdcard0";
        }
        else
            return Environment.getExternalStorageState();
    }
}
