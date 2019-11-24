package com.kai.lktMode.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.kai.lktMode.R;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.service.CommandService;
import com.kai.lktMode.service.CustomCommandService;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.widget.LktAppWidget;

public class ShortcutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);
        Intent intent=getIntent();
        Intent service=new Intent();
        int mode=intent.getIntExtra("mode",1);
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                R.layout.deskwidget);
        if ((Boolean) Preference.getBoolean(getApplicationContext(),"custom")){
            service.setClass(getApplicationContext(), CustomCommandService.class);
            service.putExtra("mode",mode);
            remoteViews.setTextViewText(R.id.mode, SystemInfo.modes[mode-1]);
        }else {
            service.setClass(getApplicationContext(), CommandService.class);
            service.putExtra("mode",mode);
            remoteViews.setTextViewText(R.id.mode,"LKT");
        }
        ComponentName thisWidget = new ComponentName(this, LktAppWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        startService(service);
        finish();
    }
}
