package com.kai.lktMode.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFormatException;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.android.material.navigation.NavigationView;
import com.kai.lktMode.bean.Sdcard;
import com.kai.lktMode.cpu.CpuBoost;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.cpu.CpuModel;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.service.AutoService;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.service.CpuService;
import com.kai.lktMode.fragment.AboutFragment;
import com.kai.lktMode.fragment.CustomFragment;
import com.kai.lktMode.fragment.GameFragment;
import com.kai.lktMode.fragment.LockFragment;
import com.kai.lktMode.fragment.MainFragment;
import com.kai.lktMode.base.MyApplication;
import com.kai.lktMode.fragment.MyFragment;
import com.kai.lktMode.fragment.PowercfgFragment;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.tool.ServiceStatusUtils;
import com.kai.lktMode.tool.ToastUtil;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.kai.lktMode.tool.util.net.WebUtil;
import com.kai.lktMode.tune.Tune;
import com.kai.lktMode.widget.CustomDrawerLayout;
import com.kai.lktMode.widget.DownloadDialog;
import com.kai.lktMode.widget.ViewPagerSlide;
import com.kai.lktMode.widget.CloudLoginDialog;
import com.leon.lfilepickerlibrary.utils.Constant;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity {
    private String[] options1Items=new String[]{"高通","猎户座","麒麟","联发科","英特尔"};
    private String[][] downloadSites={{"i5ldrxe","i5ldrzg","i5ldryf","i5lds0h","i5ldwkb","i5lds2j","i5lds3a","i5lds4b","i5lds5c"},
            {"i5ldrri","i5ldrpg","i5ldrof"},{"i5ldrwd","i5ldrvc","i5ldrub"},{"i5ldrta","i5ldrsj"},{"i5ldrne"}};
    @BindView(R.id.simple_toolbar) Toolbar mNormalToolbar;
    @BindView(R.id.viewPager)ViewPagerSlide viewPager;
    @BindView(R.id.navigationView1) NavigationView navigationView1;
    @BindView(R.id.navigationView) NavigationView navigationView;
    @BindView(R.id.actionmenuview) ActionMenuView actionmenuview;
    @BindView(R.id.drawer)DrawerLayout drawerLayout;
    @BindView(R.id.layout0) LinearLayout linearLayout0;
    @BindView(R.id.layout1) LinearLayout linearLayout1;
    MainFragment mainFragment;
    CustomFragment customFragment;
    PowercfgFragment powercfgFragment;
    GameFragment gameFragment;
    LockFragment lockFragment;
    private List<List<String>> options2Items=new ArrayList<>();
    private List<MyFragment> fragments=new ArrayList<>();
    private FragmentManager fragmentManager;
    private ProgressDialog dialog;
    private Intent bindIntent;
    public FragmentStatePagerAdapter adapter;
    private static boolean mBackKeyPressed = false;
    private CpuService.CpuBinder binder;

    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder=(CpuService.CpuBinder)iBinder;
            binder.startListening();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder.stopListening();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        bindIntent=new Intent(this, CpuService.class);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    bindService(bindIntent,connection,Context.BIND_AUTO_CREATE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },1000);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /*if (getFragment(0).isVisible()) {
            getFragment(0).Refresh();
        }*/
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
    private void setFragments(){
        fragments.add(0,mainFragment);
        fragments.add(1,customFragment);
        fragments.add(2,powercfgFragment);
        fragments.add(3,gameFragment);
        fragments.add(4,lockFragment);
        fragmentManager = getSupportFragmentManager();
        adapter=new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                drawerLayout.closeDrawers();
                MyFragment fragment=fragments.get(position);
                if (fragment==null){
                    switch (position){
                        case 0: fragment=new MainFragment();break;
                        case 1: fragment=new CustomFragment();break;
                        case 2: fragment=new PowercfgFragment();break;
                        case 3: fragment=new GameFragment();break;
                        case 4: fragment=new LockFragment();break;
                    }
                    fragments.set(position,fragment);
                }
                return fragment;
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(adapter);
        switchPage(0,false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initToolbar();
        setFragments();


        if (!ServiceStatusUtils.isServiceRunning(this, AutoService.class)){
            Intent intent=new Intent(this,AutoService.class);
            intent.setAction("reset");
            startService(intent);
        }
        setYcDevices();
        checkUpdate();
    }
    private void setYcDevices(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> options2Items_01 = new ArrayList<>();
                options2Items_01.add("骁龙625/626");
                options2Items_01.add("骁龙652/650");
                options2Items_01.add("骁龙636");
                options2Items_01.add("骁龙660");
                options2Items_01.add("骁龙801/800/805");
                options2Items_01.add("骁龙810/808");
                options2Items_01.add("骁龙820/821");
                options2Items_01.add("骁龙835");
                options2Items_01.add("骁龙845");
                ArrayList<String> options2Items_02 = new ArrayList<>();
                options2Items_02.add("Exynos 8895");
                options2Items_02.add("Exynos 8890");
                options2Items_02.add("Exynos 7420");
                ArrayList<String> options2Items_03 = new ArrayList<>();
                options2Items_03.add("麒麟970");
                options2Items_03.add("麒麟960");
                options2Items_03.add("麒麟950/955");
                ArrayList<String> options2Items_04 = new ArrayList<>();
                options2Items_04.add("Helio X20/X25");
                options2Items_04.add("Helio X10");
                ArrayList<String> options2Items_05 = new ArrayList<>();
                options2Items_05.add("Atom z3560/z3580");
                options2Items.add(options2Items_01);
                options2Items.add(options2Items_02);
                options2Items.add(options2Items_03);
                options2Items.add(options2Items_04);
                options2Items.add(options2Items_05);
            }
        }).start();

    }
    private void checkUpdate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final WebUtil webUtil =new WebUtil();
                boolean update=webUtil.isToUpdate();
                try {
                    Thread.sleep(3000);

                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (update){
                            new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                    .setTitle("版本更新:"+ webUtil.getVersionName())
                                    .setMessage(webUtil.getVersionLog())
                                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ToastUtil.longShow(MainActivity.this,"请选择酷安市场下载");
                                            Uri uri = Uri.parse("market://details?id="+getPackageName());
                                            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("忽略",null)
                                    .create().show();
                        }
                    }
                });

            }
        }).start();

    }
    public void refreshLoginV1(){
        View headview=navigationView1.getHeaderView(0);
        if (headview==null){
            headview=navigationView1.inflateHeaderView(R.layout.header);
        }
        headview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(Boolean)Preference.getBoolean(MainActivity.this,"cloud")){
                    final CloudLoginDialog dialog=new CloudLoginDialog(MainActivity.this,R.style.AppDialog);
                    dialog.setOnLoginClick(new CloudLoginDialog.OnLoginClick() {
                        @Override
                        public void onclick(String user, String password) {
                            WebUtil.login(user.trim(),password.trim(),dialog,MainActivity.this,false);
                        }
                    });
                    dialog.setOnRegistClick(new CloudLoginDialog.OnRegistClick() {
                        @Override
                        public void onregist() {
                            Intent intent2 = new Intent();
                            intent2.setAction("android.intent.action.VIEW");
                            Uri content_url1 = Uri.parse("https://www.jianguoyun.com/d/login");//此处填链接
                            intent2.setData(content_url1);
                            startActivity(intent2);
                        }
                    });
                    dialog.setView(new EditText(MainActivity.this));
                    dialog.show();
                }else {
                    new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                            .setTitle("账号信息")
                            .setMessage("账号："+Preference.getString(MainActivity.this,"username")+"\n状态："+(SystemInfo.isDonated?"已捐赠":"未捐赠"))
                            .setPositiveButton("注销", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Preference.saveBoolean(MainActivity.this,"cloud",false);
                                    Preference.saveString(MainActivity.this,"username","");
                                    Preference.saveString(MainActivity.this,"password","");
                                    refreshLogin();
                                    refreshLoginV1();
                                }
                            })
                            .setNegativeButton("刷新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    refreshLoginV1();
                                    refreshLogin();
                                }
                            })
                            .create().show();
                }
            }
        });
        TextView version=(TextView)headview.findViewById(R.id.donationVersion);
        TextView email=(TextView)headview.findViewById(R.id.email);
        String username=(String)Preference.getString(this,"username");
        email.setText(username.isEmpty()?"点击登录":username);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Boolean isDonated=WebUtil.isDonation(MainActivity.this);
                    Log.d("dodo",isDonated+"");
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    version.setText((Boolean)isDonated?"捐赠版":"普通版");
                                }
                            });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void refreshLogin(){

        View headview=navigationView.getHeaderView(0);
        if (headview==null){
            headview=navigationView.inflateHeaderView(R.layout.header);
        }
        headview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(Boolean)Preference.getBoolean(MainActivity.this,"cloud")){
                    final CloudLoginDialog dialog=new CloudLoginDialog(MainActivity.this,R.style.AppDialog);
                    dialog.setOnLoginClick(new CloudLoginDialog.OnLoginClick() {
                        @Override
                        public void onclick(String user, String password) {
                            WebUtil.login(user.trim(),password.trim(),dialog,MainActivity.this,false);
                        }
                    });
                    dialog.setOnRegistClick(new CloudLoginDialog.OnRegistClick() {
                        @Override
                        public void onregist() {
                            Intent intent2 = new Intent();
                            intent2.setAction("android.intent.action.VIEW");
                            Uri content_url1 = Uri.parse("https://www.jianguoyun.com/d/login");//此处填链接
                            intent2.setData(content_url1);
                            startActivity(intent2);
                        }
                    });
                    dialog.setView(new EditText(MainActivity.this));
                    dialog.show();
                }else {
                    ProgressDialog dialog=new ProgressDialog(MainActivity.this,R.style.AppDialog);
                    dialog.setMessage("拉取信息中");
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    new Thread(new Runnable() {
                        String donated;
                        String connectd;
                        @Override
                        public void run() {
                            donated=SystemInfo.isDonated?"已捐赠":"未捐赠";
                            boolean c=WebUtil.isNetworkAccess();
                            String connected=c?"可用":"不可用";
                            if (!c){
                                donated="无法获取";
                            }
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                            .setTitle("账号信息")
                                            .setMessage("账号："+Preference.getString(MainActivity.this,"username")+"\n状态："+donated+"\n网络连接："+connected)
                                            .setPositiveButton("注销", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Preference.saveBoolean(MainActivity.this,"cloud",false);
                                                    Preference.saveString(MainActivity.this,"username","");
                                                    Preference.saveString(MainActivity.this,"password","");
                                                    refreshLogin();
                                                    refreshLoginV1();
                                                }
                                            })
                                            .setNegativeButton("刷新", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    refreshLogin();
                                                    refreshLoginV1();
                                                }
                                            })
                                            .create().show();

                                }
                            });
                        }
                    }).start();
                }
            }
        });
        TextView version=(TextView)headview.findViewById(R.id.donationVersion);
        TextView email=(TextView)headview.findViewById(R.id.email);
        String username=(String)Preference.getString(this,"username");
        email.setText(username.isEmpty()?"点击登录":username);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Boolean isDonated=WebUtil.isDonation(MainActivity.this);
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    version.setText((Boolean)isDonated?"捐赠版":"普通版");
                                }
                            });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }
    private void initToolbar() {
        mNormalToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
                return true;
            }
        });
        setSupportActionBar(mNormalToolbar);
        mNormalToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Configuration mConfiguration = MainActivity.this.getResources().getConfiguration();
                int ori = mConfiguration.orientation; //获取屏幕方向
                if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,GravityCompat.START);
                    if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    if (linearLayout0.getVisibility()==View.INVISIBLE){
                        openDrawer(true);
                    }else {
                        closeDrawer(true);
                    }
                }else if (ori == mConfiguration.ORIENTATION_PORTRAIT){
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,GravityCompat.START);
                    if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }


            }
        });
        Configuration mConfiguration = MainActivity.this.getResources().getConfiguration();
        int ori = mConfiguration.orientation;
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            openDrawer(true);
        }else if (ori == mConfiguration.ORIENTATION_PORTRAIT){
            closeDrawer(true);
        }
        navigationView.getMenu().getItem(0).setCheckable(true);
        navigationView1.getMenu().getItem(0).setCheckable(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int ID=menuItem.getItemId();
                menuItem.setCheckable(false);
                switch (ID){
                    case R.id.main:
                        switchPage(0,true);
                        menuItem.setCheckable(true);
                        break;
                    case R.id.setting:
                        startActivity(new Intent(MainActivity.this,SettingActivity.class));
                        break;
                    case R.id.lab:
                        menuItem.setCheckable(true);
                        switchPage(3,true);
                        break;
                    case R.id.about:
                        startActivity(new Intent(MainActivity.this,AboutActivity.class));
                        break;
                    case R.id.custom:
                        menuItem.setCheckable(true);
                        switchPage(1,true);
                        break;
                    case R.id.powercfg:
                        menuItem.setCheckable(true);
                        switchPage(2,true);
                        break;
                    case R.id.lock:
                        menuItem.setCheckable(true);
                        switchPage(4,true);
                        break;
                    case R.id.cloud:
                        //startActivity(new Intent(MainActivity.this,WebViewActivity.class));
                        getOnCloud();
                        break;
                    case R.id.donation:
                        AboutFragment.donation(MainActivity.this);
                        break;
                    case R.id.processor:
                        Intent intent=new Intent(MainActivity.this, CpuManagerActivity.class);
                        intent.setAction("setting");
                        startActivity(intent);
                        break;
                    case R.id.processor_advanced:
                        startActivity(new Intent(MainActivity.this,CpuAdvancedManagerActivity.class));
                        break;
                    case R.id.unlock:
                        if (SystemInfo.getIsDonated()){
                            ToastUtil.shortShow(MainActivity.this,"你已经解锁了捐赠版(-_-)");
                            break;
                        }
                        if (!(Boolean)Preference.getBoolean(MainActivity.this,"cloud")){
                            ToastUtil.shortAlert(MainActivity.this,"请先登录");
                            getOnCloud();
                            break;
                        }
                        AlertDialog dialog=new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                .setTitle("捐赠版解锁")
                                .setMessage("捐赠版可以解锁一些高级功能：\n自定义调度--自适应生成调度\n自定义调度--克隆当前调度\n自定义调度--定制调度\n云同步--无限制全局备份" +
                                        "\nps:这些功能也不是黑科技，请先在相应功能区域体验。")
                                .setPositiveButton("立即解锁", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                                .setTitle("请仔细阅读注意事项")
                                                .setMessage("请选择任意方式捐赠3元以上，尤其是需要备注你登录的坚果云账号，否则你的付款没有效果。捐赠版的注册操作在1-2个工作日完成，请耐心等待或者酷安私信、qq群私信我。")
                                                .setPositiveButton("我已知晓", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        AboutFragment.donation(MainActivity.this);
                                                    }
                                                })
                                                .show();
                                    }
                                })
                                .setNegativeButton("暂不需要",null)
                                .show();
                        break;
                    default:break;

                }

                return true;
            }
        });
        navigationView1.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int ID=menuItem.getItemId();
                menuItem.setCheckable(false);
                switch (ID){
                    case R.id.main:
                        switchPage(0,true);
                        menuItem.setCheckable(true);
                        break;
                    case R.id.setting:
                        startActivity(new Intent(MainActivity.this,SettingActivity.class));
                        break;
                    case R.id.lab:
                        menuItem.setCheckable(true);
                        switchPage(3,true);
                        break;
                    case R.id.about:
                        startActivity(new Intent(MainActivity.this,AboutActivity.class));
                        break;
                    case R.id.custom:
                        menuItem.setCheckable(true);
                        switchPage(1,true);
                        break;
                    case R.id.powercfg:
                        menuItem.setCheckable(true);
                        switchPage(2,true);
                        break;
                    case R.id.lock:
                        menuItem.setCheckable(true);
                        switchPage(4,true);
                        break;
                    case R.id.cloud:
                        //startActivity(new Intent(MainActivity.this,WebViewActivity.class));
                        getOnCloud();
                        break;
                    case R.id.donation:
                        AboutFragment.donation(MainActivity.this);
                        break;
                    case R.id.processor:
                        Intent intent=new Intent(MainActivity.this, CpuManagerActivity.class);
                        intent.setAction("setting");
                        startActivity(intent);
                        break;
                    case R.id.processor_advanced:
                        startActivity(new Intent(MainActivity.this,CpuAdvancedManagerActivity.class));
                        break;
                    case R.id.unlock:
                        if (SystemInfo.getIsDonated()){
                            ToastUtil.shortShow(MainActivity.this,"你已经解锁了捐赠版(-_-)");
                            break;
                        }
                        if (!Preference.getBoolean(MainActivity.this,"cloud")){
                            ToastUtil.shortAlert(MainActivity.this,"请先登录");
                            getOnCloud();
                            break;
                        }
                        AlertDialog dialog=new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                .setTitle("捐赠版解锁")
                                .setMessage("捐赠版可以解锁一些高级功能：\n自定义调度--自适应生成调度\n自定义调度--克隆当前调度\n自定义调度--定制调度\n云同步--无限制全局备份" +
                                        "\nps:这些功能也不是黑科技，请先在相应功能区域体验。")
                                .setPositiveButton("立即解锁", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                                .setTitle("请仔细阅读注意事项")
                                                .setMessage("请选择任意方式捐赠3元以上，尤其是需要备注你登录的坚果云账号，否则你的付款没有效果。捐赠版的注册操作在1-2个工作日完成，请耐心等待或者酷安私信、qq群私信我。")
                                                .setPositiveButton("我已知晓", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        AboutFragment.donation(MainActivity.this);
                                                    }
                                                })
                                                .show();

                                    }
                                })
                                .setNegativeButton("暂不需要",null)
                                .show();
                        break;
                    default:break;

                }

                return true;
            }
        });

        refreshLogin();
        refreshLoginV1();
    }
    private void getOnCloud(){
        boolean isLogin=(Boolean)Preference.getBoolean(MainActivity.this,"cloud",false);
        if (isLogin){
            WebUtil.list(MainActivity.this);
        }else {
            final CloudLoginDialog dialog=new CloudLoginDialog(MainActivity.this,R.style.AppDialog);
            dialog.setOnLoginClick(new CloudLoginDialog.OnLoginClick() {
                @Override
                public void onclick(String user, String password) {
                   WebUtil.login(user,password,dialog,MainActivity.this,true);
                }
            });
            dialog.setOnRegistClick(new CloudLoginDialog.OnRegistClick() {
                @Override
                public void onregist() {
                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.VIEW");
                    Uri content_url1 = Uri.parse("https://www.jianguoyun.com/d/login");//此处填链接
                    intent2.setData(content_url1);
                    startActivity(intent2);
                }
            });
            dialog.setView(new EditText(MainActivity.this));
            dialog.show();
        }

    }
    public void refreshProp(){
        MainFragment fragment=(MainFragment)fragments.get(0);
        fragment.readProp();
    }
    public MyFragment getFragment(int i){
        return fragments.get(i);
    }
    private void downloadPowercfg(final String url) throws Exception{
        final String sdcard=Sdcard.getPath(MainActivity.this)+"/lktMode/powercfg/";
        File dir=new File(sdcard);
        if (!dir.exists())
            dir.mkdirs();
        DownloadDialog downloadDialog=new DownloadDialog.Builder(MainActivity.this)
                .setParentDic(sdcard)
                .setProgressEnable(false)
                .setDownloadUrl(url)
                .setFileName("powercfg.sh")
                .build();
        downloadDialog.setOnTaskSuccess(new DownloadDialog.OnTaskSuccess() {
            @Override
            public void onSuccess() {
                Preference.saveBoolean(MainActivity.this,"custom",true);
                Preference.saveString(MainActivity.this,"code1","sh "+sdcard+"powercfg.sh powersave");
                Preference.saveString(MainActivity.this,"code2","sh "+sdcard+"powercfg.sh balance");
                Preference.saveString(MainActivity.this,"code3","sh "+sdcard+"powercfg.sh performance");
                Preference.saveString(MainActivity.this,"code4","sh "+sdcard+"powercfg.sh fast");
                getFragment(2).Refresh();
                getFragment(1).Refresh();
                refreshProp();
                ToastUtil.shortShow(MainActivity.this,"导入成功");
            }
        });
        downloadDialog.show();
    }
    public void pickPowercfg(){
        ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在从网络获取配置");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> types=CpuModel.getLastestTypes(MainActivity.this);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.cancel();
                        try {
                            if (types.contains("yc")){
                                ToastUtil.shortShow(MainActivity.this,"已经默认为你的设备选择了脚本");
                                downloadPowercfg("https://www.lanzous.com/tp/"+
                                        CpuModel.getYcUrl(MainActivity.this));

                            }else if (types.contains("855tune")){
                                ToastUtil.shortShow(MainActivity.this,"已经默认为你的eas设备选择了脚本,但是需要你手动安装magisk模块");
                                downloadPowercfg("https://www.lanzous.com/tp/i66muxc");
                            }else {
                                ToastUtil.shortShow(MainActivity.this,"自动检测暂未适配你的设备，请自行选择");
                                OptionsPickerView pvOptions = new  OptionsPickerBuilder(MainActivity.this, new OnOptionsSelectListener() {
                                    @Override
                                    public void onOptionsSelect(int options1, int options2, int options3, View v) {
                                        String url="https://www.lanzous.com/tp/"+downloadSites[options1][options2];
                                        try {
                                            Log.d("sss",url);
                                            downloadPowercfg(url);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                })
                                        .setSubmitText("确定")//确定按钮文字
                                        .setCancelText("取消")//取消按钮文字
                                        .setTitleText("选择机型")//标题
                                        .setSubCalSize(18)//确定和取消文字大小
                                        .setTitleSize(20)//标题文字大小
                                        .setCyclic(false, false, false)//循环与否
                                        .setSelectOptions(0, 0)  //设置默认选中项
                                        .setOutSideCancelable(false)//点击外部dismiss default true
                                        .build();
                                pvOptions.setPicker(Arrays.asList(options1Items), options2Items);
                                pvOptions.show();

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });

            }
        }).start();


    }
    public static void backupCustom(final Activity context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=1;i<5;i++){
                    try {
                        File file=new File(Sdcard.getPath(context)+"/lktMode/backup/");
                        if (!file.mkdirs()&&!file.exists()){
                            return;
                        }
                        BufferedWriter writer=new BufferedWriter(new FileWriter(Sdcard.getPath(context)+"/lktMode/backup/"+"code"+i+".sh"));
                        writer.write((String) Preference.getString(context,"code"+i));
                        writer.flush();
                        writer.close();
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.shortShow(context,"备份完成");
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }).start();


    }
    public static void restoreCustom(final Activity context,final CustomFragment fragment){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=1;i<5;i++){
                    restore(context,"code"+i,"code"+i+".sh");
                }
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.shortShow(context,"恢复完成");
                        fragment.updateList();
                    }
                });
            }
        }).start();


    }
    public static void restore(final Context context,final String key,final String name){
        try {
            String code="";
            File file=new File(Sdcard.getPath(context)+"/lktMode/backup/"+name);
            BufferedReader reader=new BufferedReader(new FileReader(file));
            String line=null;
            while ((line=reader.readLine())!=null){
                code+=line+"\n";
            }
            reader.close();
            Preference.saveString(context,key,code);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void showDialog(Context context, String str, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(context,R.style.AppDialog)
                .setNegativeButton("了解",listener)
                .setTitle("功能说明")
                .setMessage(str)
                .create().show();
    }
    public void setToolbar(String title, String subtitle, ActionMenuView.OnMenuItemClickListener listener){
        menuRemove();
        mNormalToolbar.setTitle(title);
        menuAdd(subtitle);
        actionmenuview.setOnMenuItemClickListener(listener);
    }
    public void setToolbar(String title, String[] subtitle, ActionMenuView.OnMenuItemClickListener listener){
        menuRemove();
        mNormalToolbar.setTitle(title);
        for (String sub:subtitle){
            menuAdd(sub);
        }
        actionmenuview.setOnMenuItemClickListener(listener);

    }
    public void menuAdd(String str) {
        actionmenuview.getMenu().add(str);
        if (actionmenuview.getMenu().size()==1){
            actionmenuview.getMenu().getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }if (actionmenuview.getMenu().size()>1){
            String title=actionmenuview.getMenu().getItem(1).getTitle().toString();
            if (title.equals("从网络导入")||title.equals("反馈群")||title.equals("一键生成")||title.equals("支持作者")||title.equals("异常反馈")){
                actionmenuview.getMenu().getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

        }

        //actionmenuview.showOverflowMenu();
    }
    public void switchPage(int i,boolean enableSwitch){
        if (enableSwitch){
            if (drawerLayout!=null){
                drawerLayout.closeDrawers();
            }
            viewPager.setCurrentItem(i,true);
        }
        switch (i){
            case 0:
                setToolbar("首页", new String[]{"支持作者","异常反馈","恢复官方状态"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "异常反馈":
                                AboutFragment.startQQGroup(MainActivity.this);
                                break;
                            case "支持作者":
                                AboutFragment.donation(MainActivity.this);
                                break;
                            case "恢复官方状态":
                                new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                        .setTitle("提示")
                                        .setMessage("软件初始化时会备份一次官方处理器状态，包括文件所有者，读写权限，内容，当你因为错误调校导致处理器异常时可以尝试恢复，你也手动备份你认为较好的处理器状态。")
                                        .setNegativeButton("备份", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ProgressDialog dialog=new ProgressDialog(MainActivity.this,R.style.AppDialog);
                                                dialog.setCancelable(false);
                                                dialog.setMessage("正在备份处理器信息");
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Preference.saveString(MainActivity.this,"offical",CpuManager.getInstance().backup());
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                dialog.dismiss();
                                                                ToastUtil.shortShow(MainActivity.this,"备份完成").show();
                                                            }
                                                        });
                                                    }
                                                }).start();
                                                dialog.show();
                                            }
                                        })
                                        .setPositiveButton("恢复", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ProgressDialog dialog=new ProgressDialog(MainActivity.this,R.style.AppDialog);
                                                dialog.setCancelable(false);
                                                dialog.setMessage("正在恢复处理器信息");
                                                dialog.show();
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        RootUtils.runCommand((String) Preference.getString(MainActivity.this,"offical"));
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                dialog.dismiss();
                                                                ToastUtil.shortShow(MainActivity.this,"恢复成功");
                                                            }
                                                        });
                                                    }
                                                }).start();

                                            }
                                        })
                                        .show();
                                break;
                        }
                        return true;
                    }
                });
                break;
            case 1:
                setToolbar("自定义调度", new String[]{"保存","一键生成","备份","还原"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        CustomFragment fragment=(CustomFragment)fragments.get(1);
                        switch (item.getTitle().toString()){
                            case "保存":
                                fragment.saveAll();
                                ToastUtil.shortShow(MainActivity.this,"已保存");
                                break;
                            case "备份":
                                backupCustom(MainActivity.this);
                                break;
                            case "一键生成":
                                new File(Sdcard.getPath(MainActivity.this)+"/lktMode/powercfg/powercfg.sh").delete();
                                ProgressDialog progressDialog=new ProgressDialog(MainActivity.this,R.style.AppDialog);
                                progressDialog.setCancelable(false);
                                progressDialog.setMessage("生成调度中");
                                progressDialog.show();
                                if (!SystemInfo.getIsDonated()){
                                    ToastUtil.shortAlert(MainActivity.this,"该功能需要捐赠版支持，非捐赠版仅能生成均衡模式");
                                    if (!CpuManager.getInstance().isEasKernel()){
                                        ToastUtil.shortAlert(MainActivity.this,"非eas内核暂未支持");
                                        progressDialog.dismiss();
                                    }else {
                                        try {
                                            Preference.saveString(MainActivity.this,"code2",fragment.easTune(3,CpuBoost.isAddition()));
                                        }catch (Exception e){
                                            e.printStackTrace();
                                            ToastUtil.shortAlert(MainActivity.this,"生成失败，请稍后重试");
                                        }finally {
                                            fragment.refresh();
                                            progressDialog.dismiss();
                                        }

                                    }
                                    break;
                                }
                                if (!CpuManager.getInstance().isEasKernel()){
                                    ToastUtil.shortAlert(MainActivity.this,"非eas内核暂未支持");
                                    progressDialog.dismiss();
                                    break;
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            boolean isAddition=CpuBoost.isAddition();
                                            int[] indexs= Tune.getindexs(MainActivity.this);
                                            String code1=fragment.easTune(indexs[0],isAddition);
                                            String code2=fragment.easTune(indexs[1],isAddition);
                                            String code3=fragment.easTune(indexs[2],isAddition);
                                            String code4=fragment.easTune(indexs[3],isAddition);
                                            Preference.saveString(MainActivity.this,"code1",code1);
                                            Preference.saveString(MainActivity.this,"code2",code2);
                                            Preference.saveString(MainActivity.this,"code3",code3);
                                            Preference.saveString(MainActivity.this,"code4",code4);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.cancel();
                                                    fragment.refresh();
                                                    ToastUtil.shortShow(MainActivity.this,"生成成功");
                                                }
                                            });
                                        }catch (Exception e){
                                            e.printStackTrace();
                                            progressDialog.cancel();
                                            ToastUtil.shortAlert(MainActivity.this,"生成失败，请稍后重试");
                                        }

                                    }
                                }).start();




                                break;
                            case "还原":
                                restoreCustom(MainActivity.this,fragment);
                                break;
                        }
                        return true;
                    }
                });
                break;
            case 2:
                setToolbar("动态脚本", new String[]{"从网络导入","使用说明"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "从网络导入":
                                pickPowercfg();
                                break;
                            case "使用说明":
                                showDialog(MainActivity.this,"动态脚本\n动态脚本是自定义调度的附属功能，为了防止出现冲突，所以启动动态脚本后自定义调度将处于不可编辑状态，你需要在省电、均衡、游戏、极限四个挡中输入参数，powersave、balance、performance、turbo、level 0-6来合理调整系统cpu功耗。",null);
                                break;
                        }
                        return true;
                    }
                });
                break;
            case 3:
                setToolbar("游戏加速", new String[]{"设置","使用说明"},new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //Log.d("ssss",item.getTitle()+"");
                        switch (item.getTitle().toString()){
                            case "设置":
                                startActivity(new Intent(MainActivity.this, GameBoostActivity.class));
                                break;
                            case "使用说明":
                                showDialog(MainActivity.this,"\t游戏加速：\n类似于许多rom内置的游戏加速功能，会在进入游戏列表中的游戏时自动切换到游戏模式(如果你有更高的性能要求，可以在设置中定义特定的调度)；并且在退出该游戏后，自动切换到默认模式；由于采用的横屏监听原理实现，所以仅支持横屏游戏，限数5个。\n\n\t游戏辅助：\n进入游戏时，自动调整亮度、音量，关闭或打开自动亮度，省去了一些操作，退出游戏时会自动恢复进入游戏前的状态。需要在该页面的设置——添加设置中，设置特定的辅助操作。",null);
                                break;
                        }
                        return true;
                    }
                });
                break;
            case 4:

                setToolbar("锁屏清理", new String[]{"设置", "使用说明"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "设置":
                                startActivity(new Intent(MainActivity.this, SleepSettingActivity.class));
                                break;
                            case "使用说明":
                                showDialog(MainActivity.this,"\t锁屏省电：\n锁屏后会自动切换到最低功耗的模式（默认是省电模式，你也可以在该页面的设置中设置特定的调度，达到最佳的省电效果）\n\n\t锁屏自动清理：\n锁屏后会强行停止你加入列表的所有应用，默认执行时在锁屏后200s，可以在该页面的设置中调整延迟\n\n\tIM优化耗电：锁屏后清理掉QQ、微信的前台，保留后台消息服务，需要你将QQ或者微信加入清理列表，如果优化后无法获取消息，请将IM软件移出列表",null);
                                break;
                        }
                        return true;
                    }
                });
                break;

        }

    }
    public void menuRemove(){
        actionmenuview.getMenu().clear();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbarmenu ,actionmenuview.getMenu());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== RESULT_OK){
             if (requestCode==11){
                List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
                if (MainActivity.copyFile(list.get(0),Sdcard.getPath(MainActivity.this)+"/lktMode/powercfg/powercfg.sh")){
                    PowercfgFragment fragment=(PowercfgFragment)getFragment(2);
                    fragment.change();
                }else {
                    ToastUtil.shortAlert(MainActivity.this,"导入失败");
                }
            }
            if (requestCode==12){
                Uri uri=data.getData();
                grantUriPermission(getPackageName(),uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (copyFileQ(data.getData(), Sdcard.getPath(MainActivity.this) +"/lktMode/powercfg/powercfg.sh")){
                    PowercfgFragment fragment=(PowercfgFragment)getFragment(2);
                    fragment.change();
                    ToastUtil.shortShow(MainActivity.this,"导入成功");
                }else {
                    ToastUtil.shortAlert(MainActivity.this,"导入失败");
                }
            }
        }
    }
    public static boolean isContentUriExists(Context context, Uri uri){
        if (null == context) {
            return false;
        }
        ContentResolver cr = context.getContentResolver();
        try {
            AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
            if (null == afd) {
                return false;
            } else {
                try {
                    afd.close();
                } catch (IOException e) {
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        }

        return true;
    }

    private static FileDescriptor getInputStream(Context context, Uri uri){
        ContentResolver cr = context.getContentResolver();
        try {
            AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
            if (null == afd) {
                return afd.getFileDescriptor();
            } else {
                try {
                    afd.close();
                } catch (IOException e) {
                }
            }
        } catch (ParcelFormatException|FileNotFoundException e) {
            return null;
        }
        return null;
    }
    public boolean copyFileQ(Uri oldPath$Name, String newPath$Name) {
        Log.d("uri",oldPath$Name.toString());
        File dist=new File(newPath$Name.substring(0,newPath$Name.lastIndexOf("/")));

        if (!dist.exists()){
            dist.mkdirs();
        }
        if (!isContentUriExists(MainActivity.this,oldPath$Name)){
            return false;
        }
        try {
            copy(getContentResolver().openInputStream(oldPath$Name),new FileOutputStream(new File(newPath$Name)));
            return true;
        }catch (Exception e){
            //Log.d("exc",e.toString());
            e.printStackTrace();
            return false;
        }


    }
    public static void copy(File src, ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        FileInputStream istream = new FileInputStream(src);
        try {
            FileOutputStream ostream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
            try {
                copy(istream, ostream);
            } finally {
                ostream.close();
            }
        } finally {
            istream.close();
        }
    }

    public static void copy(FileDescriptor parcelFileDescriptor, File dst) throws IOException {
        FileInputStream istream = new FileInputStream(parcelFileDescriptor);
        try {
            FileOutputStream ostream = new FileOutputStream(dst);
            try {
                copy(istream, ostream);
            } finally {
                ostream.close();
            }
        } finally {
            istream.close();
        }
    }

    public static void copy(ParcelFileDescriptor parcelFileDescriptor, File dst) throws IOException {
        FileInputStream istream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
        try {
            FileOutputStream ostream = new FileOutputStream(dst);
            try {
                copy(istream, ostream);
            } finally {
                ostream.close();
            }
        } finally {
            istream.close();
        }
    }


    public static void copy(InputStream ist, OutputStream ost) throws IOException {
        byte[] buffer = new byte[4096];
        int byteCount = 0;
        while ((byteCount = ist.read(buffer)) != -1) {  // 循环从输入流读取 buffer字节
            Log.d("buffer",buffer+"");
            ost.write(buffer, 0, byteCount);        // 将读取的输入流写入到输出流
        }
    }

    public static boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File newF=new File(newPath$Name.substring(0,newPath$Name.lastIndexOf("/")));
            if(!newF.mkdirs()&&!newF.exists()){
                return false;
            }
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

        /* 如果不需要打log，可以使用下面的语句
        if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
            return false;
        }
        */

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);    //读入原文件
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            int currentItem=viewPager.getCurrentItem();
            drawerLayout.closeDrawers();
            if (currentItem!=0){
                viewPager.setCurrentItem(0,false);
                setToolbar("首页", new String[]{"支持作者","异常反馈","恢复官方状态"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "支持作者":
                                AboutFragment.donation(MainActivity.this);
                                break;
                            case "异常反馈":
                                AboutFragment.startQQGroup(MainActivity.this);
                                break;
                            case "恢复官方状态":
                                new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                        .setTitle("提示")
                                        .setMessage("软件初始化时会备份一次官方处理器状态，包括文件所有者，读写权限，内容，当你因为错误调校导致处理器异常时可以尝试恢复，你也手动备份你认为较好的处理器状态。")
                                        .setNegativeButton("备份", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ProgressDialog dialog=new ProgressDialog(MainActivity.this,R.style.AppDialog);
                                                dialog.setCancelable(false);
                                                dialog.setMessage("正在备份处理器信息");
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Preference.saveString(MainActivity.this,"offical",CpuManager.getInstance().backup());
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                dialog.dismiss();
                                                                ToastUtil.shortAlert(MainActivity.this,"备份完成");
                                                            }
                                                        });
                                                    }
                                                }).start();
                                                dialog.show();
                                            }
                                        })
                                        .setPositiveButton("恢复", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ProgressDialog dialog=new ProgressDialog(MainActivity.this,R.style.AppDialog);
                                                dialog.setMessage("正在恢复处理器信息");
                                                dialog.show();
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        RootUtils.runCommand((String) Preference.getString(MainActivity.this,"offical","String"));
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                dialog.dismiss();
                                                                ToastUtil.shortAlert(MainActivity.this,"恢复完成");
                                                            }
                                                        });
                                                    }
                                                }).start();

                                            }
                                        })
                                        .show();
                                break;
                        }
                        return true;
                    }
                });

                return false;
            }
            if(!mBackKeyPressed){
                ToastUtil.shortShow(MainActivity.this,"再按一次退出程序");
                mBackKeyPressed = true;
                new Timer().schedule(new TimerTask() {//延时两秒，如果超出则擦错第一次按键记录
                    @Override
                    public void run() {
                        mBackKeyPressed = false;
                    }
                }, 2000);
            }else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode,event);
    }


    @Override
    protected void onDestroy() {
        Intent intent = new Intent("com.kai.lktMode.restart");
        // 兼容安卓8.0  参数为（应用包名，广播路径）,但是只局限于特定的应用能收到广播
        intent.setComponent(new ComponentName(getApplication().getPackageName(),
                "com.kai.lktMode.receiver.AutoReceiver"));
        sendBroadcast(intent);
        super.onDestroy();
    }


    public void closeDrawer(boolean isLand){
        ValueAnimator animator=ValueAnimator.ofFloat(1,0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float value=(Float)animator.getAnimatedValue();
                linearLayout0.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,value));
                if (value==0){
                    linearLayout0.setVisibility(View.INVISIBLE);
                }
            }
        });
        animator.setDuration(500);
        animator.start();
    }
    public void openDrawer(boolean island){

        linearLayout0.setVisibility(View.VISIBLE);
        ValueAnimator animator=ValueAnimator.ofFloat(0,1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float value=(Float)animator.getAnimatedValue();
                linearLayout0.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,value));
            }
        });
        animator.setDuration(500);
        animator.start();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE :// 横屏
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,GravityCompat.START);
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                openDrawer(true);
                break;
            case Configuration.ORIENTATION_PORTRAIT :// 竖屏
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,GravityCompat.START);
                closeDrawer(true);
                break;
        }
    }
}
