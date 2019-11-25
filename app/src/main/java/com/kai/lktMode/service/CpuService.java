package com.kai.lktMode.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.bean.Temperature;
import com.kai.lktMode.cpu.CpuAmount;
import com.kai.lktMode.cpu.CpuModel;
import com.kai.lktMode.device.CurrentManager;
import com.kai.lktMode.gpu.GPUFreq;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.util.local.CpuUtil;
import com.kai.lktMode.tool.util.local.ShellUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class CpuService extends Service {
    private CpuThread cpuThread;
    private Temperature temperature;
    private CurrentManager currentManager;
    private int cpuAmount=CpuUtil.getCpuAmount();//cpu核心数量
    private CpuBinder binder=new CpuBinder();
    private LocalBroadcastManager localBroadcastManager;
    private int[] max=getMax();//获取每个核心的最高频率
    private boolean hasCpu=true;
    private boolean hasGpu=true;
    private GPUFreq gpuFreq=GPUFreq.getInstance();
    public CpuService() {
    }
    public class CpuBinder extends Binder{
        public void startListening(){
            cpuThread.setCreated(new OnHandlerCreated() {
                @Override
                public void created() {
                    if (gpuFreq.supported())
                        cpuThread.getHandler().sendEmptyMessageDelayed(9,0);
                    cpuThread.getHandler().sendEmptyMessageDelayed(6,0);
                    cpuThread.getHandler().sendEmptyMessageDelayed(4,0);
                    cpuThread.getHandler().sendEmptyMessageDelayed(3,0);

                }
            });

            try {
                cpuThread.start();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        public void stopListening(){
            cpuThread.getHandler().removeMessages(3);
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
            cpuThread.getHandler().removeMessages(3);
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
                        if (msg.what==3){
                            cpuThread.getHandler().removeMessages(3);
                            Bundle bundle=msg.getData();
                            Intent intent=new Intent("com.kai.lktMode.cpuListening");
                            intent.putExtras(bundle);
                            localBroadcastManager.sendBroadcast(intent);
                            cpuThread.getHandler().sendEmptyMessageDelayed(3,500);
                        }
                        if (msg.what==8){
                            cpuThread.getHandler().removeMessages(8);
                            Bundle bundle=msg.getData();
                            Intent intent=new Intent("com.kai.lktMode.cpuListening");
                            intent.putExtras(bundle);
                            localBroadcastManager.sendBroadcast(intent);
                            cpuThread.getHandler().sendEmptyMessageDelayed(8,500);
                        }
                        if (msg.what==4){
                            cpuThread.getHandler().removeMessages(4);
                            Intent intent=new Intent("com.kai.lktMode.currentUpdate");
                            intent.putExtra("current",msg.getData().getString("current"));
                            localBroadcastManager.sendBroadcast(intent);
                            cpuThread.getHandler().sendEmptyMessageDelayed(4,800);
                        }
                        if (msg.what==7){
                            cpuThread.getHandler().removeMessages(7);
                            Intent intent=new Intent("com.kai.lktMode.currentUpdate");
                            intent.putExtra("current",msg.getData().getString("current"));
                            localBroadcastManager.sendBroadcast(intent);
                            cpuThread.getHandler().sendEmptyMessageDelayed(7,1000);
                        }
                        if (msg.what==6){
                            Intent intent=new Intent("com.kai.lktMode.tempUpdate");
                            intent.putExtra("temp",msg.getData().getString("temp"));
                            localBroadcastManager.sendBroadcast(intent);
                            cpuThread.getHandler().sendEmptyMessageDelayed(6,1500);
                        }
                        if (msg.what==5){
                            Intent intent=new Intent("com.kai.lktMode.tempUpdate");
                            intent.putExtra("temp",msg.getData().getString("temp"));
                            localBroadcastManager.sendBroadcast(intent);
                            cpuThread.getHandler().sendEmptyMessageDelayed(5,1500);
                        }
                        if (msg.what==9){
                            Intent intent=new Intent("com.kai.lktMode.gpuPercent");
                            intent.putExtra("percent",msg.getData().getInt("percent",0));
                            localBroadcastManager.sendBroadcast(intent);
                            cpuThread.getHandler().sendEmptyMessageDelayed(9,1000);
                        }
                    }
                }

        );
        temperature=Temperature.getInstance(CpuService.this);
        hasCpu=temperature.hasCPU();
        hasGpu=temperature.hasGPU();
        localBroadcastManager=LocalBroadcastManager.getInstance(CpuService.this);
        currentManager=CurrentManager.getInstance(CpuService.this);
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
    //获取每一核心支持的最高频率
    private int[] getMax(){
        int[] result=new int[cpuAmount];
        for (int i=0;i<result.length;i++){
            result[i]=getMaxCpuFreq("cpu"+i);
        }
        return result;
    }
    private int[] getMin(){
        int[] result=new int[cpuAmount];
        for (int i=0;i<result.length;i++){
            result[i]=getMinCpuFreq("cpu"+i);
        }
        return result;
    }
    //获取单核支持的最高频率
    public int getMaxCpuFreq(String cpu){
        return ShellUtil.getIntFromFile(new File("/sys/devices/system/cpu/"+cpu+"/cpufreq/cpuinfo_max_freq"))/1000;
    }
    public int getMinCpuFreq(String cpu){
        return ShellUtil.getIntFromFile(new File("/sys/devices/system/cpu/"+cpu+"/cpufreq/cpuinfo_min_freq"))/1000;
    }
    //获取每一核心的频率
    private int[] getCpu(){
        int[] result=new int[cpuAmount];
        for (int i=0;i<result.length;i++){
            int cpuFreq0=getCurCpuFreq("cpu"+i);
            for (int j=0;j<4;j++){
                int freq=getCurCpuFreq("cpu"+i);
                if (freq<cpuFreq0){
                    cpuFreq0=freq;
                }
            }
            result[i]=cpuFreq0;
        }
        return result;
    }
    //获取单核的频率
    public int getCurCpuFreq(String cpu){
        return ShellUtil.getIntFromFile(new File("/sys/devices/system/cpu/"+cpu+"/cpufreq/scaling_cur_freq"))/1000;
    }
    //计算cpu的总使用率
    private int getCpuRate(int[] progress){
        int sum=0;
        for (int i=0;i<cpuAmount;i++){
            sum+=(int)progress[i];
        }
        return sum/cpuAmount;
    }
    //获取cpu温度
    private float getCpuTemp(File file){
        float temp=0;
        try {
            FileInputStream fis = new FileInputStream(file);
            StringBuffer sbTemp = new StringBuffer("");

            // read temperature
            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                sbTemp.append(new String(buffer));
            }
            fis.close();

            // parse temp
            String sTemp = sbTemp.toString().replaceAll("[^0-9.]+", "");
            temp = Float.valueOf(sTemp);
        }catch (Exception e){
            e.printStackTrace();
        }
        return temp;
    }
    private int readFile(String path, int defaultValue) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    path));
            int i = Integer.parseInt(bufferedReader.readLine(), 10);
            bufferedReader.close();
            return i;
        } catch (Exception localException) {
        }
        return defaultValue;
    }


    public class UpdateHandler extends Handler{
        private int[] freq=new int[cpuAmount];
        private String current="0mA";
        private int[] progress=new int[cpuAmount];;
        private Handler parent;
        private String cpu_temp;
        private String temp="0";
        private int sum=0;
        private Bundle bundle;
        public UpdateHandler(Handler handler){
            parent=handler;
        }
        @Override
        public void handleMessage(@NonNull final Message msg) {
            if (msg.what==4){
                try {
                    int current_int=currentManager.getCurrent();
                    Bundle bundle1=new Bundle();
                    bundle1.putString("current",(currentManager.isCharge()?"充电":"放电")+"电流："+current_int+"mA");
                    Message message=new Message();
                    message.what=4;
                    message.setData(bundle1);
                    parent.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (msg.what==9){
                int percent=gpuFreq.getBusy();
                Bundle bundle1=new Bundle();
                bundle1.putInt("percent",percent);
                Message message=new Message();
                message.what=9;
                message.setData(bundle1);
                parent.sendMessage(message);
            }
            if (msg.what==6){
                try {
                    if (hasCpu){
                        temp=String.valueOf(temperature.getCPUTemp());
                        bundle=new Bundle();
                        bundle.putString("temp",temp);
                        Message message=new Message();
                        message.setData(bundle);
                        message.what=5;
                        parent.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (msg.what==3){
                try {
                    freq=getCpu();
                    for (int i=0;i<freq.length;i++){
                        if (max[i]==0||freq[i]==0){
                            progress[i]=0;
                            continue;
                        }
                        progress[i]=100*freq[i]/max[i];
                    }
                    //计算和获取信息
                    sum=getCpuRate(progress);
                    bundle=new Bundle();
                    bundle.putIntArray("freq",freq);
                    bundle.putIntArray("progress",progress);
                    bundle.putInt("sum",sum);
                    Message message=new Message();
                    message.setData(bundle);
                    message.what=3;
                    parent.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            if (msg.what==8){
                try {
                    freq=getCpu();
                    for (int i=0;i<freq.length;i++){
                        if (max[i]==0||freq[i]==0){
                            progress[i]=0;
                            continue;
                        }
                        progress[i]=100*freq[i]/max[i];
                    }
                    //计算和获取信息
                    sum=getCpuRate(progress);
                    bundle=new Bundle();
                    bundle.putIntArray("freq",freq);
                    bundle.putIntArray("progress",progress);
                    bundle.putInt("sum",sum);
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
