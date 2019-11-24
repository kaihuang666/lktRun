package com.kai.lktMode.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kai.lktMode.R;
import com.kai.lktMode.adapter.LockSettingsAdapter;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.Settings;
import com.kai.lktMode.widget.SimplePaddingDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SleepSettingActivity extends BaseActivity {
    private String[] items=new String[]{"省电模式", "均衡模式", "游戏模式","极限模式"};
    private EditText delay;
    private Settings lockSettings;
    private Settings unlockSettings;
    private List<Settings.Setting> settingList=new ArrayList<>();
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @BindView(R.id.recyclerview1) RecyclerView recyclerView1;
    @BindView(R.id.actionmenuview) ActionMenuView actionMenuView;
    @BindView(R.id.edit)EditText edit;
    @BindView(R.id.clear)TextView clear;
    @BindView(R.id.importMode)TextView importMode;
    @BindView(R.id.simple_toolbar) Toolbar toolbar;
    LockSettingsAdapter adapter;
    LockSettingsAdapter adapter1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_setting);
        ButterKnife.bind(SleepSettingActivity.this);
        lockSettings= Settings.getInstance(this,true);
        unlockSettings= Settings.getInstance(this,false);
        actionMenuView.getMenu().add("保存");
        actionMenuView.getMenu().getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        actionMenuView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Preference.saveString(SleepSettingActivity.this,"code5",edit.getText().toString());
                Preference.saveInt(SleepSettingActivity.this,"sleepDelay",delay.getText().toString().isEmpty()?200:Integer.parseInt(delay.getText().toString()));
                finish();
                return true;
            }
        });
        init();
        initImport();
        initToolBar();
        adapter=new LockSettingsAdapter(this,lockSettings);
        adapter1=new LockSettingsAdapter(this,unlockSettings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.addItemDecoration(new SimplePaddingDecoration(this));
        recyclerView.setAdapter(adapter);
        recyclerView1.setAdapter(adapter1);
        adapter1.setOnItemClick(new LockSettingsAdapter.OnBottomClick() {
            @Override
            public void onClick() {
                new AlertDialog.Builder(SleepSettingActivity.this,R.style.AppDialog)
                        .setTitle("请选择需要操作的功能开关")
                        .setItems(unlockSettings.getValues().toArray(new String[]{}), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                unlockSettings.add(unlockSettings.getKeys()[i],true);
                                adapter1.notifyItemInserted(adapter1.getItemCount()-2);
                                adapter1.notifyItemRangeChanged(adapter1.getItemCount()-2,2);
                            }
                        }).show();
            }
        });
        adapter.setOnItemClick(new LockSettingsAdapter.OnBottomClick() {
            @Override
            public void onClick() {
                new AlertDialog.Builder(SleepSettingActivity.this,R.style.AppDialog)
                        .setTitle("请选择需要操作的功能开关")
                        .setItems(lockSettings.getValues().toArray(new String[]{}), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                lockSettings.add(lockSettings.getKeys()[i],false);
                                adapter.notifyItemInserted(adapter.getItemCount()-2);
                                adapter.notifyItemRangeChanged(adapter.getItemCount()-2,2);
                            }
                        }).show();

            }
        });

    }
    private void init(){

        if (! Preference.getBoolean(this,"custom"))
            edit.setFocusable(false);
        edit.setText( Preference.getString(SleepSettingActivity.this,"code5"));
        delay=(EditText)findViewById(R.id.delay);
        delay.setInputType(InputType.TYPE_CLASS_NUMBER);
        delay.setText(""+Preference.getInt(SleepSettingActivity.this,"sleepDelay",200));
        //clear=(TextView)findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setText("");
            }
        });

    }
    private void initImport(){
        importMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog=new AlertDialog.Builder(SleepSettingActivity.this,R.style.AppDialog)
                        .setTitle("导入已有模式")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int mode=i+1;
                                if (!(Boolean)Preference.getBoolean(SleepSettingActivity.this,"custom")){
                                    edit.setText("lkt "+mode);
                                }else {
                                    edit.setText((String)Preference.getString(SleepSettingActivity.this,"code"+mode));
                                }
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }
    private void initToolBar(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(SleepSettingActivity.this,R.style.AppDialog)
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
                                Preference.saveString(SleepSettingActivity.this,"code5",edit.getText().toString());
                                Preference.saveInt(SleepSettingActivity.this,"sleepDelay",delay.getText().toString().isEmpty()?200:Integer.parseInt(delay.getText().toString()));
                                finish();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }
}
