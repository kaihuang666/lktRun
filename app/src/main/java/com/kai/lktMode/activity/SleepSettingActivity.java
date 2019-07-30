package com.kai.lktMode.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kai.lktMode.R;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.tool.Preference;

public class SleepSettingActivity extends BaseActivity {
    private String[] items=new String[]{"省电模式", "均衡模式", "游戏模式","极限模式"};
    private EditText edit;
    private EditText delay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_setting);
        init();
        initImport();
        initToolBar();
    }
    private void init(){
        edit=(EditText)findViewById(R.id.edit);
        edit.setText((String) Preference.get(SleepSettingActivity.this,"code5","String"));
        delay=(EditText)findViewById(R.id.delay);
        delay.setInputType(InputType.TYPE_CLASS_NUMBER);
        delay.setText(""+(int)Preference.get(SleepSettingActivity.this,"sleepDelay",200));
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
                Preference.save(SleepSettingActivity.this,"code5",edit.getText().toString());
                Preference.save(SleepSettingActivity.this,"sleepDelay",delay.getText().toString().isEmpty()?200:Integer.parseInt(delay.getText().toString()));
                finish();
            }
        });
    }
    private void initImport(){
        TextView importMode=(TextView)findViewById(R.id.importMode);
        importMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog=new AlertDialog.Builder(SleepSettingActivity.this,R.style.AppDialog)
                        .setTitle("导入已有模式")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int mode=i+1;
                                if (!(Boolean)Preference.get(SleepSettingActivity.this,"custom","Boolean")){
                                    edit.setText("lkt "+mode);
                                }else {
                                    edit.setText((String)Preference.get(SleepSettingActivity.this,"code"+mode,"String"));
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
                                Preference.save(SleepSettingActivity.this,"code5",edit.getText().toString());
                                Preference.save(SleepSettingActivity.this,"sleepDelay",delay.getText().toString().isEmpty()?200:Integer.parseInt(delay.getText().toString()));
                                finish();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }
}
