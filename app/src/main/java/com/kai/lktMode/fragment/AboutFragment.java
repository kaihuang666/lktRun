package com.kai.lktMode.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.adapter.AboutAdapter;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.ToastUtil;
import com.kai.lktMode.tool.util.net.AlipayUtil;
import com.kai.lktMode.BuildConfig;
import com.kai.lktMode.R;
import com.kai.lktMode.tool.util.net.WebUtil;
import com.kai.lktMode.widget.SimplePaddingDecoration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AboutFragment extends Fragment {
    private List<Item> data=new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_about,null,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        RecyclerView recyclerView=(RecyclerView)view.findViewById(R.id.list);
        recyclerView.addItemDecoration(new SimplePaddingDecoration(getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        AboutAdapter aboutAdapter=new AboutAdapter(getContext(),data);
        aboutAdapter.setOnItemClick(new AboutAdapter.OnItemClick() {
            @Override
            public void onClick(int i) {
                switch (i){
                    case 1:
                        if (BuildConfig.BUILD_TYPE.equals("debug")){
                            ToastUtil.shortAlert(getContext(),"测试版不能接受正式版更新");
                            return;
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final WebUtil webUtil =new WebUtil();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (webUtil.isToUpdate()){
                                            new AlertDialog.Builder(getContext(),R.style.AppDialog)
                                                    .setTitle("版本更新:"+ webUtil.getVersionName())
                                                    .setMessage(webUtil.getVersionLog())
                                                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Uri uri = Uri.parse("market://details?id="+getContext().getPackageName());
                                                            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .setNegativeButton("忽略",null)
                                                    .create().show();
                                        }else {
                                            ToastUtil.safe(getContext(),"已经是最新版本");
                                        }
                                    }
                                });

                            }
                        }).start();

                        break;
                    case 3:
                        startQQGroup(getActivity());
                        break;
                    case 4:

                        Intent intent2 = new Intent();
                        intent2.setAction("android.intent.action.VIEW");
                        Uri content_url1 = Uri.parse("https://www.jianshu.com/p/c5399a7e9bb3");//此处填链接
                        intent2.setData(content_url1);
                        startActivity(intent2);
                        break;
                    case 5:

                        Intent intent3 = new Intent();
                        intent3.setAction("android.intent.action.VIEW");
                        Uri content_url2 = Uri.parse("https://github.com/kaihuang666/lktMode");//此处填链接
                        intent3.setData(content_url2);
                        startActivity(intent3);
                        break;
                }
            }
        });
        recyclerView.setAdapter(aboutAdapter);
    }
    public static void startQQGroup(Context context){
        try {
            Intent intent1 = new Intent();
            String key="-jOcqHVCKRQFS2uIWVMUsO3AMes9Hcc0";
            intent1.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
            context.startActivity(intent1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void init(){
        data.clear();
        data.add(0,new Item("开发者","@凯帝拉克"));
        data.add(1,new Item("版本更新",BuildConfig.VERSION_NAME));
        data.add(2,new Item("app编译类型",BuildConfig.BUILD_TYPE.equals("debug")?"测试版":"正式版"));
        data.add(3,new Item("加入QQ群获取测试版",""));
        data.add(4,new Item("查看使用说明书",""));
        data.add(5,new Item("github项目",""));
    }
    @TargetApi(21)
    public static void donation(final Activity context){
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", (String)Preference.getString(context,"username"));
            cm.setPrimaryClip(mClipData);
        }catch (Exception e){
            e.printStackTrace();
        }
        AlertDialog dialog=new AlertDialog.Builder(context,R.style.AppDialog)
                .setItems(new String[]{"支付宝红包码（推荐）","支付宝捐赠", "微信赞赏","给个5星好评"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                new AlertDialog.Builder(context,R.style.AppDialog)
                                        .setView(R.layout.alipay)
                                        .setPositiveButton("保存到相册并扫码", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                BitmapDrawable bitmapDrawable=(BitmapDrawable) context.getResources().getDrawable(R.mipmap.alipay);
                                                saveBitmap(context,bitmapDrawable.getBitmap(),"ali.jpg",0);
                                            }
                                        })
                                        .setNegativeButton("残忍拒绝", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .show();
                                break;
                            case 1:
                                AlipayUtil.startAlipayClient(context,"fkx02459dc3qpqdxupmmz9b");
                                ToastUtil.longShow(context,"你的账号已经复制到剪贴板，请粘贴到付款备注里");
                                break;
                            case 2:new AlertDialog.Builder(context,R.style.AppDialog)
                                    .setView(R.layout.wxdialog)
                                    .setPositiveButton("保存到相册并扫码", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            BitmapDrawable bitmapDrawable=(BitmapDrawable) context.getResources().getDrawable(R.mipmap.mm);
                                            saveBitmap(context,bitmapDrawable.getBitmap(),"wx.jpg",1);
                                        }
                                    })
                                    .setNegativeButton("残忍拒绝", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .create().show();
                                break;
                            case 3:
                                Uri uri = Uri.parse("market://details?id="+context.getPackageName());
                                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                break;
                        }
                    }
                })
                .create();
        dialog.show();
    }
    public static void saveBitmap(Context context,Bitmap bitmap, String bitName,int type){
        String fileName ;
        File file ;
        if(Build.BRAND .equals("Xiaomi") ){ // 小米手机
            fileName = Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/"+bitName ;
        }else{  // Meizu 、Oppo
            fileName = Environment.getExternalStorageDirectory().getPath()+"/DCIM/"+bitName ;
        }
        file = new File(fileName);

        if(file.exists()){
            file.delete();
        }
        FileOutputStream out;
        try{
            out = new FileOutputStream(file);
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out))
            {
                out.flush();
                out.close();
// 插入图库
                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), bitName, null);

            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();

        }
        // 发送广播，通知刷新图库的显示
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
        if (type==0){
            try {
                Uri uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
                ToastUtil.longShow(context,"你的账号已经复制到剪贴板，请粘贴到付款备注里");
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.shortAlert(context,"请先安装支付宝");
            }

        }else {
            try {
                String cmd="am start -n com.tencent.mm/com.tencent.mm.plugin.scanner.ui.BaseScanUI";
                RootUtils.runCommand(cmd);
                ToastUtil.longShow(context,"你的账号已经复制到剪贴板，请粘贴到付款备注里");
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.shortAlert(context,"请先安装微信");
            }
        }
    }


}
