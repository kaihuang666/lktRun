package com.kai.lktMode.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.Item;
import com.kai.lktMode.Preference;
import com.kai.lktMode.R;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PowercfgFragment extends MyFragment {
    private List<Item> items=new ArrayList<>();
    private int[] colors={R.color.colorBattery,R.color.colorBalance,R.color.colorPerformance,R.color.colorTurbo};
    private PowercfgAdapter adapter;
    private String output;
    private View view;
    private TextView title;
    private Button change;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_powercfg,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initList();

    }
    public void saveAll(){
        adapter.saveAll();
    }
    private void initList(){
        items.clear();
        title=view.findViewById(R.id.list_title);
        change=view.findViewById(R.id.change);
        final String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh";
        File shell=new File(sdcard);
        if (!shell.exists()){
            change.setText("导入");
            title.setText("动态脚本：未导入");
        }
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (change.getText().equals("导入")){
                    Intent importShell = new Intent(Intent.ACTION_GET_CONTENT);
                    importShell.setType("*/*");
                    importShell.addCategory(Intent.CATEGORY_OPENABLE);
                    try {
                        startActivityForResult(importShell,17);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getContext(),"找不到系统文件管理器", Toast.LENGTH_SHORT);
                    }
                }else {
                    try {
                        RootTools.getShell(true).add(new Command(4,"rm "+sdcard){
                            @Override
                            public void commandCompleted(int id, int exitcode) {
                                super.commandCompleted(id, exitcode);
                                if (exitcode==0){
                                    change();
                                }
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        items.add(new Item("省电",""));
        items.add(new Item("均衡",""));
        items.add(new Item("游戏",""));
        items.add(new Item("极限",""));
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview);
        final LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter=new PowercfgAdapter(getContext(),items,colors);
        adapter.setOnItemClick(new PowercfgAdapter.OnItemClick() {
            @Override
            public void onClick(int i, String a) {
                showTerminal(a);
            }
        });

        recyclerView.setAdapter(adapter);
        if (!shell.exists()){
            //adapter.closeAll();
        }else {
            updateList();
        }

    }
    private void change(){
        if (change.getText().equals("移除")){
            change.setText("导入");
            title.setText("动态脚本：未导入");
            for (int i=1;i<=4;i++){
                Preference.save(getContext(),"code"+i,"");
            }
            adapter.closeAll();
        }else {
            change.setText("移除");
            title.setText("动态脚本：已导入");
            for (int i=1;i<=4;i++){
                Preference.save(getContext(),"code"+i,"");
            }
            Preference.save(getContext(),"custom",true);
            adapter.openAll();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 17 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri==null){
                return;
            }
            String filePatch=MainActivity.getRealPach(uri,getContext());

            Log.d("filePath",filePatch);
            String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath();
            if (MainActivity.copyFile(MainActivity.getRealPach(uri,getContext()),sdcard+"/lktMode/powercfg/powercfg.sh")){
                Toast.makeText(getContext(),"导入成功",Toast.LENGTH_SHORT).show();
                change();
            }else {
                Toast.makeText(getContext(),"导入失败",Toast.LENGTH_LONG).show();
            }

        }
    }

    public void updateList(){
        String code1=(String) Preference.get(getContext(),"code1","String");
        items.get(0).setSubtitle(code1.substring(code1.lastIndexOf(" ")+1));
        String code2=(String) Preference.get(getContext(),"code2","String");
        items.get(1).setSubtitle(code2.substring(code2.lastIndexOf(" ")+1));
        String code3=(String) Preference.get(getContext(),"code3","String");
        items.get(2).setSubtitle(code3.substring(code3.lastIndexOf(" ")+1));
        String code4=(String) Preference.get(getContext(),"code4","String");
        items.get(3).setSubtitle(code4.substring(code4.lastIndexOf(" ")+1));;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
        final String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh";
        File shell=new File(sdcard);
        if (!shell.exists()){
            change.setText("导入");
            title.setText("动态脚本：未导入");
        }
    }

    private void showTerminal(String str){
        output="脚本开始:\n";
        final AlertDialog alertDialog=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setMessage(output)
                .setTitle("运行结果")
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        alertDialog.show();
        try{
            RootTools.getShell(true).add(new Command(2,"sh "+Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh "+str){
                @Override
                public void commandOutput(int id, String line) {
                    super.commandOutput(id, line);
                    output+=line+"\n";
                    Log.d("output",line);
                    alertDialog.setMessage(output);
                    alertDialog.show();
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    super.commandCompleted(id, exitcode);
                    Log.d("output",exitcode+"");
                    if (exitcode==0){
                        output+="脚本运行成功";
                        alertDialog.setMessage(output);
                        alertDialog.show();
                    }else {
                        output+="脚本运行失败";
                        alertDialog.setMessage(output);
                        alertDialog.show();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void showDialog(String str, String positive, DialogInterface.OnClickListener p, String negative, DialogInterface.OnClickListener n){
        new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setNegativeButton(negative,n)
                .setPositiveButton(positive,p)
                .setTitle("功能说明")
                .setMessage(str)
                .create().show();
    }

    @Override
    public void setToolbar(OnToolbarChange toolbar) {
        super.setToolbar(toolbar);
        toolbar.onchange("自定义调度","设置四个挡位");
    }
}
