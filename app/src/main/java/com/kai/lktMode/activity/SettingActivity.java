package com.kai.lktMode.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.kai.lktMode.R;
import com.kai.lktMode.base.BaseActivity;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar=(Toolbar)findViewById(R.id.simple_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ActionMenuView actionMenuView=(ActionMenuView)findViewById(R.id.actionmenuview);
        actionMenuView.getMenu().add("使用说明");
        actionMenuView.getMenu().getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        actionMenuView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getOrder()==0){
                    MainActivity.showDialog(SettingActivity.this,
                            "开机自启:\n每次开机会自动切换到你所选定的默认模式\n\n默认模式:\n选择你最常用的模式，在开机自启或其他辅助功能中作为默认的模式",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                }
                return true;
            }
        });
    }
}
