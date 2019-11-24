package com.kai.lktMode.cpu;

import android.util.Log;

import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.selinux.SElinux;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class CpuBoost {
    static RootFile parent;
    static RootFile freq;
    static RootFile ms;
    HashMap<String,String> map;
    String ms_value;
    public static String INPUT_BOOST_FREQ=SystemInfo.SystemModule$cpuboost+"/input_boost_freq";
    public static String INPUT_BOOST_MS=SystemInfo.SystemModule$cpuboost+"/input_boost_ms";
    public static boolean isSupport(){
        return new RootFile(SystemInfo.SystemModule$cpuboost).exists();
    }
    public static boolean isAddition(){
        return new RootFile("/sys/module/msm_performance/parameters").exists();
    }
    public CpuBoost(){
        parent=new RootFile(SystemInfo.SystemModule$cpuboost);
        freq=new RootFile(INPUT_BOOST_FREQ);
        ms=new RootFile(INPUT_BOOST_MS);
        map=getBoostFreqences(getFreq());
    }
    public int getFreq(String i){
        return Integer.valueOf(map.get(i));
    }
    public String getFreqStr(int i){
        return (Integer.valueOf(map.get(String.valueOf(i)))/1000)+"";
    }
    public String getFreq() {
        return freq.readFile();
    }
    public void setFreqMap(String i, String value){
        map.put(i,value);
    }
    public static String setFreq(String i,String value){
        return ShellUtil.lock(INPUT_BOOST_FREQ,"'"+i+":"+value+"'");
    }
    public static String setFreq(int i,String value){
        return ShellUtil.lock(INPUT_BOOST_FREQ,"'"+i+":"+value+"'");
    }

    public static HashMap<String,String> getBoostFreqences(String freqStr){
        HashMap<String,String> map=new HashMap<>();
        String[] freqs=freqStr.split("\\s");
        for (String freq:freqs){
            String[] param=freq.split(":");
            if (param.length<2)
                continue;
            map.put(param[0].trim(),param[1].trim());
        }
        return map;
    }

    public String getMs() {
        return ms.readFile();
    }
    public static String setMs(String value){
        return ShellUtil.modify(SystemInfo.SystemModule$cpuboost+"/input_boost_ms","'"+value+"'");
    }
}
