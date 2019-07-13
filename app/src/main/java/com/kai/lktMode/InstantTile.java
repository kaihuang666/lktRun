package com.kai.lktMode;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TargetApi(24)
public class InstantTile extends TileService{
    private int now=0;
    private String[] items=new String[]{"省电模式", "均衡模式", "游戏模式","极限模式"};
    private String[] tileLabels=new String[]{"省电模式", "均衡模式", "游戏模式","极限模式"};
    private int[] lalelIds={R.mipmap.battery_tile,R.mipmap.balance_tile,R.mipmap.performance_tile,R.mipmap.turbo_tile,R.mipmap.icon_tile};
    Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what==4){
                int mode=msg.getData().getInt("mode");
                switchMode(mode);
                DialogTile.collapseStatusBar(getApplicationContext());
                //Log.d("sss",now+"");
            }
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();
        readMode();
    }
    @Override
    public void onStopListening() {
        super.onStopListening();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        if ((Boolean)Preference.get(getApplicationContext(),"autoLock","Boolean")){
            if(!ServiceStatusUtils.isServiceRunning(getApplicationContext(), AutoService.class)){
                Intent service=new Intent(getApplicationContext(),AutoService.class);
                startService(service);
                Log.d("重启服务","success");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterScreenActionReceiver();
    }



    @Override
    public void onClick() {
        super.onClick();
        handler.removeMessages(4);
        int mode=0;
        Tile tile=getQsTile();
        int now=Arrays.asList(tileLabels).indexOf(tile.getLabel());
        if (now<0){
            now=0;
        }
        if (now==3){
            mode=0;
        }else {
            mode=now+1;
        }
        showIcon(tileLabels[mode],lalelIds[mode]);
        Message message=new Message();
        Bundle bundle=new Bundle();
        bundle.putInt("mode",mode);
        message.what=4;
        message.setData(bundle);
        handler.sendMessageDelayed(message,2000);
        //collapseStatusBar(getBaseContext());
        //showDialog(now);

    }
    private void readMode(){
        if ((Boolean) Preference.get(getApplicationContext(),"custom","Boolean")){
            int customMode=(int)Preference.get(this,"customMode","int")-1;
            if (customMode<0){
                customMode=0;
            }
            showIcon(tileLabels[customMode],lalelIds[customMode]);
            now=customMode;
            return;
        }
        try{
            Shell shell=RootTools.getShell(true);
            shell.add(new Command(1,"grep PROFILE /data/LKT.prop"){
                @Override
                public void commandOutput(int id, String line) {
                    super.commandOutput(id, line);
                    //Toast.makeText(getBaseContext(),line,Toast.LENGTH_SHORT).show();
                    cutMode(line);
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    super.commandCompleted(id, exitcode);
                    if (exitcode!=0){
                        showIcon(tileLabels[4],lalelIds[4]);
                    }
                }
            });

        }catch (IOException| TimeoutException| RootDeniedException e){
            e.printStackTrace();
            showIcon(tileLabels[4],lalelIds[4]);
        }
    }

    private void cutMode(String line){
        int mode=(int)Preference.get(getApplicationContext(),"default","int");
        String pattern = "PROFILE\\s:\\s(\\S+)";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(line);
        if (m.find( )) {
            //Toast.makeText(getApplicationContext(),m.group(1),Toast.LENGTH_SHORT).show();
            switch (m.group(1)){
                case "Battery":showIcon(tileLabels[0],lalelIds[0]);now=0;break;
                case "Balanced":showIcon(tileLabels[1],lalelIds[1]);now=1;break;
                case "Performance":showIcon(tileLabels[2],lalelIds[2]);now=2;break;
                case "Turbo":showIcon(tileLabels[3],lalelIds[3]);now=3;break;
                default:showIcon(tileLabels[mode],lalelIds[mode]);Toast.makeText(getApplicationContext(),"配置错误，切换到默认模式",Toast.LENGTH_SHORT).show();
                switchMode(mode);
                break;
            }
        } else {
            //Toast.makeText(MainActivity.this,)
        }

    }

    public void showDialog(final int mode){
        AlertDialog dialog=new AlertDialog.Builder(getApplicationContext(),R.style.AppDialog)
                .setTitle("选择调度模式")
                .setSingleChoiceItems(items,now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==mode){

                        }else {
                            dialogInterface.cancel();
                            showIcon(tileLabels[i],lalelIds[i]);
                            switchMode(i);
                        }

                    }
                })
                .create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        try {
            dialog.show();
        }catch (WindowManager.BadTokenException e){
            e.printStackTrace();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(),"已授权",Toast.LENGTH_SHORT).show();
                } else {
                    //若没有权限，提示获取.
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" +getApplicationContext().getPackageName()));
                    Toast.makeText(getApplicationContext(),"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }else {
                Toast.makeText(getApplicationContext(),"版本过低",Toast.LENGTH_SHORT).show();
                //compoundButton.setChecked(false);
            }
        }

    }
    private void switchMode(int mode){
        Intent serviceIntent;
        if ((Boolean) Preference.get(getApplicationContext(),"custom","Boolean")){
            serviceIntent = new Intent(getApplicationContext(), CustomCommandService.class);
            Preference.save(getApplicationContext(),"customMode",mode+1);
        }else
            serviceIntent=new Intent(getApplicationContext(),CommandService.class);
        serviceIntent.putExtra("mode",mode+1);
        startService(serviceIntent);
    }
    private void showIcon(String label,int resId){
        Tile tile=getQsTile();
        tile.setIcon(Icon.createWithResource(getApplicationContext(),resId));
        tile.setLabel(label);
        tile.updateTile();
    }




}
