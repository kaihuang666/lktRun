package com.kai.lktMode.activity;

import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;

import com.kai.lktMode.adapter.ListGameAdapter;
import com.kai.lktMode.bean.App;
import com.kai.lktMode.tool.util.local.AppUtils;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.widget.SimplePaddingDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddActivity extends BaseActivity {
    private List<App> apps=new ArrayList<>();
    private List<App> search_apps=new ArrayList<>();
    private ListGameAdapter adapter;
    private ListGameAdapter adapter1;
    private ProgressDialog dialog;
    private String Action;
    @BindView(R.id.simple_toolbar) Toolbar toolbar;
    @BindView(R.id.actionmenuview) ActionMenuView actionMenuView;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent=getIntent();
        Action=intent.getAction();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        ButterKnife.bind(this);
        initToolBar();
        initDialog();
        initGame();

    }

    private void initToolBar(){
        if (Action.equals("softwares")){
            toolbar.setTitle("添加需要清理的应用");
            toolbar.setSubtitle("");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        onCreateOptionsMenu(actionMenuView.getMenu());
        actionMenuView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return true;
            }

        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_app ,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.ab_search).getActionView();
        if(searchView == null){
            Log.e("SearchView" ,"Fail to get Search View.");
            return true;
        }
        searchView.setOnSearchClickListener(null);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                recyclerView.setLayoutManager(new LinearLayoutManager(AddActivity.this));
                ListGameAdapter adapter1=new ListGameAdapter(AddActivity.this,search_apps,1,!Action.equals("softwares"));
                adapter1.setChangListener(new ListGameAdapter.OnCheckBoxChangListener() {
                    @Override
                    public void onChange(int i, Boolean b) {
                        if (b){
                            if (Action.equals("softwares")){
                                Preference.softwareAdd(AddActivity.this,search_apps.get(i).getPackage_name());
                            }
                            else
                                Preference.gameAdd(AddActivity.this,search_apps.get(i).getPackage_name());
                        }

                        else{
                            if (Action.equals("softwares")){
                                Preference.softwareRemove(AddActivity.this,search_apps.get(i).getPackage_name());
                            }else
                                Preference.gameRemove(AddActivity.this,search_apps.get(i).getPackage_name());
                        }

                    }
                });

                recyclerView.setAdapter(adapter1);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search_apps.clear();
                for (App app:apps){
                    String name=app.getName();
                    String package_name=app.getPackage_name();
                    if (name.contains(newText)||package_name.contains(newText)){
                        search_apps.add(app);
                    }
                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    private void initGame(){
        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this){
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 360;
            }
        });
        recyclerView.addItemDecoration(new SimplePaddingDecoration(AddActivity.this));
        adapter=new ListGameAdapter(AddActivity.this,apps,1,!Action.equals("softwares"));
        adapter.setChangListener(new ListGameAdapter.OnCheckBoxChangListener() {
            @Override
            public void onChange(int i, Boolean b) {
                if (b){
                    if (Action.equals("softwares")){
                        Preference.softwareAdd(AddActivity.this,apps.get(i).getPackage_name());
                    }
                    else
                    Preference.gameAdd(AddActivity.this,apps.get(i).getPackage_name());
                }

                else{
                    if (Action.equals("softwares")){
                        Preference.softwareRemove(AddActivity.this,apps.get(i).getName());
                    }else
                    Preference.gameRemove(AddActivity.this,apps.get(i).getPackage_name());
                }

            }
        });
        recyclerView.setAdapter(adapter);
        Timer timer=new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getData();
                    }
                });
            }
        };
        timer.schedule(task,1000);

    }

    public void getData(){
        for (String s:scanLocalInstallAppList(this.getPackageManager())){

            if (Action.equals("softwares")) {
                apps.add(new App(AppUtils.getAppName(AddActivity.this,s),s,AppUtils.getDrawable(AddActivity.this,s), Preference.getSoftwares(AddActivity.this).contains(s)));
            }
            else {
                apps.add(new App(AppUtils.getAppName(AddActivity.this,s),s,AppUtils.getDrawable(AddActivity.this,s), Preference.getGames(AddActivity.this).contains(s)));
            }
        }
        updateList();
    }
    public void updateList(){
        adapter.notifyDataSetChanged();
        dialog.dismiss();
    }
    private void initDialog(){
        dialog=new ProgressDialog(this,R.style.AppDialog);
        dialog.setMessage("正在加载列表");
        dialog.show();
    }
    private  List<String> scanLocalInstallAppList(PackageManager packageManager) {
        List<String> myAppInfos = new ArrayList<String>();
        try {
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                //过滤掉系统app
            if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0) {
                continue;
            }
                String myAppInfo =packageInfo.packageName;
                //Log.d("installLocation",packageInfo.installLocation+"");
                if (packageInfo.applicationInfo.loadIcon(packageManager) == null|| AppUtils.getAppName(AddActivity.this,myAppInfo).isEmpty()) {
                    continue;
                }
                myAppInfos.add(myAppInfo);
            }
        }catch (Exception e){
            //Log.e(TAG,"===============获取应用包信息失败");
        }
        return myAppInfos;
    }
}
