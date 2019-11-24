package com.kai.lktMode.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.activity.PreviousActivity;
import com.kai.lktMode.bean.Device;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.cpu.CpuModel;
import com.kai.lktMode.device.CurrentManager;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.service.CommandService;
import com.kai.lktMode.tool.LKTCommand;
import com.kai.lktMode.tool.Mode;
import com.kai.lktMode.tool.util.local.CpuUtil;
import com.kai.lktMode.tool.util.net.DownloadUtil;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.adapter.ProgressAdapter;
import com.kai.lktMode.R;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.kai.lktMode.widget.DownloadDialog;
import com.kai.lktMode.widget.LktAppWidget;
import com.kai.lktMode.widget.TerminalDialog;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;

public class MainFragment extends MyFragment implements View.OnClickListener {
    private ProgressDialog dialog;
    private ProgressDialog downloadDialog;
    private CpuModel model;
    String[] modes_name=ShellUtil.concat(new String[]{"未配置"},SystemInfo.modes);
    private String[] modestring={"unsure","Battery","Balanced","Performance","Turbo"};
    private int[] buttonID={R.id.battery,R.id.balance,R.id.performance,R.id.turbo};
    private String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.INTERNET};
    private String passage;
    private List<String> cpus=new ArrayList<>();
    private ProgressAdapter adapter;
    private int cpuAmount=0;
    private DonutProgress circleProgress;
    private DonutProgress gpuProgress;
    private View contentView;
    private Mode modeManager;
    @BindView(R.id.battery_current)TextView battery_current;
    @BindView(R.id.battery_temp)TextView battery_temp;
    @BindView(R.id.cpu_temp) TextView cpu_temp;
    @BindView(R.id.cpu_info) TextView cpu_info;
    @BindView(R.id.version) TextView version;
    @BindView(R.id.mode) TextView mode;
    @BindView(R.id.busybox_version) TextView busybox_version;
    private boolean isCreated=true;
    private LocalBroadcastManager localBroadcastManager;
    private LocalRecevicer recevicer;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main,null,false);
        contentView=view;
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model=CpuModel.getInstance(getContext());
        modeManager=Mode.getInstance(getContext());
    }

    //碎片创建
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        try {
            if (getRoot()){
                setBusyBox(cutBusyBox());
                readProp();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        initButton();
        initCpuInfo();
        initDialog();
        cpu_info.setText("cpu型号："+ model.getVendor_name()+model.getModel_name());
        battery_current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current=CurrentManager.getInstance(getContext()).getRealCurrent();
                String[] currents=new String[]{current+"mA",current/10+"mA",current/100+"mA",current/1000+"mA"};
                new AlertDialog.Builder(getContext(),R.style.AppDialog)
                        .setTitle("选择你认为正确的电流")
                        .setItems(currents, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int pow=(int)Math.pow(10,i);
                                Preference.saveInt(getContext(),"current_offset",pow);
                            }
                        })
                        .show();

            }
        });
        getContext().registerReceiver(this.myBatteryReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.kai.lktMode.cpuListening");
        intentFilter.addAction("com.kai.lktMode.currentUpdate");
        intentFilter.addAction("com.kai.lktMode.tempUpdate");
        intentFilter.addAction("com.kai.lktMode.refresh");
        intentFilter.addAction("com.kai.lktMode.gpuPercent");
        localBroadcastManager=LocalBroadcastManager.getInstance(getActivity());
        recevicer=new LocalRecevicer();
        localBroadcastManager.registerReceiver(recevicer,intentFilter);

        //耗时操作的子线程
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(this.myBatteryReceiver);
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(recevicer);
    }

    //碎片被移除或替换
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void Refresh() {
        super.Refresh();
        readProp();
    }
    /**初始化控件
     *
     */
    private void initDialog(){

        downloadDialog= new ProgressDialog(getContext(),R.style.AppDialog);
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadDialog.setCancelable(false);
        downloadDialog.setMax(100);
        downloadDialog.setTitle("正在下载");
    }
    private void initCpuInfo(){
        circleProgress=(DonutProgress) contentView.findViewById(R.id.circle);
        gpuProgress=(DonutProgress)contentView.findViewById(R.id.gpu_progress);
        RecyclerView recyclerView=contentView.findViewById(R.id.recyclerview);
        adapter=new ProgressAdapter(cpus);
        StaggeredGridLayoutManager manager=new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        cpus.clear();
        cpuAmount=(int)Preference.getInt(getContext(),"cpuAmount",0);
        if (cpuAmount==0){
            cpuAmount= CpuUtil.getCpuAmount();
            Preference.saveInt(getContext(),"cpuAmount",cpuAmount);
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
            if (RootUtils.rootAccess()){
                isGranted=true;
            }else {
                RootUtils.getSU();
            }

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getContext(),"无法获取到ROOT权限",Toast.LENGTH_SHORT).show();
        }
        return isGranted;
    }

    public  String getCpuPlatform(Context context){
        String cpuPlatform=(String) Preference.getString(context,"cpuPlatform");
        if (!cpuPlatform.isEmpty()){
            return  cpuPlatform;
        }
        return Build.HARDWARE;
    }





    public void readProp(){


        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                modeManager.initalize();
                emitter.onNext(modeManager);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Object>() {
            @Override
            public void onNext(Object o) {
                version.setText(modeManager.getModeName());
                if (!modeManager.isEnable()){
                    version.setText("需要初始化");
                    new AlertDialog.Builder(getContext(),R.style.AppDialog)
                            .setTitle("初始化")
                            .setMessage("你暂未选择使用的调度，需要重新初始化选择调度")
                            .setCancelable(false)
                            .setNegativeButton("自行操作", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveButton("立即初始化", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(getContext(),PreviousActivity.class));
                                }
                            }).show();


                }

                int mode_int=modeManager.getCurrentMode();
                if (mode_int<0) {
                    mode.setText("配置错误，请切换到任意模式");
                }else {
                    setButton(buttonID[mode_int-1]);
                    mode.setText(SystemInfo.modes[modeManager.getCurrentMode()-1]+"模式");
                }

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onSubscribe(Disposable d) {

            }
        });

    }

    private void downloadPowercfg(final String url,boolean add) throws Exception{
        final String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/";
        File dir=new File(sdcard);
        if (!dir.exists())
            dir.mkdirs();
        DownloadDialog downloadDialog=new DownloadDialog.Builder(getActivity())
                .setParentDic(sdcard)
                .setProgressEnable(false)
                .setDownloadUrl(url)
                .setFileName("powercfg.sh")
                .build();
        downloadDialog.setOnTaskSuccess(new DownloadDialog.OnTaskSuccess() {
            @Override
            public void onSuccess() {
                //backupOffical();
                if (add) {
                    Toast.makeText(getContext(),"暂未获得yc调度(EAS专版)授权，仅导入了通用切换脚本，请参照网页教程自行刷入面具模块",Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.VIEW");
                    Uri content_url1 = Uri.parse("https://www.jianshu.com/p/c5399a7e9bb3");//此处填链接
                    intent2.setData(content_url1);
                    startActivity(intent2);
                }
                Preference.saveBoolean(getActivity(),"custom",true);
                downloadDialog.dismiss();
                Preference.saveString(getContext(),"code1","sh "+sdcard+"powercfg.sh powersave");
                Preference.saveString(getContext(),"code2","sh "+sdcard+"powercfg.sh balance");
                Preference.saveString(getContext(),"code3","sh "+sdcard+"powercfg.sh performance");
                Preference.saveString(getContext(),"code4","sh "+sdcard+"powercfg.sh fast");
            }
        });
        downloadDialog.show();
    }


    public  String cutBusyBox(){
        if (!RootUtils.busyboxInstalled()){
            busybox_version.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    installBusybox();
                }
            });
            return "点击安装";
        }else {
            return RootUtils.getBusyboxVersion();
        }
    }
    private void setBusyBox(String str){
        //TextView version=contentView.findViewById(R.id.busybox_version);
        if (str.contains("#")){
            busybox_version.setText("未安装");
            installBusybox();
        }else
            busybox_version.setText(str);
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
    private void installBusybox(){
        disableButton();
        final AlertDialog dialog=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setTitle("busybox安装")
                .setMessage("检测到你没有安装busybox，这将导致某些功能不正常，选择直接安装或者magisk模块安装")
                .setPositiveButton("直接安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.parse("market://details?id="+"stericson.busybox ");
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(getContext(),"安装BusyBox应用，并打开安装脚本，完成后请重启手动挡",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("模块安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/";
                        DownloadDialog downloadDialog=new DownloadDialog.Builder(getActivity())
                                .setDownloadUrl("https://www.lanzous.com/tp/i4ltpji")
                                .setFileName("busybox_magisk.zip")
                                .setProgressEnable(true)
                                .setParentDic(sdcard)
                                .build();
                        downloadDialog.setOnTaskSuccess(new DownloadDialog.OnTaskSuccess() {
                            @Override
                            public void onSuccess() {
                                downloadDialog.dismiss();
                                installWithMagisk(sdcard+"busybox_magisk.zip");
                            }
                        });
                        downloadDialog.setOnTaskFail(new DownloadDialog.OnTaskFail() {
                            @Override
                            public void onFail() {
                                downloadDialog.dismiss();
                                Toast.makeText(getContext(),"下载失败",Toast.LENGTH_SHORT).show();
                            }
                        });
                        downloadDialog.show();
                    }
                })
                .setCancelable(true)
                .create();
        dialog.show();
    }

    private void installWithMagisk(String zip){
        if (!ShellUtil.isInstalled("magisk")){
            Toast.makeText(getContext(),"设备未安装magisk，安装终止",Toast.LENGTH_SHORT).show();
            return;
        }
        final TerminalDialog dialog=new TerminalDialog(getContext());
        dialog.setCancelable(false);
        dialog.show();
        dialog.addText("正在下载安装脚本\n");
        final String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/bin";
        File dir=new File(sdcard);
        if (!dir.exists()){
            dir.mkdirs();
        }
        dialog.addText("脚本下载完成\n开始安装\n");
        DownloadDialog downloadDialog=new DownloadDialog.Builder(getActivity())
                .setParentDic(sdcard)
                .setFileName("update-binary")
                .setProgressEnable(false)
                .setDownloadUrl("https://www.lanzous.com/tp/i5lcnqh")
                .build();
        downloadDialog.setOnTaskSuccess(new DownloadDialog.OnTaskSuccess() {
            @Override
            public void onSuccess() {
                try {
                    ShellUtil.installWithMagisk(getActivity(),zip);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        downloadDialog.show();


    }
    private void installLKT(){
        final String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/";
        DownloadDialog downloadDialog=new DownloadDialog.Builder(getActivity())
                .setDownloadUrl("https://www.lanzous.com/tp/i4ltpla")
                .setFileName("lkt_magisk.zip")
                .setProgressEnable(true)
                .setParentDic(sdcard)
                .build();
        downloadDialog.setOnTaskSuccess(new DownloadDialog.OnTaskSuccess() {
            @Override
            public void onSuccess() {
                downloadDialog.dismiss();
                installWithMagisk(sdcard+"lkt_magisk.zip");
            }
        });
        downloadDialog.setOnTaskFail(new DownloadDialog.OnTaskFail() {
            @Override
            public void onFail() {
                downloadDialog.dismiss();
                Toast.makeText(getContext(),"下载失败",Toast.LENGTH_SHORT).show();
            }
        });
        downloadDialog.show();
    }





    private void initButton(){
        for (int i:buttonID){
            ((Button)contentView.findViewById(i)).setOnClickListener(this);
        }
    }
    private void setButton(int id){
        Button button=contentView.findViewById(id);
        button.setTextColor(getResources().getColor(R.color.colorPress));
        button.setEnabled(false);
        for(int i:buttonID){
            if (i!=id){
                ((Button)contentView.findViewById(i)).setTextColor(getResources().getColor(R.color.colorWhite));
                ((Button)contentView.findViewById(i)).setEnabled(true);
            }
        }
        //dialog.setMessage(title);
    }

    @Override
    public void onClick(View view) {
        TextView mode=(TextView)contentView.findViewById(R.id.mode);
        int index=0;
        for (int i=0;i<buttonID.length;i++){
            if (view.getId()==buttonID[i]){
                index=i;
                break;
            }

        }
        run(index+1);
        mode.setText(SystemInfo.modes[index]+"模式");
        setButton(view.getId());
        if ((Boolean)Preference.getBoolean(getContext(),"custom")){
            Preference.saveInt(getContext(),"customMode",index+1);
        }
    }
    private void run(int i){
        ProgressDialog dialog=new ProgressDialog(getContext(),R.style.AppDialog);
        dialog.setMessage("切换模式中");
        dialog.setCancelable(false);
        dialog.show();
        RemoteViews remoteViews = new RemoteViews(getContext().getPackageName(),
                R.layout.deskwidget);
        if ((Boolean) Preference.getBoolean(getContext(),"custom")){
            remoteViews.setTextViewText(R.id.mode,modes_name[i]);
        }else {
            remoteViews.setTextViewText(R.id.mode,"LKT");
        }
        ComponentName thisWidget = new ComponentName(getContext(), LktAppWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RootUtils.runCommand(modeManager.changeMode(i), new RootUtils.onCommandComplete() {
                        @Override
                        public void onComplete() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                }
                            });

                        }

                        @Override
                        public void onCrash(String error) {

                        }

                        @Override
                        public void onOutput(String result) {

                        }
                    });

                }
            }).start();

        }catch (Exception e){
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
        long delay=5000;
        timer.schedule(task,delay);

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
            //cpu数据的更新
            if (intent.getAction().equals("com.kai.lktMode.cpuListening")){
                for (String key:intent.getExtras().keySet()){
                    switch (key){
                        case "freq":adapter.setFreq(intent.getIntArrayExtra("freq"));break;
                        case "progress":adapter.setProgress(intent.getIntArrayExtra("progress"));break;
                        case "sum":circleProgress.setProgress(intent.getIntExtra("sum",0));break;
                        case "current":battery_current.setText(intent.getStringExtra("current"));break;
                    }
                }
                if (adapter!=null)
                    adapter.notifyDataSetChanged();
            }
            //电流信息的更新
            if (intent.getAction().equals("com.kai.lktMode.currentUpdate")){
                for (String key:intent.getExtras().keySet()){
                    if (key.equals("current")){
                        battery_current.setText(intent.getStringExtra("current"));
                    }
                }
            }
            //gpu更新
            if (intent.getAction().equals("com.kai.lktMode.gpuPercent")){
                gpuProgress.setProgress(intent.getIntExtra("percent",0));
            }
            //温度信息的更新
            if (intent.getAction().equals("com.kai.lktMode.tempUpdate")){
                for (String key:intent.getExtras().keySet()){
                    if (key.equals("temp")){
                        cpu_temp.setText("cpu温度："+intent.getStringExtra("temp")+"°C");
                    }
                }
            }
            if (intent.getAction().equals("com.kai.lktMode.refresh")){
                Refresh();
            }

        }
    }







}
