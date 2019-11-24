package com.kai.lktMode.base;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.kai.lktMode.R;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.activity.ShortcutActivity;
import com.kai.lktMode.activity.StartActivity;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.selinux.SElinux;
import com.kai.lktMode.service.AutoService;
import com.kai.lktMode.fragment.MyFragment;
import com.kai.lktMode.service.CommandService;
import com.kai.lktMode.service.CustomCommandService;
import com.kai.lktMode.thermal.Thermal;
import com.kai.lktMode.thermal.ThermalBean;
import com.kai.lktMode.tool.Preference;
import com.xdandroid.hellodaemon.DaemonEnv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MyApplication extends Application {
    private MainActivity mainActivity;
    @Override
    public void onCreate() {
        super.onCreate();
        if ( Preference.getBoolean(this,"nightMode")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        DaemonEnv.initialize(this, AutoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        DaemonEnv.startServiceMayBind(AutoService.class);
        if (Preference.getBoolean(this,"init")){
            try {
                RootUtils.rootAccess();
                Thermal thermal=new Thermal();
                for (ThermalBean bean:thermal.getBeans()){
                    if (bean.isEnable()){
                        bean.setEnable(false);
                    }
                }
                if (SElinux.isSupprot()&&SElinux.getEnable())
                    RootUtils.runCommand(SElinux.setEnable(false));
            }catch (Exception e){
                e.printStackTrace();
            }


        }
        setShortcuts(getApplicationContext());

    }


    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
    public Context getContext(){
        return mainActivity;
    }
    public static void setShortcuts(Context context){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N_MR1){
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            List<ShortcutInfo> shortcuts=new ArrayList<>();
            for (int i=0;i<SystemInfo.modes.length;i++){
                shortcuts.add(getShortcut(i,context));
            }
            shortcutManager.setDynamicShortcuts(shortcuts);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @TargetApi(25)
    private static ShortcutInfo getShortcut(int i,Context context){
        Intent intent=new Intent(context, ShortcutActivity.class);
        intent.putExtra("mode",i+1);
        intent.setAction(Intent.ACTION_VIEW);
        ShortcutInfo shortcut = new ShortcutInfo.Builder(context, "id"+i)
                .setShortLabel(SystemInfo.modes[i])
                .setLongLabel(SystemInfo.modes[i]+"模式")
                .setIcon(Icon.createWithResource(context, SystemInfo.icons[i]))
                .setIntent(intent)
                .build();
        return shortcut;
    }
}
