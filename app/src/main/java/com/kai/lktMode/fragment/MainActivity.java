package com.kai.lktMode.fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.kai.lktMode.AutoService;
import com.kai.lktMode.GameBoostActivity;
import com.kai.lktMode.R;
import com.kai.lktMode.ServiceStatusUtils;
import com.kai.lktMode.SleepSettingActivity;
import com.kai.lktMode.ViewPagerSlide;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    ActionBarDrawerToggle mActionBarDrawerToggle;
    private Toolbar mNormalToolbar;
    private List<MyFragment> fragments=new ArrayList<>();
    private ViewPagerSlide viewPager;
    private FragmentManager fragmentManager;
    private static boolean mBackKeyPressed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNormalToolbar=(Toolbar) findViewById(R.id.simple_toolbar);
        initToolbar();
        fragments.add(new MainFragment());
        fragments.add(new SettingFragment());
        fragments.add(new CustomFragment());
        fragments.add(new GameFragment());
        fragments.add(new LockFragment());
        fragments.add(new AboutFragment());
        viewPager=(ViewPagerSlide) findViewById(R.id.viewPager);
        viewPager.setSlide(false);
        viewPager.setOffscreenPageLimit(5);
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
        setToolbar("首页", "捐赠", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AboutFragment.donation(MainActivity.this);
            }
        });
        if (!ServiceStatusUtils.isServiceRunning(this, AutoService.class)){
            Intent intent=new Intent(this,AutoService.class);
            intent.setAction("reset");
            startService(intent);
        }
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
                        setToolbar("首页", "捐赠", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AboutFragment.donation(MainActivity.this);
                            }
                        });
                        break;
                    case R.id.setting:
                        MainFragment m=(MainFragment)fragments.get(0);
                        Bundle bundle=new Bundle();
                        bundle.putString("passage",m.getPassage());
                        fragments.get(1).setArguments(bundle);
                        viewPager.setCurrentItem(1,false);
                        drawerLayout.closeDrawers();
                        setToolbar("设置", "", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                        break;
                    case R.id.lab:
                        viewPager.setCurrentItem(3,false);
                        drawerLayout.closeDrawers();
                        setToolbar("游戏加速", "设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(MainActivity.this, GameBoostActivity.class));
                            }
                        });
                        break;
                    case R.id.about:
                        viewPager.setCurrentItem(5,false);
                        drawerLayout.closeDrawers();
                        setToolbar("关于", "", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                        break;
                    case R.id.custom:
                        viewPager.setCurrentItem(2,false);
                        drawerLayout.closeDrawers();
                        setToolbar("自定义调度", "保存", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomFragment fragment=(CustomFragment)fragments.get(2);
                                fragment.saveAll();
                            }
                        });
                        break;
                    case R.id.lock:
                        viewPager.setCurrentItem(4,false);
                        drawerLayout.closeDrawers();
                        setToolbar("锁屏清理", "设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(MainActivity.this, SleepSettingActivity.class));
                            }
                        });
                        default:break;

                }

                return true;
            }
        });

    }

    public void setToolbar(String title,String subtitle,View.OnClickListener listener){
        mNormalToolbar.setTitle(title);
        TextView extra=(TextView)findViewById(R.id.extra);
        extra.setText(subtitle);
        extra.setOnClickListener(listener);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            int currentItem=viewPager.getCurrentItem();
            if (currentItem!=0){
                viewPager.setCurrentItem(0,false);
                setToolbar("首页", "捐赠", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AboutFragment.donation(MainActivity.this);
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
                System.exit(0);
            }
            return false;
        }
        return super.onKeyDown(keyCode,event);
    }
}
