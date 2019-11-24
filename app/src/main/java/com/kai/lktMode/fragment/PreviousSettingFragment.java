package com.kai.lktMode.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.BuildConfig;
import com.kai.lktMode.R;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.activity.StartActivity;
import com.kai.lktMode.adapter.AboutAdapter;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.permission.FloatWindowManager;
import com.kai.lktMode.tool.util.net.AlipayUtil;
import com.kai.lktMode.tool.util.net.WebUtil;
import com.kai.lktMode.widget.SimplePaddingDecoration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class PreviousSettingFragment extends MyFragment {
    private List<Item> data=new ArrayList<>();
    AboutAdapter aboutAdapter;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_setting,null,false);
        return view;
    }

    @Override
    public boolean isPassed() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        init();
        recyclerView.addItemDecoration(new SimplePaddingDecoration(getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        aboutAdapter=new AboutAdapter(getContext(),data);
        recyclerView.setAdapter(aboutAdapter);
        aboutAdapter.setOnItemClick(new AboutAdapter.OnItemClick() {
            @Override
            public void onClick(int i) {
                switch (i){
                    case 0:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            if (Settings.canDrawOverlays(getContext())) {
                                //Toast.makeText(getContext(),"已授权",Toast.LENGTH_SHORT).show();
                            } else {
                                //若没有权限，提示获取.
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getActivity().getPackageName()));
                                Toast.makeText(getContext(),"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
                                startActivityForResult(intent,13);
                            }

                        }else {
                            FloatWindowManager.getInstance().check(getContext());
                        }
                        break;
                    case 1:
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 17);
                        }
                        break;
                    case 2:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PowerManager powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);

                            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(getActivity().getPackageName());
                            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
                            if (!hasIgnored) {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                startActivityForResult(intent, 12);
                            }
                        }
                        break;
                }
            }
        });
        Refresh();
    }
    public boolean isPowersaveClosed() {
        boolean isIgnored=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);

            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName());
            isIgnored=hasIgnored;
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。

        }else {
            return true;
        }
        return isIgnored;
    }

    public boolean isSuspendEnable(){
        boolean isGranted=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Settings.canDrawOverlays(getContext())) {
                isGranted=true;
                //Toast.makeText(context,"已授权",Toast.LENGTH_SHORT).show();
            }

        }else {
            isGranted= FloatWindowManager.getInstance().check(getContext());
        }
        return isGranted;
    }

    private void init(){
        data.clear();
        data.add(0,new Item("悬浮窗口权限","未授权"));
        data.add(1,new Item("储存读写权限(必选)","未授权"));
        data.add(2,new Item("后台自启权限","未授权"));
    }

    @Override
    public void Refresh() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                Boolean suspend=isSuspendEnable();
                data.get(0).setChecked(suspend);
                data.get(0).setSubtitle(suspend?"已授权":"未授权");
                Boolean readable= ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                data.get(1).setChecked(readable);
                data.get(1).setSubtitle(readable?"已授权":"未授权");
                Boolean powersaved= isPowersaveClosed();
                data.get(2).setChecked(powersaved);
                data.get(2).setSubtitle(powersaved?"已授权":"未授权");
                emitter.onComplete();
            }
        }).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                aboutAdapter.notifyDataSetChanged();
            }
        });

    }
}
