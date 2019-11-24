package com.kai.lktMode.activity;

import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.kai.lktMode.R;
import com.kai.lktMode.adapter.CpuManagerAdapter;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.cpu.CpuBoost;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.cpu.Group;
import com.kai.lktMode.bean.ParentItem;
import com.kai.lktMode.cpu.Stune;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.gpu.GPUFreq;
import com.kai.lktMode.service.CpuMService;
import com.kai.lktMode.widget.AnimatedExpandableListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.Subject;

public class CpuManagerActivity extends BaseActivity {
    CpuManager manager;
    LocalBroadcastManager localBroadcastManager;
    List<ParentItem> parentItems=new ArrayList<>();
    BroadcastReceiver recevicer;
    int codeCount=0;
    @BindView(R.id.simple_toolbar)
    Toolbar toolbar;
    CpuManagerAdapter adapter;
    @BindView(R.id.list)
    AnimatedExpandableListView list;
    private CpuMService.CpuBinder binder;
    private Intent bindIntent;
    ProgressDialog progressDialog;
    private static String[] kernel_names=new String[]{"小核集群","大核集群","中央集群"};
    private static String[] limit_names=new String[]{"小核频率设置","大核频率设置","中央频率设置"};
    private static String[] param_names=new String[]{"小核参数设置","大核参数设置","中央参数设置"};
    private static String[] boost_names=new String[]{"小核升频设置","大核升频设置","中央升频设置"};
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder=(CpuMService.CpuBinder)iBinder;
            binder.startListening();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder.stopListening();
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu_manager);
        //Toast.makeText(this,CpuBoost.isAddition()+"",Toast.LENGTH_SHORT).show();
        Intent intent=getIntent();
        String action=intent.getAction();
        progressDialog=new ProgressDialog(this,R.style.AppDialog);
        progressDialog.setMessage("获取参数中");
        progressDialog.setCancelable(false);
        progressDialog.show();
        codeCount=intent.getIntExtra("code",0);
        ButterKnife.bind(this);
        manager=new CpuManager();
        adapter=new CpuManagerAdapter(this,parentItems);
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
        regist();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (action.equals("creation")){
            ActionMenuView actionMenuView=(ActionMenuView)findViewById(R.id.actionmenuview);
            actionMenuView.getMenu().add("创建当前调度");
            actionMenuView.getMenu().getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            actionMenuView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    ProgressDialog progressDialog=new ProgressDialog(CpuManagerActivity.this,R.style.AppDialog);
                    progressDialog.setMessage("保存中");
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String command=adapter.getCommand();
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Intent intent=new Intent();
                                    intent.putExtra("passage",command);
                                    intent.putExtra("code",codeCount);
                                    setResult(RESULT_OK,intent);
                                    finish();
                                }
                            });
                        }
                    }).start();

                    return true;
                }
            });
        }else {
            toolbar.setTitle("常规设置");
        }

        refresh();
    }
    private void refresh(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getData();
            }
        }).start();
    }
    private void getData(){
        CpuManager manager=new CpuManager();
        //获取核心集群
        CpuManager.Kernel[][] groups=manager.getCpuGroup();
        int[][] counts=manager.getKernelCount();
        List<ParentItem> parentItemsnew=new ArrayList<>();
        for (int i=0;i<groups.length;i++){
            Group group=new Group(groups[i],counts[i]);
            parentItemsnew.add(new ParentItem(kernel_names[i],group, SystemInfo.type_freq));
            parentItemsnew.add(new ParentItem(limit_names[i],group,SystemInfo.type_limit));
            parentItemsnew.add(new ParentItem(param_names[i],group,SystemInfo.type_param));
            if (CpuBoost.isSupport()){
                Group group1=new Group(groups[i],counts[i]);
                group1.setCpuBoost(new CpuBoost());
                parentItemsnew.add(new ParentItem(boost_names[i],group1,SystemInfo.type_boost));
            }
        }
        GPUFreq gpuFreq=GPUFreq.getInstance();
        if (gpuFreq.supported()){
            parentItemsnew.add(new ParentItem("gpu设置",gpuFreq,SystemInfo.type_gpu));
        }
        if (Stune.isSupport()){
            parentItemsnew.add(new ParentItem("eas激进控制",new Stune(),SystemInfo.type_stune));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentItems.clear();
                parentItems.addAll(parentItemsnew);
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
                for (int i=0;i<parentItems.size()-1;i++){
                    list.expandGroup(i);
                }
            }
        });

    }
    public void regist(){
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.kai.lktMode.cpu");
        localBroadcastManager= LocalBroadcastManager.getInstance(this);
        recevicer =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int[] freqs=intent.getIntArrayExtra("freq");
                adapter.setFreqs(freqs);
            }
        };
        localBroadcastManager.registerReceiver(recevicer,intentFilter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        bindIntent=new Intent(this, CpuMService.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bindService(bindIntent,connection, Context.BIND_AUTO_CREATE);
            }
        },1000);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if (bindIntent!=null)
            try {
                unbindService(connection);
            }catch (Exception e){
                e.printStackTrace();
            }
        super.onStop();
    }
}
