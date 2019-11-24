package com.kai.lktMode.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.kai.lktMode.R;
import com.kai.lktMode.adapter.CpuAdvancedManagerAdapter;
import com.kai.lktMode.adapter.CpuManagerAdapter;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.bean.ParentItem;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.cpu.Cpuset;
import com.kai.lktMode.selinux.SElinux;
import com.kai.lktMode.thermal.Thermal;
import com.kai.lktMode.widget.AnimatedExpandableListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CpuAdvancedManagerActivity extends BaseActivity {
    @BindView(R.id.simple_toolbar)
    Toolbar toolbar;
    CpuAdvancedManagerAdapter adapter;
    @BindView(R.id.list)
    AnimatedExpandableListView list;
    List<ParentItem> parentItems=new ArrayList<>();
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu_advanced_manager);
        ButterKnife.bind(this);
        setToolbar();
        progressDialog=new ProgressDialog(this,R.style.AppDialog);
        progressDialog.setMessage("获取参数中");
        progressDialog.show();
        adapter=new CpuAdvancedManagerAdapter(this,parentItems);
        list.setAdapter(adapter);
        list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and
                // expandGroupWithAnimation(int) to animate group
                // expansion/collapse.
                if (list.isGroupExpanded(groupPosition)) {
                    list.collapseGroupWithAnimation(groupPosition);
                } else {
                    list.expandGroupWithAnimation(groupPosition);
                }
                return true;
            }

        });

        refresh();
    }
    private void refresh(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                        for (int i=0;i<parentItems.size();i++){
                            list.expandGroup(i);
                        }
                    }
                });
            }
        }).start();


    }

    @TargetApi(21)
    private void setToolbar(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void getData(){
        parentItems.clear();
        if (Cpuset.isSupport()){
            parentItems.add(new ParentItem("核心分配机制",new Cpuset(), SystemInfo.type_cpuset));
        }
        if (SElinux.isSupprot()){
            parentItems.add(new ParentItem("SElinux安全机制(推荐关闭)",null,SystemInfo.type_selinux));
        }
        if (Thermal.supported()){
            parentItems.add(new ParentItem("温控机制",new Thermal(),SystemInfo.type_thermal));
        }
    }

}
