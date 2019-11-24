package com.kai.lktMode.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.kai.lktMode.R;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.ServiceStatusUtils;
import com.kai.lktMode.service.AutoService;
import com.kai.lktMode.service.CommandService;
import com.kai.lktMode.service.CustomCommandService;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.util.Timer;

/**
 * Implementation of App Widget functionality.
 */
public class LktAppWidget extends AppWidgetProvider {
    private static String ACTION="com.kai.lktUpdate";
    int[] buttonIds={R.id.battery_button,R.id.balance_button,R.id.performance_button,R.id.turbo_button};
    int[] modes={1,2,3,4};
    String[] modes_name= ShellUtil.concat(new String[]{"未配置"}, SystemInfo.modes);
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.deskwidget);
        if ( Preference.getBoolean(context,"custom")){
            int now=Preference.getInt(context,"customMode");
            views.setTextViewText(R.id.mode,modes_name[now]);
        }else {
            views.setTextViewText(R.id.mode,"LKT");
        }
        for (int id:modes){
            //views.setOnClickPendingIntent(buttonIds[id-1],getPendingIntent(id,context));
            views.setOnClickPendingIntent(buttonIds[id-1],getPendingIntent(context,id));
        }
        appWidgetManager.updateAppWidget(appWidgetIds, views);

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        int mode=intent.getIntExtra("mode",1);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.deskwidget);
        if (intent.getAction().equals("click_custom")){
            int now=Preference.getInt(context,"customMode");
            if (now==mode){
                return;
            }
            Toast.makeText(context,"正在切换到"+modes_name[mode]+"模式",Toast.LENGTH_LONG).show();
            String code= Preference.getString(context,"code"+mode);
            try {
                remoteViews.setTextViewText(R.id.mode,modes_name[mode]);
                Preference.saveInt(context,"customMode",mode);
                Context context1=context;
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context1);
                ComponentName componentName = new ComponentName(context1, LktAppWidget.class);
                appWidgetManager.updateAppWidget(componentName, remoteViews);
                RootUtils.runCommand(code, new RootUtils.onCommandComplete() {
                    @Override
                    public void onComplete() {
                        Toast.makeText(context1,"切换成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCrash(String error) {

                    }

                    @Override
                    public void onOutput(String result) {

                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }if (intent.getAction().equals("click_lkt")){
            Toast.makeText(context,"正在切换到"+modes_name[mode]+"模式",Toast.LENGTH_LONG).show();
            try {
                remoteViews.setTextViewText(R.id.mode,"LKT");
                RootUtils.runCommand("lkt "+mode);
                Context context1=context;
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context1);
                ComponentName componentName = new ComponentName(context1, LktAppWidget.class);
                appWidgetManager.updateAppWidget(componentName, remoteViews);
                new Handler(Looper.getMainLooper())
                        .postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context1,"切换成功",Toast.LENGTH_SHORT).show();

                            }
                        },4000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
    private PendingIntent getPendingIntent(Context context,int mode){
        Intent intent=new Intent();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            intent.setComponent((new ComponentName(context,LktAppWidget.class)));
        if ( Preference.getBoolean(context,"custom")){
            intent.setAction("click_custom");
        }else {
            intent.setAction("click_lkt");
        }
        intent.putExtra("mode",mode);
        Log.d("sss",mode+"");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,mode,intent,0);
        return pendingIntent;
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

