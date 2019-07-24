package com.kai.lktMode;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kai.lktMode.fragment.MainFragment;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

public class CpuService extends Service {
    private CpuThread cpuThread;
    private Shell shell;
    private int cpuAmount=CpuUtil.getCpuAmount();//cpu核心数量
    private CpuBinder binder=new CpuBinder();
    private LocalBroadcastManager localBroadcastManager;
    private int[] max=getMax();//获取每个核心的最高频率
    private File temp_file;
    public CpuService() {
    }
    public class CpuBinder extends Binder{
        public void startListening(){
            try {
                cpuThread.start();
            }catch (Exception e){
                e.printStackTrace();
            }

            cpuThread.setCreated(new OnHandlerCreated() {
                @Override
                public void created() {
                    cpuThread.getHandler().sendEmptyMessageDelayed(3,500);

                }
            });
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
        try {
            shell=RootTools.getShell(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        cpuThread=new CpuThread(
                new Handler(){
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        if (msg.what==3){
                            Bundle bundle=msg.getData();
                            Intent intent=new Intent("com.kai.lktMode.cpuListening");
                            intent.putExtra("freq",bundle.getIntArray("freq"));
                            intent.putExtra("temp",bundle.getString("temp"));
                            intent.putExtra("progress",bundle.getIntArray("progress"));
                            intent.putExtra("sum",bundle.getInt("sum"));
                            intent.putExtra("current",bundle.getString("current"));
                            localBroadcastManager.sendBroadcast(intent);
                            cpuThread.getHandler().sendEmptyMessageDelayed(3,500);
                        }
                    }
                }

        );
        localBroadcastManager=LocalBroadcastManager.getInstance(CpuService.this);
        temp_file=CpuUtil.getTempFile(CpuService.this,null);
    }

    private class CpuThread extends Thread{
        private UpdateHandler handler;
        private OnHandlerCreated created;
        private Handler parent;
        public boolean isRemoved=false;
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
    //获取单核支持的最高频率
    public int getMaxCpuFreq(String cpu){
        return ShellUtil.getIntFromFile(new File("/sys/devices/system/cpu/"+cpu+"/cpufreq/cpuinfo_max_freq"))/1000;
    }
    //获取每一核心的频率
    private int[] getCpu(){
        int[] result=new int[cpuAmount];
        for (int i=0;i<result.length;i++){
            int cpuFreq0=getCurCpuFreq("cpu"+i);
            for (int j=0;j<6;j++){
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
    private float getMeanCurrentVal(String filePath, int totalCount, int intervalMs) {
        float meanVal = 0.0f;
        if (totalCount <= 0) {
            return 0.0f;
        }
        for (int i = 0; i < totalCount; i++) {
            try {
                float f = Float.valueOf(readFile(filePath, 0));
                meanVal += f / totalCount;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (intervalMs <= 0) {
                continue;
            }
            try {
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return meanVal;
    }

    private String getCurrent(){
        String current="0mA";
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
            BatteryManager manager = (BatteryManager)CpuService.this.getSystemService(Context.BATTERY_SERVICE);
            int current_int=manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)/1000;
            if (current_int==0){
                current_int=manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            }
            current=current_int+"mA";
        }
        return current;
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
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==4){
                getLooper().quit();
                removeMessages(3);
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
                        Log.d("cup"+i,progress[i]+"");
                    }
                    //计算和获取信息
                    sum=getCpuRate(progress);
                    float cpuTemp=getCpuTemp(temp_file)/1000;
                    temp= String.format("%.1f",cpuTemp);
                    current=getCurrent();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    bundle=new Bundle();
                    bundle.putIntArray("freq",freq);
                    bundle.putIntArray("progress",progress);
                    bundle.putInt("sum",sum);
                    //如果没有办法获取到电流，使用RootTools工具
                    if (current.equals("0mA")){
                        try {
                            shell.add(new Command(5,"cat /sys/class/power_supply/battery/current_now"){
                                @Override
                                public void commandOutput(int id, String line) {
                                    super.commandOutput(id, line);
                                    String s = line.toString().trim().replaceAll("[^0-9.]+", "");
                                    float c=Float.valueOf(s)/1000;
                                    //部分机型是以毫安为单位的
                                    if (c<=1){
                                        c=Float.valueOf(s);
                                    }
                                    current = c+"mA";
                                    //将电流放进结果集
                                    bundle.putString("current",current);
                                    //检测cpu温度
                                    if (temp.contains("0")){
                                        try {
                                            shell.add(new Command(6,"cat /sys/class/thermal/thermal_zone0/temp"){
                                                @Override
                                                public void commandOutput(int id, String line) {
                                                    super.commandOutput(id, line);
                                                    String s = line.toString().trim().replaceAll("[^0-9.]+", "");
                                                    int c=Integer.parseInt(s)/1000;
                                                    //部分机型是以毫安为单位的
                                                    if (c<=10){
                                                        c=Integer.parseInt(s);
                                                    }
                                                    temp=c+"";
                                                    bundle.putString("temp",temp);
                                                    Message message=new Message();
                                                    message.setData(bundle);
                                                    message.what=3;
                                                    parent.sendMessage(message);
                                                }
                                            });
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }else {
                                        bundle.putString("temp",temp);
                                        Message message=new Message();
                                        message.setData(bundle);
                                        message.what=3;
                                        parent.sendMessage(message);
                                    }

                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }else {
                        bundle.putString("temp",temp);
                        bundle.putString("current",current);
                        Message message=new Message();
                        message.setData(bundle);
                        message.what=3;
                        parent.sendMessage(message);
                    }

                }

            }

        }
    }


}
