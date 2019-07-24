package com.kai.lktMode.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.kai.lktMode.CpuService;
import com.kai.lktMode.CpuUtil;
import com.kai.lktMode.DownloadUtil;
import com.kai.lktMode.Preference;
import com.kai.lktMode.ProgressAdapter;
import com.kai.lktMode.R;
import com.kai.lktMode.ShellUtil;
import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFragment extends MyFragment implements View.OnClickListener {
    private ProgressDialog dialog;
    private ProgressDialog downloadDialog;
    private String[] modeTitle={"省电模式","均衡模式","游戏模式","极限模式"};
    private String[] modestring={"unsure","Battery","Balanced","Performance","Turbo"};
    private int[] buttonID={R.id.battery,R.id.balance,R.id.performance,R.id.turbo};
    private Shell shell;
    private String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.INTERNET};
    private String passage;
    private List<String> cpus=new ArrayList<>();
    private ProgressAdapter adapter;
    private int cpuAmount=0;
    private CircleProgress circleProgress;
    private View contentView;
    private TextView battery_current;
    private TextView battery_temp;
    private TextView cpu_temp;
    private TextView cpu_info;
    private String temp="0";
    private boolean isCreated=true;
    private ShellUtil shellUtil;
    private int cpuSenserId=0;

    private LocalBroadcastManager localBroadcastManager;
    private LocalRecevicer recevicer;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main,container,false);
        contentView=view;
        shellUtil=ShellUtil.create(true);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
    }

    //碎片创建
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        battery_current=(TextView)contentView.findViewById(R.id.battery_current);
        battery_temp=(TextView)contentView.findViewById(R.id.battery_temp);
        cpu_temp=(TextView)contentView.findViewById(R.id.cpu_temp);
        cpu_info=(TextView)contentView.findViewById(R.id.cpu_info);
        cpu_info.setText("cpu型号："+getCpuPlatform(getContext()));
        getContext().registerReceiver(this.myBatteryReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.kai.lktMode.cpuListening");
        localBroadcastManager=LocalBroadcastManager.getInstance(getActivity());
        recevicer=new LocalRecevicer();
        localBroadcastManager.registerReceiver(recevicer,intentFilter);
        initButton();
        initCpuInfo();
        initDialog();
        //耗时操作的子线程
        try {
            if (getRoot()){
                //shell=RootTools.getShell(true);
                setBusyBox(cutBusyBox());
                readProp(isCreated);
                if (isCreated){
                    isCreated=false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        /*
         max=getMax();
        cpuSenserId=getCpuSenserId();
        Handler handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {

                if (msg.what==3){
                    Log.d("state","I have handled the message");

                    Bundle bundle=msg.getData();
                    adapter.setFreq(bundle.getIntArray("freq"));
                    adapter.setProgress(bundle.getIntArray("progress"));
                    adapter.notifyDataSetChanged();
                    circleProgress.setProgress(bundle.getInt("sum"));
                    battery_current.setText("电流："+bundle.getString("current"));
                    circleProgress.setProgress(bundle.getInt("sum"));
                    cpu_temp.setText("cpu温度："+bundle.getString("cpu_temp").substring(0,bundle.getString("cpu_temp").indexOf(".")+2)+"°C");
                    thread.getHandler().sendEmptyMessageDelayed(3,500);
                }
            }
        };
        thread=new MyThread(handler);

        */
    }

    @Override
    public void onDestroy() {
        //thread.getHandler().removeMessages(3);
        getContext().unregisterReceiver(this.myBatteryReceiver);
        //thread.handler.getLooper().quit();
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(recevicer);
    }

    //碎片激活
    @Override
    public void onResume() {
        super.onResume();
        try {
            readProp(false);

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //碎片被移除或替换
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void Refresh() {
        super.Refresh();
        readProp(false);
    }

    /**初始化控件
     *
     */

    private void initDialog(){
        dialog= new ProgressDialog(getContext(),R.style.AppDialog);
        dialog.setMessage("切换中");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        downloadDialog= new ProgressDialog(getContext(),R.style.AppDialog);
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadDialog.setCancelable(false);
        downloadDialog.setMax(100);
        downloadDialog.setTitle("正在下载");
    }
    private void initCpuInfo(){
        circleProgress=(CircleProgress)contentView.findViewById(R.id.circle);
        RecyclerView recyclerView=contentView.findViewById(R.id.recyclerview);
        adapter=new ProgressAdapter(cpus);
        StaggeredGridLayoutManager manager=new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        cpus.clear();
        cpuAmount=(int)Preference.get(getContext(),"cpuAmount",0);
        if (cpuAmount==0){
            cpuAmount= CpuUtil.getCpuAmount();
            Preference.save(getContext(),"cpuAmount",cpuAmount);
        }
        for (int i=0;i<cpuAmount;i++){
            cpus.add(i,"cpu"+i);
        }
        adapter.notifyDataSetChanged();
    }
    //获取root权限
    private boolean getRoot(){
        boolean isGranted=false;
        try {
            if (RootTools.isRootAvailable()){
                shell=RootTools.getShell(true);
                isGranted=true;
            }else {
                Runtime.getRuntime().exec("su");
                shell=RootTools.getShell(true);
            }

        }catch (IOException | TimeoutException | RootDeniedException e){
            e.printStackTrace();
            Toast.makeText(getContext(),"无法获取到ROOT权限",Toast.LENGTH_SHORT).show();
        }
        return isGranted;
    }
    //
    private int[] getCpu(){
        int[] result=new int[cpuAmount];
        for (int i=0;i<result.length;i++){
            int cpuFreq0=getCurCpuFreq("cpu"+i);
            for (int j=0;j<10;j++){
                int freq=getCurCpuFreq("cpu"+i);
                if (freq<cpuFreq0){
                    cpuFreq0=freq;
                }
            }
            result[i]=cpuFreq0;
            //Log.d("ssss",result[i]+"");
        }
        return result;
    }
    public int getCurCpuFreq(String cpu){
        ShellUtil.Result result =shellUtil.command(new String[]{"su","-c","cat","/sys/devices/system/cpu/"+cpu+"/cpufreq/scaling_cur_freq"});
        if (result.getExitValue()!=0){
            return 0;
        }
        return Integer.parseInt(result.getOutput())/1000;
    }

    public  String getCpuPlatform(Context context){
        String cpuPlatform=(String) Preference.get(context,"cpuPlatform","String");
        if (!cpuPlatform.isEmpty()){
            return  cpuPlatform;
        }
        return Build.HARDWARE;
        /*
        if (result.getExitValue()!=0)
            return "";
        String pattern = ":\\s(\\S+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(result.getOutput());
        if (m.find()){
            Preference.save(context,"cpuPlatform",result);
            return m.group(1);
        }else
            return result.getOutput();
            */
    }






    public static int getCpuAmount(){
        int result = 0;
        DataInputStream dis=null;
        DataOutputStream dos=null;
        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            dos.writeBytes("ls sys/devices/system/cpu|grep -c -E \"cpu[0-9]\"" + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = dis.readLine();
            result=Integer.parseInt(line.trim());
        } catch (Exception e) {
            e.printStackTrace();
            result =0;
        }finally {
            try {
                dos.close();
                dis.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        //Preference.save(context,"cpuAmount",result);
        return result;
    }




    public void readProp(final boolean isCreate){
        final ProgressDialog p=new ProgressDialog(getContext(),R.style.AppDialog);
        if (isCreate){
            p.setMessage("获取配置中");
            p.show();
        }
        Preference.clearAll(getContext());
        if ((Boolean) Preference.get(getContext(),"custom","Boolean")){
            p.dismiss();
            setVersion("自定义调度");
            setMode(modestring[(int)Preference.get(getContext(),"customMode","int")]);
            return;
        }
        try {
            File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/");
            if (!file.exists()){
                file.mkdirs();
            }
            String lkt=shellUtil.command(new String[]{"which","lkt"}).getOutput();
            if (lkt.isEmpty()){
                p.dismiss();
                if (!isCreate){
                    //setBusyBox("未配置");
                    setVersion("未配置");
                    setMode("未配置");
                    return;
                }
                new AlertDialog.Builder(getContext(),R.style.AppDialog)
                        .setTitle("选择调度")
                        .setItems(new String[]{"LKT调度(需按照面具模块)","YC调度(直接导入，推荐)","我要自定义"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:installLKT();break;
                                    case 1:getMain().pickPowercfg();break;
                                    case 2:getMain().switchPage(2,null);Toast.makeText(getActivity(),"请在自定义调度中进行配置",Toast.LENGTH_LONG).show();break;
                                }
                            }
                        })
                        .setCancelable(false)
                        .create().show();
            }else {
                Command command=new Command(0,"cp -f /data/LKT.prop "+Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/LKT.prop"){
                    @Override
                    public void commandCompleted(int id, int exitcode) {
                        super.commandCompleted(id, exitcode);
                        if (exitcode==0){
                            p.dismiss();
                            try{
                                passage="";
                                FileReader reader=new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/LKT.prop");
                                BufferedReader reader1=new BufferedReader(reader);
                                String line=null;
                                while ((line=reader1.readLine())!=null){
                                    passage+=line+"\n";
                                }
                                reader.close();
                                reader1.close();
                                cutVersion(passage);
                                cutMode(passage);
                            }
                            catch (FileNotFoundException e){
                                e.printStackTrace();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }else {
                            dialog.setMessage("正在切换到默认调度");
                            reset();
                            dialog.dismiss();
                        }


                    }
                };
                shell.add(command);
            }


        }catch (IOException e){
            e.printStackTrace();
        }
        p.dismiss();
    }
    private void cutVersion(String str){
        String pattern = "LKT™\\s(\\S+)";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(str);
        if (m.find( )) {
            Preference.save(getContext(),"version",true);
            setVersion(m.group(1));

        } else {
            //Toast.makeText(getContext(),)
        }
    }
    private void setVersion(String str){
        TextView version=contentView.findViewById(R.id.version);
        version.setText(str);

    }


    private void cutMode(String line){
        String pattern = "PROFILE\\s:\\s(\\S+)";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(line);
        if (m.find( )) {
            //Toast.makeText(this,m.group(1),Toast.LENGTH_SHORT).show();
            setMode(m.group(1));
        } else {
            //Toast.makeText(getContext(),)
        }

    }
    private void setMode(String str){
        TextView mode=(TextView)contentView.findViewById(R.id.mode);
        switch (str){
            case "Battery":mode.setText("省电模式");setButton("省电模式切换中",R.id.battery);break;
            case "Balanced":mode.setText("均衡模式");setButton("均衡模式切换中",R.id.balance);break;
            case "Performance":mode.setText("游戏模式");setButton("游戏模式切换中",R.id.performance);break;
            case "Turbo":mode.setText("极限模式");setButton("极限模式切换中",R.id.turbo);break;
            case "unsure":mode.setText("未完成开机配置");break;
        }
    }
    private void reset(){
        TextView mode=(TextView)contentView.findViewById(R.id.mode);
        int index=(int)Preference.get(getContext(),"default","int")+1;
        mode.setText(modeTitle[index-1]);
        Toast.makeText(getContext(),"已切换到默认模式",Toast.LENGTH_LONG).show();
        setButton("配置错误，切换到默认模式",buttonID[index-1]);
        try{
            shell.add(new Command(0,"su -c lkt "+index){
                @Override
                public void commandCompleted(int id, int exitcode) {
                    super.commandCompleted(id, exitcode);

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public static String cutBusyBox(){
        if (!RootTools.isBusyboxAvailable()){
            return "未安装";
        }else {
            return RootTools.getBusyBoxVersion();
        }
    }
    private void setBusyBox(String str){
        TextView version=contentView.findViewById(R.id.busybox_version);
        if (str.contains("#")){
            version.setText("未安装");
            installBusybox();
        }else
            version.setText(str);
    }
    public static void cmd(String str){
        try{
            Runtime.getRuntime().exec(str);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private  void disableButton(){
        for (int i:buttonID){
            ((Button)contentView.findViewById(i)).setEnabled(false);
        }
    }
    private  void enableButton(){
        for (int i:buttonID){
            ((Button)contentView.findViewById(i)).setEnabled(true);
        }
    }
    public static void installStyleB(final Context context){
        AlertDialog dialog2=new AlertDialog.Builder(context,R.style.AppDialog)
                .setTitle("已经下载到内部储存/lktMode/busybox_magisk.zip")
                .setItems(new String[]{"使用magisk安装", "重启到rec安装", "稍后再说"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                PackageManager packageManager = context.getPackageManager();
                                Intent intent= packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                                context.startActivity(intent);
                                Toast.makeText(context,"请选择内部储存/lktMode/lkt_magisk.zip安装",Toast.LENGTH_LONG);
                                break;
                            case 1:
                                try{
                                    Runtime.getRuntime().exec(
                                            new String[]{"su","-c","reboot recovery"});
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                break;
                            default:break;
                        }
                    }
                })

                .create();
        dialog2.show();
    }
    public static void installStyleL(final Context context){
        AlertDialog dialog1=new AlertDialog.Builder(context,R.style.AppDialog)
                .setTitle("已经下载内部储存/lktMode/lkt_magisk.zip")
                .setItems(new String[]{"使用magisk安装", "重启到rec安装", "稍后再说"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                PackageManager packageManager =context.getPackageManager();
                                Intent intent= packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                                context.startActivity(intent);
                                Toast.makeText(context,"请选择内部储存/lktMode/lkt_magisk.zip安装",Toast.LENGTH_LONG);
                                break;
                            case 1:
                                try{
                                    Runtime.getRuntime().exec(
                                            new String[]{"su","-c","reboot recovery"});
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                break;
                            default:break;
                        }
                    }
                })

                .create();
        dialog1.show();
    }
    private void installBusybox(){
        disableButton();
        final AlertDialog dialog=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setTitle("检测到您的设备暂未安装BusyBox，这可能使模块运行不稳定")
                .setCancelable(true)
                .setItems(new String[]{"直接安装", "安装magisk模块"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        switch (i){
                            case 0: try{
                                Uri uri = Uri.parse("market://details?id="+"stericson.busybox ");
                                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                Toast.makeText(getContext(),"安装BusyBox应用，并打开安装脚本，完成后请重启手动挡",Toast.LENGTH_LONG).show();
                            }catch(ActivityNotFoundException e){
                                Toast.makeText(getContext(), "找不到应用市场", Toast.LENGTH_SHORT).show();
                            }
                                break;
                            case 1:
                                downloadDialog.setProgress(0);
                                downloadDialog.show();
                                final String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/";
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DownloadUtil.get().download("http://puq8bljed.bkt.clouddn.com/Busybox_magisk.zip", sdcard, "busybox_magisk.zip", new DownloadUtil.OnDownloadListener() {
                                            @Override
                                            public void onDownloadSuccess(File file) {
                                                downloadDialog.dismiss();
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        installStyleB(getContext());
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onDownloading(int progress) {
                                                downloadDialog.setProgress(progress);
                                            }

                                            @Override
                                            public void onDownloadFailed(Exception e) {

                                            }
                                        });

                                    }
                                }).start();
                                break;
                        }
                    }
                })
                .create();
        dialog.show();
    }
    private void installLKT(){
        disableButton();
        downloadDialog.show();
        downloadDialog.show();
        downloadDialog.setProgress(0);
        final String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/";
        new Thread(new Runnable() {
            @Override
            public void run() {
                DownloadUtil.get().download("http://puq8bljed.bkt.clouddn.com/LKT-v1.8.zip", sdcard, "lkt_magisk.zip", new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        downloadDialog.dismiss();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                installStyleL(getContext());
                            }
                        });

                    }

                    @Override
                    public void onDownloading(int progress) {
                        downloadDialog.setProgress(progress);
                    }

                    @Override
                    public void onDownloadFailed(Exception e) {

                    }
                });

            }
        }).start();
    }





    private void initButton(){
        for (int i:buttonID){
            ((Button)contentView.findViewById(i)).setOnClickListener(this);
        }
    }
    private void setButton(String title,int id){
        Button button=contentView.findViewById(id);
        button.setTextColor(getResources().getColor(R.color.colorPress));
        button.setEnabled(false);
        for(int i:buttonID){
            if (i!=id){
                ((Button)contentView.findViewById(i)).setTextColor(getResources().getColor(R.color.colorWhite));
                ((Button)contentView.findViewById(i)).setEnabled(true);
            }
        }
        dialog.setMessage(title);
    }

    @Override
    public void onClick(View view) {
        TextView mode=(TextView)contentView.findViewById(R.id.mode);
        if ((Boolean)Preference.get(getContext(),"custom","Boolean")){
            switch (view.getId()){
                case R.id.battery:run((String)Preference.get(getContext(),"code1","String"));mode.setText("省电模式");setButton("省电模式切换中",R.id.battery);Preference.save(getContext(),"customMode",1);break;
                case R.id.balance:run((String)Preference.get(getContext(),"code2","String"));mode.setText("均衡模式");setButton("均衡模式切换中",R.id.balance);Preference.save(getContext(),"customMode",2);break;
                case R.id.performance:run((String)Preference.get(getContext(),"code3","String"));mode.setText("游戏模式");setButton("游戏模式切换中",R.id.performance);Preference.save(getContext(),"customMode",3);break;
                case R.id.turbo:run((String)Preference.get(getContext(),"code4","String"));mode.setText("极限模式");setButton("极限模式切换中",R.id.turbo);Preference.save(getContext(),"customMode",4);break;
            }
        }else
            switch (view.getId()){
                case R.id.battery:run("lkt 1");mode.setText("省电模式");setButton("省电模式切换中",R.id.battery);break;
                case R.id.balance:run("lkt 2");mode.setText("均衡模式");setButton("均衡模式切换中",R.id.balance);break;
                case R.id.performance:run("lkt 3");mode.setText("游戏模式");setButton("游戏模式切换中",R.id.performance);break;
                case R.id.turbo:run("lkt 4");mode.setText("极限模式");setButton("极限模式切换中",R.id.turbo);break;
            }
    }
    private void run(String cmd){
        //dialog.setMessage("初始化中");
        //handler.removeMessages(3);
        dialog.show();
        try{
            Command command=new Command(0,cmd);
            shell.add(command);
        }catch (IOException e){
            e.printStackTrace();
        }
        Timer timer=new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
                //handler.sendEmptyMessage(3);
            }
        };
        timer.schedule(task,3500);

    }
    private void initLKT(){
        setButton("省电模式切换中",R.id.battery);
        setMode("Battery");
        dialog.setMessage("初始化中");
        dialog.show();
        try{
            Command command=new Command(0,"lkt 1");
            shell.add(command);
        }catch (IOException e){
            e.printStackTrace();
        }
        Timer timer=new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        };
        timer.schedule(task,3500);
    }

    private void showToast(Object e){
        Toast.makeText(getContext(),String.valueOf(e),Toast.LENGTH_SHORT).show();
    }
    private boolean requetPermission() {
        boolean isGranted=false;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        }else {
            isGranted=true;
        }
        return isGranted;
    }







    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //同意权限申请
                    //readProp(true);

                }else { //拒绝权限申请
                    Toast.makeText(getContext(),"权限被拒绝了",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    private BroadcastReceiver myBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            int mtemperature=arg1.getIntExtra("temperature",0);
            battery_temp.setText("电池温度："+mtemperature/10+"°C");
        }
    };
    class LocalRecevicer extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter!=null){
                adapter.setFreq(intent.getIntArrayExtra("freq"));
                adapter.setProgress(intent.getIntArrayExtra("progress"));
                adapter.notifyDataSetChanged();
            }
            circleProgress.setProgress(intent.getIntExtra("sum",0));
            battery_current.setText("电流："+intent.getStringExtra("current"));
            cpu_temp.setText("cpu温度："+intent.getStringExtra("temp")+"°C");
        }
    }







}
