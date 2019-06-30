package com.kai.lktMode;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransTool {
    private Map<String,String> dictionary=new HashMap<>();
    public  TransTool(){
        dictionary.put("brightness","亮度");
        dictionary.put("autoBright","自动亮度");
        dictionary.put("volume","音量");
    }
    public String getTitle(String str){
        return  dictionary.get(str);
    }
    public static String trans(String str){
        String[] args=str.split(":");
        String pattern = "^\\d+$";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(args[1]);
        if (m.find( )) {
            //Preference.save(MainActivity.this,"version",true);
            Log.d("数字",m.group(1)+"%");
            return m.group(1)+"%";
        } else {
            if(Boolean.parseBoolean(args[1])){
                return "开启";
            }
            else {
                return "开启";
            }
        }
    }
}
