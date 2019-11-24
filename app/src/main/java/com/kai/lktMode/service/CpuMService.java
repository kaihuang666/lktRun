package com.kai.lktMode.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kai.lktMode.tool.util.local.CpuUtil;
import com.kai.lktMode.tool.util.local.ShellUtil;

import java.io.File;

public class CpuMService extends Service {
    private CpuThread cpuThread;
    private int cpuAmount= CpuUtil.getCpuAmount();//cpu核心数量
    private CpuBinder binder=new CpuBinder();
    private LocalBroadcastManager localBroadcastManager;
    public CpuMService() {
    }
    public class CpuBinder extends Binder{
        public void startListening(){
            cpuThread.setCreated(new OnHandlerCreated() {
                @Override
                public void created() {
                    cpuThread.getHandler().sendEmptyMessageDelayed(8,500);

                }
            });

            try {
                cpuThread.start();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        public void stopListening(){
            cpuThread.getHandler().removeMessages(8);
        }
        public void startOnlyCpu(){
            cpuThread.setCreated(new OnHandlerCreated() {
                @Override
                public void created() {
                    cpuThread.getHandler().sendEmptyMessageDelayed(8,500);
                }
            });

        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (cpuThread.getHandler()!=null){
            cpuThread.interrupt();
            cpuThread.getHandler().getLooper().quit();
            cpuThread.getHandler().removeMessages(8);
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cpuThread=new CpuThread(
                new Handler(){
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        if (msg.what==8){
                            Bundle bundle=msg.getData();
                            Intent intent=new Intent("com.kai.lktMode.cpu");
                            intent.putExtras(bundle);
                            localBroadcastManager.sendBroadcast(intent);
                            Log.d("cpu$","sss");
                            cpuThread.getHandler().sendEmptyMessageDelayed(8,1500);
                        }

                    }
                }

        );
        localBroadcastManager=LocalBroadcastManager.getInstance(CpuMService.this);
    }

    private class CpuThread extends Thread{
        private UpdateHandler handler;
        private OnHandlerCreated created;
        private Handler parent;
        public Looper child;
        public CpuThread(Handler handler){
            parent=handler;
        }

        public void setCreated(OnHandlerCreated created) {
            this.created = created;
        }

        @Override
        public void run() {
            Looper.prepare();
            child=Looper.myLooper();
            this.handler=new UpdateHandler(parent);
            if (created!=null)
                created.created();
            Looper.loop();
        }

        public void setHandler(UpdateHandler handler) {
            this.handler = handler;
        }

        public UpdateHandler getHandler() {
            return handler;
        }

    }
    interface OnHandlerCreated{
        void created();
    }
    //获取每一核心的频率
    private int[] getCpu(){
        int[] result=new int[cpuAmount];
        for (int i=0;i<result.length;i++){
            int cpuFreq0=getCurCpuFreq("cpu"+i);
            result[i]=cpuFreq0;
        }
        return result;
    }
    //获取单核的频率
    public int getCurCpuFreq(String cpu){
        return ShellUtil.getIntFromFile(new File("/sys/devices/system/cpu/"+cpu+"/cpufreq/scaling_cur_freq"))/1000;
    }




    public class UpdateHandler extends Handler{
        private int[] freq=new int[cpuAmount];
        private int[] progress=new int[cpuAmount];;
        private Handler parent;
        private Bundle bundle;
        public UpdateHandler(Handler handler){
            parent=handler;
        }
        @Override
        public void handleMessage(@NonNull final Message msg) {
            if (msg.what==8){
                try {
                    this.removeMessages(8);
                    freq=getCpu();
                    bundle=new Bundle();
                    bundle.putIntArray("freq",freq);
                    Message message=new Message();
                    message.setData(bundle);
                    message.what=8;
                    parent.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }
    }


}
