package com.kai.lktMode.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.DownloadUtil;
import com.kai.lktMode.Item;
import com.kai.lktMode.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.ShellUtil;
import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingFragment extends MyFragment {
    private View view;
    List<Item> items=new ArrayList<>();
    private ListAdapter adapter;
    private String[] modes={"省电模式","均衡模式","游戏模式","极限模式"};
    private String busyBoxInfo="";
    private ProgressDialog downloadDialog;
    private String passage;
    private ShellUtil shellUtil;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_setting,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        shellUtil=ShellUtil.create(true);
        passage=shellUtil.command(new String[]{"su","-c","cat","/data/LKT.prop"}).getOutput();
        downloadDialog= new ProgressDialog(getContext(),R.style.AppDialog);
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadDialog.setCancelable(false);
        downloadDialog.setMax(100);
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
                if (i==0){
                    Preference.save(getContext(),"autoBoot",isChecked);
                }
                if (isChecked){
                    Toast.makeText(getActivity(),"你还需要自行前往设置-电池-电池优化，取消手动调度的电池优化",Toast.LENGTH_LONG).show();
                    if (i==4){
                        setting(compoundButton,getActivity());
                    }
                }
            }
        });
        adapter.setOnItemClick(new ListAdapter.OnItemClick() {
            @Override
            public void onClick(int i) {
                switch (i){
                    case 1:
                        String isInstalledL=shellUtil.command(new String[]{"which","lkt"}).getOutput();
                        if (isInstalledL.isEmpty()){
                            installLKT();
                            return;
                        }
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

                        break;
                    case 2:
                            try{
                                if (!RootTools.isBusyboxAvailable()){
                                    installBusybox();
                                    return;
                                }
                                busyBoxInfo=shellUtil.command(new String[]{"busybox","--help"}).getOutput();
                                showBusyboxInfo();
                            }catch (Exception e){
                                e.printStackTrace();
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
        Item item=new Item("LKT模块",!shellUtil.command(new String[]{"which","lkt"}).getOutput().isEmpty()?"已安装":((Boolean)Preference.get(getContext(),"custom","Boolean"))?"自定义调度":"点击安装");
        Item item1=new Item("BusyBox模块",RootTools.isBusyboxAvailable()?RootTools.getBusyBoxVersion():"未安装");
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

    @Override
    public void Refresh() {
        super.Refresh();
        updateList();
    }

    private void installBusybox(){
        final AlertDialog dialog=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setTitle("安装Busybox")
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
                                downloadDialog.show();
                                downloadDialog.setProgress(0);
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
                                                        MainFragment.installStyleB(getContext());
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
    public static boolean  setting(CompoundButton compoundButton, Activity context){
        boolean isGranted=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(context)) {
                isGranted=true;
                Toast.makeText(context,"已授权",Toast.LENGTH_SHORT).show();
            } else {
                //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                Toast.makeText(context,"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
                context.startActivityForResult(intent,13);
            }

        }else {
            //Toast.makeText(context,"版本过低",Toast.LENGTH_SHORT).show();
            if (compoundButton!=null)
            compoundButton.setChecked(false);
            if (StartActivity.getAppOps(context)){
                isGranted=true;
            }else {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                context.startActivityForResult(intent,13);
            }
        }
        return isGranted;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void installLKT(){
        final AlertDialog dialog1=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setTitle("下载")
                .setMessage("是否下载LKT magisk模块到您的设备？")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
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
                                                MainFragment.installStyleL(getContext());
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


}