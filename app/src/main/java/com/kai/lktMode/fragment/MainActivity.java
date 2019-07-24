package com.kai.lktMode.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.android.material.navigation.NavigationView;
import com.kai.lktMode.AutoService;
import com.kai.lktMode.BaseActivity;
import com.kai.lktMode.BuildConfig;
import com.kai.lktMode.CpuService;
import com.kai.lktMode.CrashHandler;
import com.kai.lktMode.DownloadUtil;
import com.kai.lktMode.GameBoostActivity;
import com.kai.lktMode.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.ServiceStatusUtils;
import com.kai.lktMode.SleepSettingActivity;
import com.kai.lktMode.ViewPagerSlide;
import com.kai.lktMode.tool.CloudLoginDialog;
import com.kai.lktMode.tool.UpdateUtil;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends BaseActivity {
    private String[] options1Items=new String[]{"高通","麒麟","猎户座","联发科","英特尔"};
    private String[][] downloadSites={{"sd_625_626","sd_652_650","sd_636","sd_660","sd_801_800_805","sd_801_808","sd_820_821","sd_835","sd_845"},
            {"exynos_8895","exynos_8890","exynos_7420"},{"kirin_970","kirin_960","kirin_950_955"},{"helio_x20_x25","helio_x10"},{"atom_z3560_z3580"}};
    private List<List<String>> options2Items=new ArrayList<>();
    ActionBarDrawerToggle mActionBarDrawerToggle;
    private Toolbar mNormalToolbar;
    private List<MyFragment> fragments=new ArrayList<>();
    private ViewPagerSlide viewPager;
    private FragmentManager fragmentManager;
    private ProgressDialog dialog;
    private Intent bindIntent;
    private ActionMenuView actionmenuview;
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
        bindService(bindIntent,connection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (bindIntent!=null)
            unbindService(connection);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //new ProgressDialog(this).show();
        if (!(Boolean)Preference.get(this,"isFirstRun","Boolean")){
            startActivity(new Intent(this,StartActivity.class));
        }

        //startActivity(new Intent(this,StartActivity.class));
        mNormalToolbar=(Toolbar) findViewById(R.id.simple_toolbar);
        initToolbar();
        initDownloadDialog();

        fragments.add(new MainFragment());
        fragments.add(new SettingFragment());
        fragments.add(new CustomFragment());
        fragments.add(new PowercfgFragment());
        fragments.add(new GameFragment());
        fragments.add(new LockFragment());
        fragments.add(new AboutFragment());

        viewPager=(ViewPagerSlide) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(7);
        fragmentManager = getSupportFragmentManager();
        adapter=new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };
        viewPager.setAdapter(adapter);

        setToolbar("首页", new String[]{"捐赠","反馈群"}, new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getTitle().toString()){
                    case "捐赠":
                        AboutFragment.donation(MainActivity.this,null);
                        break;
                    case "反馈群":
                        AboutFragment.startQQGroup(MainActivity.this);
                        break;
                }
                return true;
            }
        });
        if (!ServiceStatusUtils.isServiceRunning(this, AutoService.class)){
            Intent intent=new Intent(this,AutoService.class);
            intent.setAction("reset");
            startService(intent);
        }
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
                options2Items.add(options2Items_03);
                options2Items.add(options2Items_02);
                options2Items.add(options2Items_04);
                options2Items.add(options2Items_05);
            }
        }).start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) this.getSystemService(POWER_SERVICE);

            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(this.getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                Toast.makeText(this,"请手动前往设置-电池-电池优化中取消限制",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                // 设置ComponentName参数1:packagename参数2:Activity路径
                ComponentName cn = ComponentName.unflattenFromString("com.android.settings/.Settings$HighPowerApplicationsActivity");
                intent.setComponent(cn);
                startActivity(intent);
            } else {

            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final UpdateUtil updateUtil=new UpdateUtil();
                //updateUtil.backup();
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (updateUtil.isToUpdate()){
                            new AlertDialog.Builder(MainActivity.this,R.style.AppDialog)
                                    .setTitle("版本更新:"+updateUtil.getVersionName())
                                    .setMessage(updateUtil.getVersionLog())
                                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Toast.makeText(MainActivity.this,"请选择酷安市场下载",Toast.LENGTH_SHORT).show();
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


    public static boolean ignoreBatteryOptimization(Activity activity) {
        boolean isIgnored=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) activity.getSystemService(POWER_SERVICE);

            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
            isIgnored=hasIgnored;
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+activity.getPackageName()));
                activity.startActivityForResult(intent,12);
            } else {

            }
        }else {
            return true;
        }
        return isIgnored;
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
        actionmenuview = (ActionMenuView) findViewById(R.id.actionmenuview);
        mNormalToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"s",Toast.LENGTH_SHORT).show();
            }
        });
        final DrawerLayout drawerLayout=findViewById(R.id.drawer);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mNormalToolbar,R.string.app_name,R.string.app_name);
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        mActionBarDrawerToggle.setHomeAsUpIndicator(R.mipmap.ic_launcher);//channge the icon,改变图标
        mActionBarDrawerToggle.syncState();////show the default icon and sync the DrawerToggle state,如果你想改变图标的话，这句话要去掉。这个会使用默认的三杠图标
        drawerLayout.setDrawerListener(mActionBarDrawerToggle);//关联 drawerlayout
        NavigationView view=findViewById(R.id.navigationView);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int ID=menuItem.getItemId();
                switch (ID){
                    case R.id.main:
                        switchPage(0,drawerLayout);
                        break;
                    case R.id.setting:
                        switchPage(1,drawerLayout);
                        break;
                    case R.id.lab:
                        switchPage(4,drawerLayout);
                        break;
                    case R.id.about:
                        switchPage(6,drawerLayout);
                        break;
                    case R.id.custom:
                        switchPage(2,drawerLayout);
                        break;
                    case R.id.powercfg:
                        switchPage(3,drawerLayout);
                        break;
                    case R.id.lock:
                        switchPage(5,drawerLayout);
                        break;
                    case R.id.cloud:
                        getOnCloud();
                        break;
                    default:break;

                }

                return true;
            }
        });

    }
    private void getOnCloud(){
        boolean isLogin=(Boolean)Preference.get(MainActivity.this,"cloud",false);
        if (isLogin){
            UpdateUtil.list(MainActivity.this);
        }else {
            final CloudLoginDialog dialog=new CloudLoginDialog(MainActivity.this,R.style.AppDialog);
            dialog.setOnLoginClick(new CloudLoginDialog.OnLoginClick() {
                @Override
                public void onclick(String user, String password) {
                   UpdateUtil.login(user,password,dialog,MainActivity.this);
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

    private void initDownloadDialog(){
        dialog=new ProgressDialog(this,R.style.AppDialog);
        dialog.setMessage("正在下载");
    }
    public void refreshProp(){
        MainFragment fragment=(MainFragment)fragments.get(0);
        fragment.readProp(false);
    }
    public MyFragment getFragment(int i){
        return fragments.get(i);
    }
    private void downloadPowercfg(final String url) throws Exception{
        final String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/";
        new Thread(new Runnable() {
            @Override
            public void run() {
                DownloadUtil.get().download(url, sdcard, "powercfg.sh",
                        new DownloadUtil.OnDownloadListener() {
                            @Override
                            public void onDownloadSuccess(File file) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        Preference.save(MainActivity.this,"custom",true);
                                        Toast.makeText(MainActivity.this,"导入成功",Toast.LENGTH_SHORT).show();
                                        Preference.save(MainActivity.this,"code1","sh "+sdcard+"powercfg.sh powersave");
                                        Preference.save(MainActivity.this,"code2","sh "+sdcard+"powercfg.sh balance");
                                        Preference.save(MainActivity.this,"code3","sh "+sdcard+"powercfg.sh performance");
                                        Preference.save(MainActivity.this,"code4","sh "+sdcard+"powercfg.sh fast");
                                        getFragment(3).Refresh();
                                        getFragment(2).Refresh();
                                        refreshProp();
                                    }
                                });
                            }

                            @Override
                            public void onDownloading(final int progress) {

                            }

                            @Override
                            public void onDownloadFailed(Exception e) {

                            }
                        });
            }
        }).start();

    }
    public void pickPowercfg(){
        OptionsPickerView pvOptions = new  OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                dialog.setMessage("下载中");
                dialog.setCancelable(false);
                dialog.show();
                String url="http://puq8bljed.bkt.clouddn.com/"+downloadSites[options1][options2]+".sh";
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
    public static void backupCustom(final Activity context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=1;i<5;i++){
                    try {
                        File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/backup/");
                        if (!file.mkdirs()&&!file.exists()){
                            return;
                        }
                        BufferedWriter writer=new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/backup/"+"code"+i+".sh"));
                        writer.write((String) Preference.get(context,"code"+i,"String"));
                        writer.flush();
                        writer.close();
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"备份完成",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context,"恢复完成",Toast.LENGTH_SHORT).show();
                        fragment.updateList();
                    }
                });
            }
        }).start();


    }
    public static void restore(final Context context,final String key,final String name){
        try {
            String code="";
            File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/backup/"+name);
            BufferedReader reader=new BufferedReader(new FileReader(file));
            String line=null;
            while ((line=reader.readLine())!=null){
                code+=line+"\n";
            }
            reader.close();
            Preference.save(context,key,code);
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
            if (actionmenuview.getMenu().getItem(1).getTitle().equals("从网络导入")||actionmenuview.getMenu().getItem(1).getTitle().equals("反馈群")){
                actionmenuview.getMenu().getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

        }

        //actionmenuview.showOverflowMenu();
    }
    public void switchPage(int i,DrawerLayout drawerLayout){
        if (drawerLayout!=null){
            drawerLayout.closeDrawers();
        }
        viewPager.setCurrentItem(i,true);
        switch (i){
            case 0:
                setToolbar("首页", new String[]{"捐赠","反馈群"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "捐赠":
                                AboutFragment.donation(MainActivity.this,null);
                                break;
                            case "反馈群":
                                AboutFragment.startQQGroup(MainActivity.this);
                                break;
                        }
                        return true;
                    }
                });
                break;
            case 1:
                MainFragment m=(MainFragment)fragments.get(0);
                final Bundle bundle=new Bundle();
                //bundle.putString("passage",m.getPassage());
                setToolbar("设置", "使用说明", new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getOrder()==0){
                            showDialog(MainActivity.this,
                                    "开机自启:\n每次开机会自动切换到你所选定的默认模式\n\n默认模式:\n选择你最常用的模式，在开机自启或其他辅助功能中作为默认的模式",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                        }
                        return true;
                    }
                });
                break;
            case 2:
                setToolbar("自定义调度", new String[]{"保存","备份","还原"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "保存":
                                CustomFragment fragment=(CustomFragment)fragments.get(2);
                                fragment.saveAll();
                                Toast.makeText(MainActivity.this,"已保存",Toast.LENGTH_SHORT).show();
                                break;
                            case "备份":
                                backupCustom(MainActivity.this);
                                break;
                            case "还原":
                                CustomFragment fragment1=(CustomFragment) fragments.get(2);
                                restoreCustom(MainActivity.this,fragment1);
                                break;
                        }
                        return true;
                    }
                });
                break;
            case 3:
                setToolbar("动态脚本", new String[]{"保存","从网络导入","使用说明"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "保存":
                                PowercfgFragment fragment=(PowercfgFragment) fragments.get(3);
                                fragment.saveAll();
                                Toast.makeText(MainActivity.this,"已保存",Toast.LENGTH_SHORT).show();
                                break;
                            case "从网络导入":
                                pickPowercfg();
                                break;
                            case "使用说明":
                                showDialog(MainActivity.this,"动态脚本\n动态脚本是自定义调度的附属功能，为了反正出现冲突，所以启动动态脚本后自定义调度将处于不可编辑状态，你需要在省电、均衡、游戏、极限四个挡中输入参数，powersave、balance、performance、turbo、level 0-6来合理调整系统cpu功耗。",null);
                                break;
                        }
                        return true;
                    }
                });
                break;
            case 4:
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
            case 5:

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
            case 6:
                setToolbar("关于", "",null);
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
            if (currentItem!=0){
                viewPager.setCurrentItem(0,false);
                setToolbar("首页", new String[]{"捐赠","反馈群"}, new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "捐赠":
                                AboutFragment.donation(MainActivity.this,null);
                                break;
                            case "反馈群":
                                AboutFragment.startQQGroup(MainActivity.this);
                                break;
                        }
                        return true;
                    }
                });
                return false;
            }
            if(!mBackKeyPressed){
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mBackKeyPressed = true;
                new Timer().schedule(new TimerTask() {//延时两秒，如果超出则擦错第一次按键记录
                    @Override
                    public void run() {
                        mBackKeyPressed = false;
                    }
                }, 2000);
            }else {
                finish();
                //System.exit(0);
            }
            return false;
        }
        return super.onKeyDown(keyCode,event);
    }


    @Override
    protected void onDestroy() {

        //unbindService(connection);
        startService(new Intent(this,AutoService.class));
        super.onDestroy();
    }


}
