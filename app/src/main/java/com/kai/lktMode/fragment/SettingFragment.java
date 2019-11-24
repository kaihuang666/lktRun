package com.kai.lktMode.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.activity.StartActivity;
import com.kai.lktMode.adapter.ListAdapter;
import com.kai.lktMode.bean.Device;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.permission.FloatWindowManager;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.LKTCommand;
import com.kai.lktMode.tool.util.net.DownloadUtil;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.kai.lktMode.tool.util.net.WebUtil;
import com.kai.lktMode.widget.DownloadDialog;
import com.kai.lktMode.widget.SimplePaddingDecoration;
import com.kai.lktMode.widget.TerminalDialog;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    private View view;
    List<Item> items=new ArrayList<>();
    private ListAdapter adapter;
    private String[] modes={"省电模式","均衡模式","游戏模式","极限模式"};
    private String busyBoxInfo="";
    private String passage;
    private ProgressDialog progressDialog;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_setting,null,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog=new ProgressDialog(getContext(),R.style.AppDialog);
        progressDialog.setMessage("主题切换中");
        passage=new RootFile("/data/LKT.prop").readFile();
        Item item=new Item("LKT模块","点击安装");
        Item item1=new Item("BusyBox工具集","点击安装");
        Item item2=new Item("默认模式","省电模式");
        Item item3=new Item("通知栏磁贴",false);
        Item item0=new Item("开机自启",false);
        Item item4=new Item("夜间模式",false);

        items.add(item0);
        items.add(item);
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview);
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new SimplePaddingDecoration(getContext()));
        adapter=new ListAdapter(items);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemCheck(new ListAdapter.OnItemCheck() {
            @Override
            public void onCheck(int i, Boolean isChecked, CompoundButton compoundButton) {
                if (i==0){
                    Preference.saveBoolean(getContext(),"autoBoot",isChecked);
                    Toast.makeText(getContext(),"你还需要自行前往设置-电池-电池优化，取消手动调度的电池优化",Toast.LENGTH_SHORT).show();
                }
                if (i==4){
                    if (isChecked){
                        Toast.makeText(getContext(),"你还需要自行前往设置-电池-电池优化，取消手动调度的电池优化",Toast.LENGTH_SHORT).show();

                        setting(compoundButton,getActivity());
                    }

                }
                if (i==5){
                    progressDialog.show();
                    Preference.saveBoolean(getContext(),"nightMode",isChecked);
                    if (isChecked){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    Intent intent=new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }


            }
        });
        adapter.setOnItemClick(new ListAdapter.OnItemClick() {
            @Override
            public void onClick(int i) {
                switch (i){
                    case 1:
                        if (!LKTCommand.isLKTInstalled()){
                            new AlertDialog.Builder(getActivity(),R.style.AppDialog)
                                    .setTitle("LKT安装方式")
                                    .setMessage("选择直接安装，LKT脚本会直接集成到软件内，更方便快捷；如果你安装了magisk框架，你可以选择使用模块安装LKT")
                                    .setPositiveButton("模块安装", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            installLKT();
                                        }
                                    })
                                    .show();

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
                                if (!RootUtils.busyboxInstalled()){
                                    installBusybox();
                                    return;
                                }
                                busyBoxInfo= RootUtils.runCommand("busybox --help");
                                        //ShellUtil.command(new String[]{"busybox","--help"});
                                showBusyboxInfo();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        break;
                    case 3:

                        AlertDialog dialog1=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                                .setTitle("设置默认模式")
                                .setSingleChoiceItems(modes,  Preference.getInt(getContext(), "default"), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Preference.saveInt(getContext(),"default",i);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }

    private void updateList(){
        items.clear();
        Item item0=new Item("开机自启",(Boolean) Preference.getBoolean(getContext(),"autoBoot"));
        Item item=new Item("LKT模块",LKTCommand.isLKTInstalled()?"已安装":(Preference.getBoolean(getContext(),"custom"))?"自定义调度":"点击安装");
        Item item1=new Item("BusyBox工具集",RootUtils.busyboxInstalled()?RootUtils.getBusyboxVersion():"未安装");
        Item item2=new Item("默认模式",modes[(int)Preference.getInt(getContext(),"default")]);
        Item item3=new Item("通知栏磁贴",true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            item3.setChecked(false);
        }else if (!Settings.canDrawOverlays(getContext())){
            item3.setChecked(false);
        }
        Item item4=new Item("夜间模式",(Boolean)Preference.getBoolean(getContext(),"nightMode"));
        items.add(item0);
        items.add(item);
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
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
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ShellUtil.installWithMagisk(getActivity(),sdcard+"busybox_magisk.zip");
                                    }
                                });

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

    public static boolean  setting(CompoundButton compoundButton, Activity context){
        boolean isGranted=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Settings.canDrawOverlays(context)) {
                isGranted=true;

            } else {
                //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                Toast.makeText(context,"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
                context.startActivityForResult(intent,13);
            }

        }else {
            if (compoundButton!=null)
            compoundButton.setChecked(false);
            isGranted=FloatWindowManager.getInstance().check(context);
        }
        return isGranted;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShellUtil.installWithMagisk(getActivity(),sdcard+"lkt_magisk.zip");
                    }
                });

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


}