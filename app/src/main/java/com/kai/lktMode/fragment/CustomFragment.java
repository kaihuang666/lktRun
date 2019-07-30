package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.bean.Item;
import com.kai.lktMode.adapter.CustomAdapter;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomFragment extends MyFragment {
    private List<Item> items=new ArrayList<>();
    private int[] colors={R.color.colorBattery,R.color.colorBalance,R.color.colorPerformance,R.color.colorTurbo};
    private CustomAdapter adapter;
    private String output;
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_custom,null,false);
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
        Switch custom=(Switch)view.findViewById(R.id.swicth);
        custom.setChecked((Boolean) Preference.get(getContext(),"custom","Boolean"));
        custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, boolean b) {
                if (b){
                    Preference.save(getContext(),"custom",b);
                }else {
                    String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh";
                    File shell=new File(sdcard);
                    if (shell.exists()){
                        Toast.makeText(getContext(),"老铁，正在用动态脚本，不能关啊",Toast.LENGTH_LONG).show();
                        compoundButton.setChecked(true);
                    }else {
                        Preference.save(getContext(),"custom",b);
                    }
                }
                getMain().refreshProp();
            }
        });
        items.add(new Item("省电",""));
        items.add(new Item("均衡",""));
        items.add(new Item("游戏",""));
        items.add(new Item("极限",""));
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview);
        final LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter=new CustomAdapter(getContext(),items,colors);
        adapter.setOnItemClick(new CustomAdapter.OnItemClick() {
            @Override
            public void onClick(int i, String a) {
                showTerminal(a);
            }
        });
        adapter.setOnImportClick(new CustomAdapter.OnImportClick() {
            @Override
            public void onImport(int i, final EditText e, ImageButton self) {
                PopupMenu popup = new PopupMenu(getContext(), self);//第二个参数是绑定的那个view
                //获取菜单填充器
                MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.importmenu, popup.getMenu());
                //绑定菜单项的点击事件
                final String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.importPowercfg:
                                e.setText("sh "+sdcard+"/lktMode/powercfg/powercfg.sh ");
                                break;
                            case R.id.importFile:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        recyclerView.setAdapter(adapter);
        updateList();
    }
    public void updateList(){
        items.get(0).setSubtitle((String) Preference.get(getContext(),"code1","String"));
        items.get(1).setSubtitle((String) Preference.get(getContext(),"code2","String"));
        items.get(2).setSubtitle((String) Preference.get(getContext(),"code3","String"));
        items.get(3).setSubtitle((String) Preference.get(getContext(),"code4","String"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void Refresh() {
        super.Refresh();
        refresh();
    }

    public void refresh(){
        updateList();
        Switch custom=(Switch)view.findViewById(R.id.swicth);
        custom.setChecked((Boolean) Preference.get(getContext(),"custom","Boolean"));
        updateList();
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
            RootTools.getShell(true).add(new Command(2,str){
                @Override
                public void commandOutput(int id, String line) {
                    super.commandOutput(id, line);
                    if (line.contains("No such file or directory")){
                        line="内核不支持该命令";
                    }if (line.contains("Permission denied")){
                        line="命令无法执行，内核正在被其他调度占用";
                    }
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

}
