package com.kai.lktMode.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kai.lktMode.R;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.root.RootUtils;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.util.ArrayList;
import java.util.List;

public class ParamDialog extends AlertDialog {
    TextView title;
    ListView listView;
    String titleS;
    private CpuManager.Governor governor;
    private Context context;
    private CpuManager manager;
    public ParamDialog(Context context, String title, CpuManager.Governor governor) {
        super(context,R.style.AppDialog);
        this.context=context;
        this.titleS=title;
        this.governor=governor;
        manager=CpuManager.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view=View.inflate(context, R.layout.dialog_param,null);
        title=(TextView)view.findViewById(R.id.title);
        ListView listView=(ListView)view.findViewById(R.id.list);
        title.setText(titleS);
        List<String> params=new ArrayList<>();
        if (governor.keySet!=null)
            params=governor.keySet;
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,params);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long l) {
                View view1=View.inflate(context,R.layout.dialog_edittext,null);
                EditText e=view1.findViewById(R.id.edit);
                AlertDialog dialog=new AlertDialog.Builder(context,R.style.AppDialog)
                        .setView(view1)
                        .setPositiveButton("确定", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                StringBuilder builder=new StringBuilder("");

                                for (int k:governor.getParentKernel().getRelated_cpus()){
                                    builder.append(manager.getKernel(k).getGovernor(governor.getName()).setValue(governor.keySet.get(postion),e.getText().toString()));
                                }
                                command(builder.toString());
                                //Toast.makeText(context,builder.toString(),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                e.setText(governor.getValue(governor.keySet.get(postion)));
            }
        });
        setContentView(view);
    }
    private void command(String args){
        try {
            RootUtils.runCommand(args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
