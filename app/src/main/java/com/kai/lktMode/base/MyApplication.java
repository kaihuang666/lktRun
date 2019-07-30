package com.kai.lktMode.base;

import android.app.Application;

import com.kai.lktMode.service.AutoService;
import com.kai.lktMode.fragment.MyFragment;
import com.xdandroid.hellodaemon.DaemonEnv;

import java.util.ArrayList;
import java.util.List;


public class MyApplication extends Application {
    private List<MyFragment> fragments=new ArrayList<>();
    @Override
    public void onCreate() {
        super.onCreate();
        DaemonEnv.initialize(this, AutoService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        DaemonEnv.startServiceMayBind(AutoService.class);
    }

}
