package com.kai.lktMode.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.kai.lktMode.Preference;
import com.kai.lktMode.ProgressAdapter;
import com.kai.lktMode.R;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;

public class MainFragment extends MyFragment implements View.OnClickListener {

    private AlertDialog dialog;
    private ProgressDialog downloadDialog;
    private String[] modeTitle={"省电模式","均衡模式","游戏模式","极限模式"};
    private String[] modestring={"unsure","Battery","Balanced","Performance","Turbo"};
    private int[] buttonID={R.id.battery,R.id.balance,R.id.performance,R.id.turbo};
    private Shell shell;
    private String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.INTERNET};
    private String passage;
    private boolean isLktInstalled=false;
    private boolean isBusyboxInstalled=false;
    private List<String> cpus=new ArrayList<>();
    private ProgressAdapter adapter;
    private int cpuAmount=0;
    private CircleProgress circleProgress;
    private View contentView;
    private HandlePassage handlePassage;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int[] freq=msg.getData().getIntArray("freq");
            int mean=msg.getData().getInt("mean");
            circleProgress.setProgress(mean);
            adapter.setFreq(freq);
            adapter.notifyDataSetChanged();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MainFragment.this.sendMessage();
                }
            }).start();

            super.handleMessage(msg);
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main,container,false);
        contentView=view;
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FileDownloader.setup(getContext());
        initButton();
        initDialog();
        getRoot();
        //initCpuInfo();
    }

    private int[] getCpu(){
        int[] result=new int[cpuAmount];
        for (int i=0;i<result.length;i++){
            int cpuFreq0=ProgressAdapter.getCurCpuFreq("cpu"+i);
            for (int j=0;j<6;j++){
                int cpuFreq=ProgressAdapter.getCurCpuFreq("cpu"+i);
                if (cpuFreq<cpuFreq0)
                    cpuFreq0=cpuFreq;
            }
            result[i]=cpuFreq0;
        }
        return result;
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
        return result;
    }




    public void readProp(final boolean isCreate){
        Preference.clearAll(getContext());
        if ((Boolean) Preference.get(getContext(),"custom","Boolean")){
            setBusyBox("已忽略");
            setVersion("自定义调度");
            setMode(modestring[(int)Preference.get(getContext(),"customMode","int")]);
            dialog.dismiss();
            return;
        }
        try {
            cmd("mkdir "+ Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/");
            Command command=new Command(0,"cp -f /data/LKT.prop "+Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/LKT.prop"){
                @Override
                public void commandCompleted(int id, int exitcode) {
                    super.commandCompleted(id, exitcode);
                    if (exitcode==0){
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
                            cutBusyBox(passage);
                            //Toast.makeText(getContext(),passage,Toast.LENGTH_LONG).show();
                        }
                        catch (FileNotFoundException e){
                            e.printStackTrace();
                            //installLKT();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        finally {
                            dialog.dismiss();
                        }
                    }else {
                        if (!isCreate)
                            return;
                        try{
                            RootTools.getShell(false).add(new Command(0,"lkt"){
                                @Override
                                public void commandCompleted(int id, int exitcode) {
                                    super.commandCompleted(id, exitcode);
                                    if (exitcode==0) {
                                        dialog.dismiss();
                                        setVersion("已安装");
                                        setMode("unsure");
                                        setBusyBox("未完成开机配置");
                                        Toast.makeText(getContext(),"还未完成开机配置，5秒后执行默认模式",Toast.LENGTH_LONG).show();
                                        TimerTask task=new TimerTask() {
                                            @Override
                                            public void run() {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        reset();
                                                    }
                                                });
                                            }
                                        };
                                        Timer timer=new Timer();
                                        timer.schedule(task,5000);
                                    }else {
                                        dialog.dismiss();
                                        installLKT();
                                    }
                                }

                            });
                        }catch (Exception e){

                        }

                        //installLKT();
                    }


                }
            };
            shell.add(command);

        }catch (IOException e){
            e.printStackTrace();
        }


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
            default:mode.setText("错误配置");reset();break;
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
    private void cutBusyBox(String str){
        String pattern = "BUSYBOX\\s:\\s(\\S+)";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(str);
        if (m.find( )) {
            //showToast(m.group(1));
            Preference.save(getContext(),"busybox",true);
            setBusyBox(m.group(1));
        } else {
            installBusybox();
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
    /*
    @Download.onTaskRunning protected void running(DownloadTask task) {
        int p = task.getPercent();	//任务进度百分比
        downloadDialog.setMessage("下载进度："+p+"%");
        downloadDialog.show();
    }

    @Download.onTaskComplete void taskComplete(DownloadTask task) {
        //在这里处理任务完成的状态
        downloadDialog.dismiss();
        switch (task.getTaskName()){
            case "lkt_magisk.zip":

                break;
                case "busybox_magisk.zip":

                break;
        }
    }*/
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
                .setTitle("busybox模块已经下载到内部储存/lktMode/lkt_magisk.zip")
                .setItems(new String[]{"使用magisk安装", "重启到rec安装", "稍后再说"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                PackageManager packageManager = context.getPackageManager();
                                Intent intent= packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                                context.startActivity(intent);
                                Toast.makeText(context,"请在<模块>中选择内部储存/lktMode/lkt_magisk.zip安装",Toast.LENGTH_LONG);
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
                .setTitle("lkt模块已经下载到内部储存/lktMode/lkt_magisk.zip")
                .setItems(new String[]{"使用magisk安装", "重启到rec安装", "稍后再说"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                PackageManager packageManager =context.getPackageManager();
                                Intent intent= packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                                context.startActivity(intent);
                                Toast.makeText(context,"请在<模块>中选择内部储存/lktMode/lkt_magisk.zip安装",Toast.LENGTH_LONG);
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
                            case 1:downloadDialog.show();
                                FileDownloader.getImpl().create("https://files.catbox.moe/5t8g9z.zip")
                                        .setPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/busybox_magisk.zip")
                                        .setForceReDownload(true)
                                        .setListener(new FileDownloadListener() {
                                            @Override
                                            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                            }
                                            @Override
                                            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                                int p=soFarBytes*100/totalBytes;
                                                downloadDialog.setMessage("下载进度："+p+"%");
                                                downloadDialog.show();
                                            }
                                            @Override
                                            protected void completed(BaseDownloadTask task) {
                                                downloadDialog.dismiss();
                                                installStyleB(getContext());
                                            }

                                            @Override
                                            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                            }

                                            @Override
                                            protected void error(BaseDownloadTask task, Throwable e) {
                                                downloadDialog.dismiss();
                                                showToast("下载失败");
                                            }

                                            @Override
                                            protected void warn(BaseDownloadTask task) {
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
        final AlertDialog dialog1=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setTitle("检测到您的设备暂未安装LKT模块")
                .setMessage("是否下载LKT magisk模块到您的设备？")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        downloadDialog.show();
                        FileDownloader.getImpl().create("https://files.catbox.moe/9ik95m.zip")
                                .setPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lkt_magisk.zip")
                                .setForceReDownload(true)
                                .setCallbackProgressTimes(300)
                                .setMinIntervalUpdateSpeed(400)
                                .setListener(new FileDownloadListener() {
                                    @Override
                                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                    }
                                    @Override
                                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                        int p=soFarBytes*100/totalBytes;
                                        downloadDialog.setMessage("下载进度："+p+"%");
                                        downloadDialog.show();
                                    }
                                    @Override
                                    protected void completed(BaseDownloadTask task) {
                                        downloadDialog.dismiss();
                                        installStyleL(getContext());
                                    }

                                    @Override
                                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                    }

                                    @Override
                                    protected void error(BaseDownloadTask task, Throwable e) {
                                        downloadDialog.dismiss();
                                        e.printStackTrace();
                                        //Toast.makeText(getContext(),e.toString(),7000).show();
                                    }

                                    @Override
                                    protected void warn(BaseDownloadTask task) {
                                    }
                                }).start();
                        /*
                        Aria.download(this)
                                .load("https://files.catbox.moe/9ik95m.zip")     //读取下载地址
                                .setFilePath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/lkt_magisk.zip") //设置文件保存的完整路径
                                .start();   //启动下载*/


                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false)
                .create();
        dialog1.show();
    }





    private void getRoot(){
        try {
            if (RootTools.isRootAvailable()){
                //dialog.setMessage("获取配置中");
                //dialog.show();
                shell=RootTools.getShell(true);
                requetPermission();
                initCpuInfo();
            }else {
                Runtime.getRuntime().exec("su");
                //dialog.setMessage("正在获取root权限");
                //dialog.show();
                shell=RootTools.getShell(true);
                requetPermission();
            }

        }catch (IOException | TimeoutException | RootDeniedException e){
            e.printStackTrace();
            Toast.makeText(getContext(),"无法获取到ROOT权限",Toast.LENGTH_SHORT).show();
        }
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
    private void initDialog(){
        dialog= new SpotsDialog.Builder()
                .setContext(getContext())
                .setCancelable(false)
                .setMessage("模式切换中")
                .build();
        downloadDialog= new ProgressDialog(getContext(),R.style.AppDialog);
        downloadDialog.setCancelable(false);
        downloadDialog.setTitle("正在下载");
    }

    private void showToast(Object e){
        Toast.makeText(getContext(),String.valueOf(e),Toast.LENGTH_SHORT).show();
    }
    private void requetPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        }else {
            readProp(true);
        }
    }

    public String getPassage(){
        return passage;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            shell=RootTools.getShell(true);
            //adapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            readProp(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeMessages(3);
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
                    readProp(true);

                }else { //拒绝权限申请
                    Toast.makeText(getContext(),"权限被拒绝了",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void initCpuInfo(){
        circleProgress=(CircleProgress)contentView.findViewById(R.id.circle);
        RecyclerView recyclerView=contentView.findViewById(R.id.recyclerview);
        adapter=new ProgressAdapter(getContext(),cpus);
        StaggeredGridLayoutManager manager=new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        cpus.clear();
        cpuAmount=getCpuAmount();
        for (int i=0;i<cpuAmount;i++){
            cpus.add(i,"cpu"+i);
        }
        adapter.notifyDataSetChanged();
        sendMessage();

    }
    private void sendMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int[] freq=getCpu();
                int sum=0;
                for (int i=0;i<cpuAmount;i++){
                    sum+=(int) 100*freq[i]/adapter.maxfreq[i];
                }
                Message message=new Message();
                Bundle a=new Bundle();
                a.putIntArray("freq",freq);
                a.putInt("mean",sum/cpuAmount);
                message.setData(a);
                message.what=3;
                handler.sendMessage(message);
            }
        }).start();
    }

    interface HandlePassage{
        void passage(String passage);
    }
}
