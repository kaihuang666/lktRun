package com.kai.lktMode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LabActivity extends AppCompatActivity {
    private ListLabAdapter adapter;
    private List<Item> items=new ArrayList<>();
    private List<Item> gameItems=new ArrayList<>();
    private String[] checks={"autoBoot","autoLock","gameMode"};
    private ListGameAdapter gameAdapter;;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab);

        RecyclerView recyclerView=findViewById(R.id.recyclerview);
        initList();
        initToolBar();
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter=new ListLabAdapter(items);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClick(new ListLabAdapter.OnItemClick() {
            @Override
            public void onClick(int i) {
                switch (i){
                    case 0:showDialog("开机自启模式，会在系统启动后自动切换默认模式。",null);break;
                    case 1:showDialog("锁屏沉睡功能，会在你的手机锁屏后进入超低功耗模式，选择进入修改则可以自定义调度和进入延迟", "进入修改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent1=new Intent(LabActivity.this,SleepSettingActivity.class);
                            startActivity(intent1);
                        }
                    }, "忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });break;
                    case 2:showDialog("功能设计来源于一加游戏模式\n游戏加速功能只能加速限5个横屏游戏；选择进入修改则可以自定义游戏模式的调度和操作。", "进入修改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent1=new Intent(LabActivity.this,GameBoostActivity.class);
                            startActivity(intent1);
                        }
                    }, "忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });break;
                }
            }
        });
        adapter.setOnItemCheck(new ListLabAdapter.OnItemCheck() {
            @Override
            public void onCheck(int i, Boolean isChecked) {
                Preference.save(LabActivity.this,checks[i],isChecked);
                switch (i){
                    case 1:
                        if (isChecked){
                            Intent intent=new Intent(LabActivity.this,AutoService.class);
                            intent.setAction("lockOn");
                            startService(intent);
                            showDialog("锁屏沉睡功能，会在你的手机锁屏后进入超低功耗模式；选择进入修改则可以自定义调度和进入延迟", "进入修改", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent=new Intent(LabActivity.this,SleepSettingActivity.class);
                                    startActivity(intent);
                                }
                            }, "忽略", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                        }else {
                            Intent intent=new Intent(LabActivity.this,AutoService.class);
                            intent.setAction("lockOff");
                            startService(intent);
                        }
                        break;
                    case 2:
                        if (isChecked){
                            if (hasPermission()){
                                Intent intent=new Intent(LabActivity.this,AutoService.class);
                                intent.setAction("gameOn");
                                startService(intent);
                            }else {
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                startActivityForResult(intent,10);
                            }

                        }else {
                            Intent intent=new Intent(LabActivity.this,AutoService.class);
                            intent.setAction("gameOff");
                            startService(intent);
                        }
                       break;
                }
            }
        });
        updateList();
        initGame();
    }
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    private void updateList(){
        items.get(0).setChecked((Boolean)Preference.get(LabActivity.this,"autoBoot","Boolean"));
        items.get(1).setChecked((Boolean)Preference.get(LabActivity.this,"autoLock","Boolean"));
        items.get(2).setChecked((Boolean)Preference.get(LabActivity.this,"gameMode","Boolean"));
        adapter.notifyDataSetChanged();
    }
    private void initList(){
        Item item=new Item("开机自启",false);
        Item item1=new Item("锁屏沉睡",false);
        Item item2=new Item("游戏加速",false);

        items.add(item);
        items.add(item1);
        items.add(item2);
    }
    private void initGame(){
        gameItems=new ArrayList<>();
        RecyclerView recyclerView=findViewById(R.id.gameList);
        for (String s:Preference.getGames(this)){
            gameItems.add(new Item(s,false));
        }
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        gameAdapter=new ListGameAdapter(LabActivity.this,gameItems,0);
        recyclerView.setAdapter(gameAdapter);
        gameAdapter.setBottomClick(new ListGameAdapter.OnBottomClick() {
            @Override
            public void onClick() {
                Intent intent=new Intent(LabActivity.this,AddGameActivity.class);
                startActivity(intent);
            }
        });
    }
    private void upcateGames(){
        gameItems.clear();
        for (String s:Preference.getGames(this)){
            gameItems.add(new Item(s,false));
        }
        gameAdapter.notifyDataSetChanged();
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
    private void showDialog(String str, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(LabActivity.this,R.style.AppDialog)
                .setNegativeButton("了解",listener)
                .setTitle("功能说明")
                .setMessage(str)
                .create().show();
    }
    private void showDialog(String str, String positive, DialogInterface.OnClickListener p, String negative, DialogInterface.OnClickListener n){
        new AlertDialog.Builder(LabActivity.this,R.style.AppDialog)
                .setNegativeButton(negative,n)
                .setPositiveButton(positive,p)
                .setTitle("功能说明")
                .setMessage(str)
                .create().show();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        upcateGames();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==10){
            if (hasPermission()){
                Intent intent=new Intent(LabActivity.this,AutoService.class);
                intent.setAction("gameOn");
                startService(intent);
            }else {
                Toast.makeText(LabActivity.this,"需要使用情况访问权限！",Toast.LENGTH_LONG).show();
                items.get(2).setChecked(false);
                adapter.notifyItemChanged(2);
            }
        }
    }
}
