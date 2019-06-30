package com.kai.lktMode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GameBoostActivity extends AppCompatActivity {
    private String[] items=new String[]{"省电模式", "均衡模式", "游戏模式","极限模式"};
    private EditText edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_boost);
        init();
        initImport();
        initToolBar();
    }
    private void init(){
        edit=(EditText)findViewById(R.id.edit);
        edit.setText((String)Preference.get(GameBoostActivity.this,"code6","String"));
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
                                switch (i){
                                    case 0:break;
                                    case 1:break;
                                    case 2:break;
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
                                finish();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }
}
