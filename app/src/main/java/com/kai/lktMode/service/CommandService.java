package com.kai.lktMode.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.base.MyApplication;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.LKTCommand;

import java.util.Timer;
import java.util.TimerTask;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class CommandService extends IntentService {
    private String[] modes=new String[]{"省电模式", "均衡模式", "游戏模式","极限模式"};
    Handler msgHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LocalBroadcastManager manager=LocalBroadcastManager.getInstance(CommandService.this);
            manager.sendBroadcast(new Intent("com.kai.lktMode.refresh"));
            Toast.makeText(CommandService.this, msg.getData().getString("Text"), Toast.LENGTH_SHORT).show();
            super.handleMessage(msg);
        }
    };
    public CommandService(){
        super("CommandService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final boolean isShow=intent.getBooleanExtra("isShow",true);
        int mode=intent.getIntExtra("mode",1);
        Log.d("switchTo:",mode+"");
        if (isShow)
        showToastByMsg(CommandService.this,modes[mode-1]+"切换中",1000);
        RootUtils.runCommand(LKTCommand.getLKTCommand()+" "+mode);
        TimerTask task= new TimerTask() {
            @Override
            public void run() {
                if (isShow)
                showToastByMsg(CommandService.this,"切换完成",1000);

                msgHandler.removeCallbacksAndMessages(Looper.getMainLooper());
            }
        };
        Timer timer=new Timer();
        timer.schedule(task,5000);
    }
    private void showToastByMsg(final IntentService context, final CharSequence text, final int duration) {
        Bundle data = new Bundle();
        data.putString("Text", text.toString());
        Message msg = new Message();
        msg.setData(data);
        msgHandler.sendMessage(msg);
    }



}
