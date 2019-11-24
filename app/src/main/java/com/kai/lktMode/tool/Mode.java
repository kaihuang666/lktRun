package com.kai.lktMode.tool;

import android.content.Context;
import android.os.Environment;

import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.root.RootFile;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mode {
    private String modeName;
    private boolean isEnable=true;
    private int currentMode=1;//1 2 3 4代表四个模式
    boolean lktEnable=false;//时候安装了LKT模块
    private Context context;
    private String lktPassage;
    private String readPropFormData(){
        return new RootFile("/data/LKT.prop").readFile();
    }
    public static Mode getInstance(Context context){
        return new Mode(context);
    }
    private Mode(Context context){
        this.context=context;
    }

    public String getModeName() {
        return modeName;
    }

    public void initalize(){
        isEnable=true;
        if (isPowerCfgEnable()){
            if (! Preference.getBoolean(context,"custom"))
                Preference.saveBoolean(context,"custom",true);
            lktEnable=false;
            modeName="yc调度";
            currentMode=(int)Preference.getInt(context,"customMode");
            return;
        }
        if ( Preference.getBoolean(context,"custom")){
            lktEnable=false;
            modeName="自定义调度";
            currentMode=(int)Preference.getInt(context,"customMode");
            return;
        }
        if (LKTCommand.isLKTInstalled()){
            lktEnable=true;
            lktPassage=readPropFormData();
            modeName=compile("LKT™.*",lktPassage,0);
            currentMode= Arrays.asList(LKTCommand.lktModes).indexOf(compile("PROFILE[^\\w]+(\\w+)",lktPassage,1))+1;
            return;
        }
        isEnable=false;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setLktEnable(boolean lktEnable) {
        this.lktEnable = lktEnable;
    }
    public String changeMode(int i){
        if (lktEnable)
            return LKTCommand.getLKTCommand()+" "+i;
        return Preference.getString(context,"code"+i);
    }
    public int getCurrentMode() {
        return currentMode;
    }

    public boolean isLktEnable() {
        return lktEnable;
    }

    private String compile(String pattern, String matcher, int group){
        //String pattern = "LKT™.*";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(matcher);

        if (m.find()) {
            if (group==0)
                return m.group(0).trim();
            return m.group(group).trim().toLowerCase();
        }
        return "LKT";
    }
    private boolean isPowerCfgEnable(){
        return new File(SystemInfo.powercfg).exists();
    }
}
