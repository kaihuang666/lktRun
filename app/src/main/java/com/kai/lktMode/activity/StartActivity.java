package com.kai.lktMode.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.fragment.MainFragment;
import com.kai.lktMode.fragment.SettingFragment;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;

import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;

public class StartActivity extends BaseActivity {
    boolean[] setting={false,false,false,false,false};
    private TextView content;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d("pass",msg.what+"");
            switch (msg.what){

                case 0:
                    content.setText("正在获取root权限");
                    try {
                        if (checkRootPathSU()){
                            if (getRoot()){
                                handler.sendEmptyMessageDelayed(1,500);
                                setting[1]=true;
                            }else {
                                content.setText("获取权限失败");
                                Toast.makeText(StartActivity.this,"即将跳转到magisk进行设置",Toast.LENGTH_SHORT);
                                handler.sendEmptyMessageDelayed(6,500);
                            }
                        }else {
                            content.setText("您的设备没有获取ROOT权限");
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    content.setText("正在检测cpu信息");
                    try {
                        int amount= MainFragment.getCpuAmount();
                        if (amount>0) {
                            Preference.save(StartActivity.this, "cpuAmount", amount);
                            setting[0] = true;
                            handler.sendEmptyMessageDelayed(2, 500);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        content.setText("cpu信息获取失败");
                    }
                    break;

                case 2:
                    content.setText("正在检测电池优化");
                    if(MainActivity.ignoreBatteryOptimization(StartActivity.this)){
                        setting[2]=true;
                        handler.sendEmptyMessageDelayed(3,500);
                    }
                    break;
                case 3:
                    content.setText("正在获取存储权限");
                    requestPermission();
                    break;
                case 4:
                    content.setText("正在获取浮窗权限");
                    if (SettingFragment.setting(null,StartActivity.this)){
                        setting[4]=true;
                        Preference.save(StartActivity.this,"isFirstRun",true);
                        finish();
                    }
                    break;
                case 6:
                    PackageManager packageManager =StartActivity.this.getPackageManager();
                    Intent intent= packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    StartActivity.this.startActivity(intent);
                    break;
                case 7:
                    Preference.save(StartActivity.this,"isFirstRun",true);
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
        if (!(Boolean)Preference.get(this,"isFirstRun","Boolean")){
            handler.sendEmptyMessage(0);
        }else {
            handler.sendEmptyMessage(7);
        }




    }

    @Override
    protected void onDestroy() {
        Preference.save(this,"firstRun", Arrays.asList(setting).contains(false));
        super.onDestroy();

    }

    public static boolean checkRootPathSU()
    {
        File f=null;
        final String kSuSearchPaths[]={"/system/bin/","/system/xbin/","/system/sbin/","/sbin/","/vendor/bin/"};
        try{
            for(int i=0;i<kSuSearchPaths.length;i++)
            {
                f=new File(kSuSearchPaths[i]+"su");
                if(f!=null&&f.exists())
                {
                    Log.i("root","find su in : "+kSuSearchPaths[i]);
                    return true;
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StartActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 17);
        }else {
            setting[3]=true;
            handler.sendEmptyMessageDelayed(4,500);
        }
    }
    public static boolean getAppOps(Context context) {
        try {
            Object object = context.getSystemService(context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = Integer.valueOf(24);
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Log.e("debug", "permissions judge: -->" + e.toString());
        }
        return false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        for (int i=0;i<setting.length;i++){
            if (!setting[i]){
                handler.sendEmptyMessage(i);
                break;
            }
        }
    }

    private boolean getRoot(){
        boolean root=false;
        Process process=null;
        DataOutputStream os=null;
        try {
            process=Runtime.getRuntime().exec("su");
            os=new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue=process.waitFor();
            if (exitValue==0){
                root=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            root=false;
        }
        return root;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 12:
                if (resultCode==RESULT_OK){
                    setting[2]=true;
                    handler.sendEmptyMessageDelayed(3,500);
                }else {
                    MainActivity.ignoreBatteryOptimization(StartActivity.this);
                }
                break;

            case 13:
                if (resultCode==RESULT_OK){
                    Preference.save(StartActivity.this,"isFirstRun",true);
                    finish();
                }else {
                    SettingFragment.setting(null,StartActivity.this);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 17:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    setting[3]=true;
                    handler.sendEmptyMessageDelayed(4,500);
                }
                else {
                    Toast.makeText(StartActivity.this,"必须授予读写权限",Toast.LENGTH_SHORT).show();
                    requestPermission();
                }
                break;
        }
    }
}
