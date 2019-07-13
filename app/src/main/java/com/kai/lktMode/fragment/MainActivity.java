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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.kai.lktMode.AutoService;
import com.kai.lktMode.BaseActivity;
import com.kai.lktMode.CrashHandler;
import com.kai.lktMode.GameBoostActivity;
import com.kai.lktMode.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.ServiceStatusUtils;
import com.kai.lktMode.SleepSettingActivity;
import com.kai.lktMode.ViewPagerSlide;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends BaseActivity {
    ActionBarDrawerToggle mActionBarDrawerToggle;
    private Toolbar mNormalToolbar;
    private List<MyFragment> fragments=new ArrayList<>();
    private ViewPagerSlide viewPager;
    private FragmentManager fragmentManager;
    private ActionMenuView actionmenuview;
    private static boolean mBackKeyPressed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this,StartActivity.class));
        setContentView(R.layout.activity_main);
        mNormalToolbar=(Toolbar) findViewById(R.id.simple_toolbar);
        initToolbar();
        fragments.add(new MainFragment());
        fragments.add(new SettingFragment());
        fragments.add(new CustomFragment());
        fragments.add(new PowercfgFragment());
        fragments.add(new GameFragment());
        fragments.add(new LockFragment());
        fragments.add(new AboutFragment());
        viewPager=(ViewPagerSlide) findViewById(R.id.viewPager);
        viewPager.setSlide(false);
        viewPager.setOffscreenPageLimit(7);
        fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });
        setToolbar("首页", "捐赠", new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getOrder()==0){
                    AboutFragment.donation(MainActivity.this);
                }
                return true;
            }
        });
        if (!ServiceStatusUtils.isServiceRunning(this, AutoService.class)){
            Intent intent=new Intent(this,AutoService.class);
            intent.setAction("reset");
            startService(intent);
        }
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
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivityForResult(intent,12);
                }
            } else {
                Log.d("ignoreBattery", "hasIgnored");
            }
        }else {
            return true;
        }
        return isIgnored;
    }
    private void initToolbar() {
        //设置menu
        //设置menu的点击事件
        mNormalToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
                return true;
            }
        });
        setSupportActionBar(mNormalToolbar);

        actionmenuview = (ActionMenuView) findViewById(R.id.actionmenuview);
        //actionmenuview.setOverflowIcon(null);
        //设置左侧NavigationIcon点击事件
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
                        viewPager.setCurrentItem(0,false);
                        drawerLayout.closeDrawers();
                        setToolbar("首页", "捐赠", new ActionMenuView.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getOrder()==0){
                                    AboutFragment.donation(MainActivity.this);
                                }
                                return true;
                            }
                        });
                        break;
                    case R.id.setting:
                        MainFragment m=(MainFragment)fragments.get(0);
                        final Bundle bundle=new Bundle();
                        bundle.putString("passage",m.getPassage());
                        fragments.get(1).setArguments(bundle);
                        viewPager.setCurrentItem(1,false);
                        drawerLayout.closeDrawers();
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
                    case R.id.lab:
                        viewPager.setCurrentItem(4,false);
                        drawerLayout.closeDrawers();
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
                    case R.id.about:
                        viewPager.setCurrentItem(6,false);
                        drawerLayout.closeDrawers();
                        setToolbar("关于", "",null);
                        break;
                    case R.id.custom:
                        viewPager.setCurrentItem(2,false);
                        drawerLayout.closeDrawers();
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
                                        for (int i=1;i<5;i++){
                                            backupCustom(MainActivity.this,"code"+i+".sh","code"+i);
                                        }
                                        Toast.makeText(MainActivity.this,"备份完成",Toast.LENGTH_SHORT).show();
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
                    case R.id.powercfg:
                        viewPager.setCurrentItem(3,false);
                        drawerLayout.closeDrawers();
                        setToolbar("自定义调度-动态脚本", new String[]{"保存","从网络导入"}, new ActionMenuView.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getTitle().toString()){
                                    case "保存":
                                        PowercfgFragment fragment=(PowercfgFragment) fragments.get(3);
                                        fragment.saveAll();
                                        Toast.makeText(MainActivity.this,"已保存",Toast.LENGTH_SHORT).show();
                                        break;
                                    case "导入网络脚本":

                                        break;
                                }
                                return true;
                            }
                        });
                        break;
                    case R.id.lock:
                        viewPager.setCurrentItem(5,false);
                        drawerLayout.closeDrawers();
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
                        default:break;

                }

                return true;
            }
        });

    }
    public static void backupCustom(final Activity context,final String name,final String key){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/backup/");
                    if (!file.mkdirs()&&!file.exists()){
                        return;
                    }
                    BufferedWriter writer=new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/backup/"+name));
                    writer.write((String) Preference.get(context,key,"String"));
                    writer.flush();
                    writer.close();
                }catch (Exception e){
                    e.printStackTrace();
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
    private static void restore(final Context context,final String key,final String name){
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
        }

        //actionmenuview.showOverflowMenu();
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
    public static String getRealPach(Uri uri,Context context){
        String path="";
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        if (uri.getPath().contains("external_files")){
            int begin=uri.getPath().indexOf("/",1);
            path=Environment.getExternalStorageDirectory()+uri.getPath().substring(begin);
        }if (uri.getPath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())){
            path=uri.getPath();
        }
        return path;
    }
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
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
                setToolbar("首页", "捐赠", new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getOrder()==0){
                            AboutFragment.donation(MainActivity.this);
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
        super.onDestroy();
        startService(new Intent(this,AutoService.class));
    }


}
