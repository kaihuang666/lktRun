package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kai.lktMode.BuildConfig;
import com.kai.lktMode.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AboutFragment extends MyFragment {
    private List<String> data=new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_about,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        adapter=new ArrayAdapter<String >(getContext(),android.R.layout.simple_list_item_1,data);
        ListView listView=(ListView)view.findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        Uri uri = Uri.parse("market://details?id="+getContext().getPackageName());
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case 1:
                        Intent intent1 = new Intent();
                        String key="-jOcqHVCKRQFS2uIWVMUsO3AMes9Hcc0";
                        intent1.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
                        startActivity(intent1);
                        break;
                    case 2:
                        Intent intent2 = new Intent();
                        intent2.setAction("android.intent.action.VIEW");
                        Uri content_url1 = Uri.parse("https://github.com/kaihuang666/lktMode");//此处填链接
                        intent2.setData(content_url1);
                        startActivity(intent2);
                        break;
                    case 3:
                        donation(getContext());
                        break;
                }
            }
        });
        listView.setAdapter(adapter);
    }
    private void init(){
        data.clear();
        data.add(0,"版本：v"+ BuildConfig.VERSION_NAME);
        data.add(1,"加入QQ群反馈信息");
        data.add(2,"查看使用说明及源代码");
        data.add(3,"捐赠支持作者");
    }
    public static void donation(final Context context){
        AlertDialog dialog=new AlertDialog.Builder(context,R.style.AppDialog)
                .setItems(new String[]{"支付宝", "微信"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                Intent intent2 = new Intent();
                                intent2.setAction("android.intent.action.VIEW");
                                Uri content_url1 = Uri.parse("https://qr.alipay.com/fkx02459dc3qpqdxupmmz9b");//此处填链接
                                intent2.setData(content_url1);
                                context.startActivity(intent2);
                                break;
                            case 1:new AlertDialog.Builder(context,R.style.AppDialog)
                                    .setView(R.layout.wxdialog)
                                    .setPositiveButton("保存到相册并扫码", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            BitmapDrawable bitmapDrawable=(BitmapDrawable) context.getResources().getDrawable(R.mipmap.mm);
                                            saveBitmap(context,bitmapDrawable.getBitmap(),"wx.jpg");
                                        }
                                    })
                                    .setNegativeButton("残忍拒绝", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .create().show();
                                break;
                        }
                    }
                })
                .create();
        dialog.show();
    }
    public static void saveBitmap(Context context,Bitmap bitmap, String bitName){
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
        String cmd="su -c am start -n com.tencent.mm/com.tencent.mm.plugin.scanner.ui.BaseScanUI";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setToolbar(OnToolbarChange toolbar) {
        super.setToolbar(toolbar);
        toolbar.onchange("关于","");
    }
}