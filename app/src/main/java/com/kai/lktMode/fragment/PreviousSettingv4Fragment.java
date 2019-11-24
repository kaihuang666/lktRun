package com.kai.lktMode.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kai.lktMode.R;
import com.kai.lktMode.activity.PreviousActivity;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.cpu.CpuModel;
import com.kai.lktMode.tool.util.local.AppUtils;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.kai.lktMode.widget.DownloadDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviousSettingv4Fragment extends MyFragment {
    @BindView(R.id.title)
    TextView title;
    Boolean pass=true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_settingv4,null,false);
        return view;
    }

    @Override
    public boolean isPassed() {
        return pass;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
    }


    private void init(){

    }


}
