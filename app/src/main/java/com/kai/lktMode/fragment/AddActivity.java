package com.kai.lktMode.fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kai.lktMode.AppUtils;
import com.kai.lktMode.Item;
import com.kai.lktMode.Preference;
import com.kai.lktMode.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AddActivity extends AppCompatActivity {
    private  List<Item> items=new ArrayList<>();
    private ListGameAdapter adapter;
    private ProgressDialog dialog;
    private String Action;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent=getIntent();
        Action=intent.getAction();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        initToolBar();
        initDialog();
        initGame();
    }
    private void initToolBar(){
        Toolbar toolbar=findViewById(R.id.simple_toolbar);
        if (Action.equals("softwares")){
            toolbar.setTitle("添加需要清理的应用");
            toolbar.setSubtitle("");
        }
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
        adapter=new ListGameAdapter(AddActivity.this,items,1,!Action.equals("softwares"));
        adapter.setChangListener(new ListGameAdapter.OnCheckBoxChangListener() {
            @Override
            public void onChange(int i, Boolean b) {
                //Toast.makeText(AddActivity.this,i+"",Toast.LENGTH_SHORT).show();
                if (b){
                    if (Action.equals("softwares")){
                        Preference.softwareAdd(AddActivity.this,items.get(i).getTitle());
                    }
                    else
                    Preference.gameAdd(AddActivity.this,items.get(i).getTitle());
                }

                else{
                    if (Action.equals("softwares")){
                        Preference.softwareRemove(AddActivity.this,items.get(i).getTitle());
                    }else
                    Preference.gameRemove(AddActivity.this,items.get(i).getTitle());
                }

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

            if (Action.equals("softwares")) {
                items.add(new Item(s, Preference.getSoftwares(AddActivity.this).contains(s)));
            }
            else {
                items.add(new Item(s, Preference.getGames(AddActivity.this).contains(s)));
            }
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
                if (packageInfo.applicationInfo.loadIcon(packageManager) == null|| AppUtils.getAppName(AddActivity.this,myAppInfo).isEmpty()) {
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
