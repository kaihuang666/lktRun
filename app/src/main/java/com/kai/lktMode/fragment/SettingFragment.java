package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.Item;
import com.kai.lktMode.Preference;
import com.kai.lktMode.R;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends MyFragment {
    private View view;
    List<Item> items=new ArrayList<>();
    private ListAdapter adapter;
    private String[] modes={"省电模式","均衡模式","游戏模式","极限模式"};
    private String busyBoxInfo="";
    private ProgressDialog downloadDialog;
    private String passage;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_setting,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            passage=this.getArguments().getString("passage");
        }catch (Exception e){
            e.printStackTrace();
        }
        downloadDialog= new ProgressDialog(getContext(),R.style.AppDialog);
        downloadDialog.setCancelable(false);
        downloadDialog.setTitle("正在下载");
        Item item=new Item("LKT模块","点击安装");
        Item item1=new Item("BusyBox模块","点击安装");
        Item item2=new Item("默认模式","省电模式");
        Item item3=new Item("通知栏磁贴",false);
        Item item0=new Item("开机自启",false);
        items.add(item0);
        items.add(item);
        items.add(item1);
        items.add(item2);
        items.add(item3);
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview);
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter=new ListAdapter(items);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemCheck(new ListAdapter.OnItemCheck() {
            @Override
            public void onCheck(int i, Boolean isChecked, CompoundButton compoundButton) {
                if (isChecked){
                    if (i==4){
                        setting(compoundButton);
                    }
                }
            }
        });
        adapter.setOnItemClick(new ListAdapter.OnItemClick() {
            @Override
            public void onClick(int i) {
                switch (i){
                    case 1:
                        if ((Boolean) Preference.get(getContext(),"version","Boolean")){
                            AlertDialog dialog=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                                    .setMessage(passage)
                                    .setTitle("配置文件")
                                    .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .create();
                            dialog.show();
                        }else {
                            installLKT();
                        }

                        break;
                    case 2:
                        if((Boolean) Preference.get(getContext(),"busybox","Boolean")){
                            try{
                                if (busyBoxInfo.isEmpty()) {
                                    RootTools.getShell(false).add(new Command(0, "busybox --help") {
                                        @Override
                                        public void commandOutput(int id, String line) {
                                            super.commandOutput(id, line);
                                            busyBoxInfo+=line+"\n";
                                        }

                                        @Override
                                        public void commandCompleted(int id, int exitcode) {
                                            super.commandCompleted(id, exitcode);
                                            showBusyboxInfo();
                                        }
                                    });
                                }else {
                                    showBusyboxInfo();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else {
                            installBusybox();
                        }

                        break;
                    case 3:

                        AlertDialog dialog1=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                                .setTitle("设置默认模式")
                                .setSingleChoiceItems(modes, (int) Preference.get(getContext(), "default", "int"), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Preference.save(getContext(),"default",Integer.valueOf(i));
                                        updateList();
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create();
                        dialog1.show();
                        break;

                }

            }
        });
        updateList();
    }
    private void updateList(){
        items.clear();
        Item item0=new Item("开机自启",(Boolean) Preference.get(getContext(),"autoBoot","Boolean"));
        Item item=new Item("LKT模块",((Boolean) Preference.get(getContext(),"version","Boolean"))?"已安装":((Boolean)Preference.get(getContext(),"custom","Boolean"))?"自定义调度":"点击安装");
        Item item1=new Item("BusyBox模块",((Boolean) Preference.get(getContext(),"busybox","Boolean"))?"已安装":((Boolean)Preference.get(getContext(),"custom","Boolean"))?"自定义调度":"点击安装");
        Item item2=new Item("默认模式",modes[(int)Preference.get(getContext(),"default","int")]);
        Item item3=new Item("通知栏磁贴",true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            item3.setChecked(false);
        }else if (!Settings.canDrawOverlays(getContext())){
            item3.setChecked(false);
        }
        items.add(item0);
        items.add(item);
        items.add(item1);
        items.add(item2);
        items.add(item3);
        adapter.notifyDataSetChanged();

    }
    private void showBusyboxInfo(){
        AlertDialog dialog=new AlertDialog.Builder(getContext(), R.style.AppDialog)
                .setMessage(busyBoxInfo)
                .setTitle("Busybox版本信息")
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        dialog.show();
    }
    private void installBusybox(){
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
                                                MainFragment.installStyleB(getContext());
                                            }

                                            @Override
                                            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                            }

                                            @Override
                                            protected void error(BaseDownloadTask task, Throwable e) {
                                                downloadDialog.dismiss();

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
    private void setting(CompoundButton compoundButton){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(getContext())) {
                Toast.makeText(getContext(),"已授权",Toast.LENGTH_SHORT).show();
            } else {
                //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getActivity().getPackageName()));
                Toast.makeText(getContext(),"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }

        }else {
            Toast.makeText(getContext(),"版本过低",Toast.LENGTH_SHORT).show();
            compoundButton.setChecked(false);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void installLKT(){
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
                                .setPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/",true)
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
                                        MainFragment.installStyleL(getContext());
                                    }

                                    @Override
                                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                    }

                                    @Override
                                    protected void error(BaseDownloadTask task, Throwable e) {
                                        downloadDialog.dismiss();
                                        e.printStackTrace();
                                        Toast.makeText(getContext(),e.toString(),Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    protected void warn(BaseDownloadTask task) {
                                    }
                                }).start();
                        /*
                        Aria.download(getContext())
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

    @Override
    public void setToolbar(OnToolbarChange toolbar) {
        super.setToolbar(toolbar);
        toolbar.onchange("设置","");
    }
}