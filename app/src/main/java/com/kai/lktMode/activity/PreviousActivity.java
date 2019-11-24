package com.kai.lktMode.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kai.lktMode.R;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.fragment.MyFragment;
import com.kai.lktMode.fragment.PreviousSettingFragment;
import com.kai.lktMode.fragment.PreviousSettingv2Fragment;
import com.kai.lktMode.fragment.PreviousSettingv3Fragment;
import com.kai.lktMode.fragment.PreviousSettingv4Fragment;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.widget.ViewPagerSlide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviousActivity extends BaseActivity {
    FragmentManager fragmentManager;
    FragmentStatePagerAdapter adapter;
    @BindView(R.id.viewPager)
    ViewPagerSlide viewPager;
    @BindView(R.id.back) TextView back;
    @BindView(R.id.forward) TextView forward;
    public String tune_type="diy";
    List<MyFragment> fragments=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous);
        ButterKnife.bind(this);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current=viewPager.getCurrentItem();
                if (current==0)
                    return;
                viewPager.setCurrentItem(current-1,true);
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current=viewPager.getCurrentItem();
                if (!fragments.get(current).isPassed()){
                    Toast.makeText(PreviousActivity.this,"请完善必选操作后继续",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (current==fragments.size()-1){
                    ProgressDialog dialog=new ProgressDialog(PreviousActivity.this,R.style.AppDialog);
                    dialog.setMessage("正在保存设置");
                    dialog.show();
                    Preference.saveBoolean(PreviousActivity.this,"init",true);
                    startActivity(new Intent(PreviousActivity.this,MainActivity.class));
                    finish();
                    dialog.dismiss();
                }
                fragments.get(current).next();
                viewPager.setCurrentItem(current+1,true);
            }
        });
        fragments.add(0,new PreviousSettingFragment());
        fragments.add(1,new PreviousSettingv2Fragment());
        fragments.add(2,new PreviousSettingv3Fragment());
        fragments.add(3,new PreviousSettingv4Fragment());
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        fragments.get(0).Refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragments.get(viewPager.getCurrentItem()).Refresh();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        fragments.get(viewPager.getCurrentItem()).Refresh();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            int current=viewPager.getCurrentItem();
            if (current==0)
                return false;
            viewPager.setCurrentItem(current-1,true);
            return false;
        }
        return super.onKeyDown(keyCode,event);
    }

}
