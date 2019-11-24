package com.kai.lktMode.tool.util;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.util.local.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopApp {
    String dumpCommand="";
    String app="";
    boolean enableRoot=true;
    int sdk=Build.VERSION.SDK_INT;
    private Context context;
    public static TopApp getInstance(Context context){
        return new TopApp(context);
    }
    private TopApp(Context context){
        this.context=context;
        //设置root的命令
        if ( sdk>= Build.VERSION_CODES.P) {//9.0以上
            dumpCommand = "dumpsys activity activities | grep \"Run #\" | head -1 | awk '{print $5}'";
        } else if (sdk>=Build.VERSION_CODES.M){//6.0以上
            dumpCommand = "dumpsys activity r | grep realActivity | head -1";
        }else {
            enableRoot=false;
        }
        if (enableRoot){
            String sample=getByRoot();
            enableRoot= AppUtils.checkAppInstalled(context,sample)||isPackage(sample);
        }

    }
    public String get(){
        if (enableRoot)
            return getByRoot();
        else
            return getTopAppByUsage(context);
    }
    public boolean enterGameMode(){
        Log.d("boo",enableRoot+"");
        return Preference.getGames(context).contains(get());
    }
    private boolean isPackage(String packageName){
        Pattern p=Pattern.compile("\\w\\.\\w\\.\\w");
        Matcher m=p.matcher(packageName);
        return m.find();
    }
    private String getByRoot() {
        String packageName = "";
        try {
            String request = RootUtils.runCommand(dumpCommand);
            if (!TextUtils.isEmpty(request)) {
                String[] requests;
                if (sdk == Build.VERSION_CODES.P) {
                    requests = request.split("/");
                } else {
                    requests = request.replace("realActivity=", "").replaceAll(" ", "").split("/");
                }
                packageName = requests[0];
                Log.d("package",packageName+"v");
                return packageName;
            }

        }catch (Exception ignored){
            ignored.printStackTrace();
        }
        return "";
    }

    public String getTopAppByUsage(Context context){
        final long start = System.currentTimeMillis()-1000;
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        final long end=System.currentTimeMillis()+1000;
        final UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService( Context.USAGE_STATS_SERVICE);
        final UsageEvents events = usageStatsManager.queryEvents(start, end);
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
        return "";
    }

}
