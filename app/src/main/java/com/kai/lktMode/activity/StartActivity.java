package com.kai.lktMode.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFormatException;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.bean.Sdcard;
import com.kai.lktMode.fragment.MainFragment;
import com.kai.lktMode.fragment.PowercfgFragment;
import com.kai.lktMode.fragment.SettingFragment;
import com.kai.lktMode.network.LTE;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class StartActivity extends BaseActivity {
    boolean[] setting={false,false,false,false,false};
    private TextView content;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 7:
                    startActivity(new Intent(StartActivity.this,MainActivity.class));
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    finish();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        content=(TextView)findViewById(R.id.content);
        String command="settings put secure location_providers_allowed -gps";
        if (!Preference.getBoolean(this,"init")){
            //首次进入获取root权限
            try {
                RootTools.getShell(true);
            }catch (Exception e){
                //root权限被禁止
                String error="暂不支持您的设备";
                if (e instanceof RootDeniedException){
                    content.setText("root权限被拒绝");
                    error="手动调度在获取root权限时被拒绝，请前往superSu、magisk等权限管理软件授权";
                }else if (e instanceof TimeoutException){
                    content.setText("root权限获取超时");
                    error="手动调度获取root权限超时，请打开superSu、magisk等权限管理软件并保持后台重试";
                }else if (e instanceof  IOException){
                    content.setText("root权限获取失败");
                    error="手动调度获取root失败，设备可能未获得root权限，请检查root或尝试获取root";
                }
                new AlertDialog.Builder(StartActivity.this,R.style.AppDialog)
                        .setTitle("提示")
                        .setMessage(error)
                        .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                System.exit(1);
                            }
                        }).show();
                return;
            }
            startActivity(new Intent(this,PreviousActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        }else {
            handler.sendEmptyMessageDelayed(7,500);
        }


    }




    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }


}
