package com.kai.lktMode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AddGameActivity extends AppCompatActivity {
    private  List<Item> items=new ArrayList<>();
    private ListGameAdapter adapter;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        initToolBar();
        initDialog();
        initGame();
        //

    }
    private void initToolBar(){
        Toolbar toolbar=findViewById(R.id.simple_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void initGame(){
        RecyclerView recyclerView=findViewById(R.id.recyclerview);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter=new ListGameAdapter(AddGameActivity.this,items,1);
        adapter.setChangListener(new ListGameAdapter.OnCheckBoxChangListener() {
            @Override
            public void onChange(int i, Boolean b) {
                //Toast.makeText(AddGameActivity.this,i+"",Toast.LENGTH_SHORT).show();
                if (b)
                    Preference.gameAdd(AddGameActivity.this,items.get(i).getTitle());
                else
                    Preference.gameRemove(AddGameActivity.this,items.get(i).getTitle());
            }
        });
        recyclerView.setAdapter(adapter);
        Timer timer=new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getData();
                    }
                });
            }
        };
        timer.schedule(task,1000);

    }

    public void getData(){
        for (String s:scanLocalInstallAppList(this.getPackageManager())){
            items.add(new Item(s,Preference.getGames(AddGameActivity.this).contains(s)));
        }
        updateList();
    }
    public void updateList(){
        adapter.notifyDataSetChanged();
        dialog.dismiss();
    }
    private void initDialog(){
        dialog=new ProgressDialog(this,R.style.AppDialog);
        dialog.setMessage("正在加载列表");
        dialog.show();
    }
    private  List<String> scanLocalInstallAppList(PackageManager packageManager) {
        List<String> myAppInfos = new ArrayList<String>();
        try {
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                //过滤掉系统app
            if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0) {
                continue;
            }
                String myAppInfo =packageInfo.packageName;
                Log.d("installLocation",packageInfo.installLocation+"");
                if (packageInfo.applicationInfo.loadIcon(packageManager) == null||AppUtils.getAppName(AddGameActivity.this,myAppInfo).isEmpty()) {
                    continue;
                }
                myAppInfos.add(myAppInfo);
            }
        }catch (Exception e){
            //Log.e(TAG,"===============获取应用包信息失败");
        }
        return myAppInfos;
    }
}
