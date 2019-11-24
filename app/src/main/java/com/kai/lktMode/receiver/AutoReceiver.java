package com.kai.lktMode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.kai.lktMode.fragment.MainFragment;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.Preference;

public class AutoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        if (intent.getAction().equals("com.kai.lktMode.restart")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e){
                        RootUtils.runCommand("am startservice -n com.kai.lktMode/.AutoService");
                    }

                }
            }).start();
        }
    }
}
