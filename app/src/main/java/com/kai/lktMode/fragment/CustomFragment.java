package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.Item;
import com.kai.lktMode.Preference;
import com.kai.lktMode.R;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

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
        view=inflater.inflate(R.layout.activity_custom,container,false);
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
                    showDialog("使用自定义调度会禁用默认的LKT调度，你还需要再下面的输入框中输入对应的调度命令", "立即开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Preference.save(getContext(),"custom",true);
                        }
                    }, "暂不开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            compoundButton.setChecked(false);
                        }
                    });
                }
                else {
                    compoundButton.setChecked(false);
                    Preference.save(getContext(),"custom",false);
                }
            }
        });
        items.add(new Item("省电",""));
        items.add(new Item("均衡",""));
        items.add(new Item("游戏",""));
        items.add(new Item("极限",""));
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview);
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter=new CustomAdapter(getContext(),items,colors);
        adapter.setOnItemClick(new CustomAdapter.OnItemClick() {
            @Override
            public void onClick(int i, String a) {
                showTerminal(a);
            }
        });

        recyclerView.setAdapter(adapter);
        updateList();
    }
    private void updateList(){
        items.get(0).setSubtitle((String) Preference.get(getContext(),"code1","String"));
        items.get(1).setSubtitle((String) Preference.get(getContext(),"code2","String"));
        items.get(2).setSubtitle((String) Preference.get(getContext(),"code3","String"));
        items.get(3).setSubtitle((String) Preference.get(getContext(),"code4","String"));
        adapter.notifyDataSetChanged();
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
