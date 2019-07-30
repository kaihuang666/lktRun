package com.kai.lktMode.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kai.lktMode.bean.Item;
import com.kai.lktMode.adapter.ListAddAdapter;
import com.kai.lktMode.R;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.TransTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GameBoostActivity extends BaseActivity {
    private String[] items=new String[]{"省电模式", "均衡模式", "游戏模式","极限模式"};
    private EditText edit;
    private ListAddAdapter adapter;
    private List<Item> settings=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_boost);
        init();
        initImport();
        initToolBar();
        initList();
    }
    private void init(){
        edit=(EditText)findViewById(R.id.edit);
        edit.setText((String) Preference.get(GameBoostActivity.this,"code6","String"));
        TextView clear=(TextView)findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setText("");
            }
        });
        Button save=(Button)findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preference.save(GameBoostActivity.this,"code6",edit.getText().toString());
                TransTool transTool=new TransTool(GameBoostActivity.this);
                transTool.save(adapter.getItems());
                finish();
            }
        });
        TextView add=(TextView)findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(GameBoostActivity.this,R.style.AppDialog)
                        .setTitle("添加附加设置")
                        .setItems(new String[]{"自动亮度", "亮度", "音量"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (!Settings.System.canWrite(GameBoostActivity.this)) {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                                Uri.parse("package:" + getPackageName()));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivityForResult(intent, 10);
                                    } else {
                                        //有了权限，你要做什么呢？具体的动作
                                    }
                                }
                                switch (i){
                                    case 0:adapter.add(new Item("自动亮度",false));break;
                                    case 1:adapter.add(new Item("亮度","50"));break;
                                    case 2:adapter.add(new Item("音量","50"));break;
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });
    }
    private void initImport(){
        TextView importMode=(TextView)findViewById(R.id.importMode);
        importMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog=new AlertDialog.Builder(GameBoostActivity.this,R.style.AppDialog)
                        .setTitle("导入已有模式")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int mode=i+1;
                                if (!(Boolean)Preference.get(GameBoostActivity.this,"custom","Boolean")){
                                    edit.setText("lkt "+mode);
                                }else {
                                    edit.setText((String)Preference.get(GameBoostActivity.this,"code"+mode,"String"));
                                }
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }
    private void initToolBar(){
        Toolbar toolbar=findViewById(R.id.simple_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(GameBoostActivity.this,R.style.AppDialog)
                        .setTitle("是否保存配置文件")
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Preference.save(GameBoostActivity.this,"code6",edit.getText().toString());
                                TransTool transTool=new TransTool(GameBoostActivity.this);
                                transTool.save(adapter.getItems());
                                finish();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }
    private void initList(){
        TransTool tool=new TransTool(this);
        settings=tool.getItems(new ArrayList((Set<String>)Preference.get(this,"gameSettings","StringSet")));
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recyclerview);
        adapter=new ListAddAdapter(this,settings);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_WRITE_SETTINGS,
                    android.os.Process.myUid(), getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
