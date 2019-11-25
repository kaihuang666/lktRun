package com.kai.lktMode.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.kai.lktMode.R;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.activity.PreviousActivity;
import com.kai.lktMode.adapter.AboutAdapter;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.bean.Utils;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.cpu.CpuModel;
import com.kai.lktMode.tool.LKTCommand;
import com.kai.lktMode.tool.Mode;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.util.local.AppUtils;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.kai.lktMode.widget.DownloadDialog;
import com.kai.lktMode.widget.SimplePaddingDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class PreviousSettingv3Fragment extends MyFragment {
    List<String> tune_type=new ArrayList<>();
    List<String> tune_type_true=new ArrayList<>();
    boolean isEas=CpuManager.getInstance().isEasKernel();
    int defealt=-1;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tune_type=new ArrayList<>(Arrays.asList(CpuModel.getTuneTypes(getContext())));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_settingv3,null,false);
        return view;
    }

    @Override
    public boolean isPassed() {
        return defealt!=-1;
    }
    private void downloadPowercfg(final String url,boolean add) throws Exception{
        final String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/";
        File dir=new File(sdcard);
        if (!dir.exists())
            dir.mkdirs();
        DownloadDialog downloadDialog=new DownloadDialog.Builder(getActivity())
                .setParentDic(sdcard)
                .setProgressEnable(false)
                .setDownloadUrl(url)
                .setFileName("powercfg.sh")
                .build();
        downloadDialog.setOnTaskSuccess(new DownloadDialog.OnTaskSuccess() {
            @Override
            public void onSuccess() {
                backupOffical();
                if (add) {
                    Toast.makeText(getContext(), "暂未获得yc调度(EAS专版)授权，仅导入了通用切换脚本，请参照网页教程自行刷入面具模块", Toast.LENGTH_LONG).show();
                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.VIEW");
                    Uri content_url1 = Uri.parse("https://www.jianshu.com/p/c5399a7e9bb3");//此处填链接
                    intent2.setData(content_url1);
                    startActivity(intent2);
                }
                Preference.saveBoolean(getActivity(),"custom",true);
                downloadDialog.dismiss();
                Preference.saveString(getContext(),"code1","sh "+sdcard+"powercfg.sh powersave");
                Preference.saveString(getContext(),"code2","sh "+sdcard+"powercfg.sh balance");
                Preference.saveString(getContext(),"code3","sh "+sdcard+"powercfg.sh performance");
                Preference.saveString(getContext(),"code4","sh "+sdcard+"powercfg.sh fast");
            }
        });
        downloadDialog.show();
    }

    @Override
    public void next(){
        try {
            switch (tune_type_true.get(defealt)){
                case "ycv2":
                    Preference.saveBoolean(getActivity(),"custom",true);
                    backupOffical();
                    Toast.makeText(getActivity(),"暂未获得yc调度(新版)授权，请参照网页教程自行导入",Toast.LENGTH_LONG).show();
                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.VIEW");
                    Uri content_url1 = Uri.parse("https://www.jianshu.com/p/c5399a7e9bb3");//此处填链接
                    intent2.setData(content_url1);
                    startActivity(intent2);
                    //pass=true;
                    break;
                case "yc":
                    try {
                        downloadPowercfg("https://www.lanzous.com/tp/"+CpuModel.getYcUrl(getContext()),false);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case "855tune":
                    Preference.saveBoolean(getContext(),"custom",true);
                    try {
                        downloadPowercfg("https://www.lanzous.com/tp/i66muxc",true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case "lkt":
                    Preference.saveBoolean(getContext(),"custom",false);
                    installLKT();
                    break;
                case "diy":
                    Preference.saveBoolean(getContext(),"custom",true);
                    backupOffical();
                    break;
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void backupOffical(){
        ProgressDialog dialog=new ProgressDialog(getContext(),R.style.AppDialog);
        dialog.setCancelable(false);
        dialog.setMessage("正在备份处理器信息");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Preference.saveString(getContext(),"offical",CpuManager.getInstance().backup());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        //Toast.makeText(getContext(),"备份完成",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
        dialog.show();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        init();
    }

    private void installLKT(){
        if (LKTCommand.isLKTInstalled())
            return;
        final String sdcard= SystemInfo.appPath;
        DownloadDialog downloadDialog=new DownloadDialog.Builder(getActivity())
                .setDownloadUrl("https://www.lanzous.com/tp/i4ltpla")
                .setFileName("lkt_magisk.zip")
                .setProgressEnable(true)
                .setParentDic(sdcard)
                .build();
        downloadDialog.setOnTaskSuccess(new DownloadDialog.OnTaskSuccess() {
            @Override
            public void onSuccess() {
                downloadDialog.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShellUtil.installWithMagisk(getActivity(),sdcard+"/lkt_magisk.zip");
                    }
                });

            }
        });
        downloadDialog.setOnTaskFail(new DownloadDialog.OnTaskFail() {
            @Override
            public void onFail() {
                downloadDialog.dismiss();
                Toast.makeText(getContext(),"下载失败",Toast.LENGTH_SHORT).show();
            }
        });
        downloadDialog.show();
    }

    private void init(){
        //eas内核都应该支持LKT
        tune_type_true=CpuModel.getLastestTypes(getContext());
        for (String type:CpuModel.getItem(getContext())){
            RadioButton tempButton=buildRadioButton();
            tempButton.setText(type);
            radioGroup.addView(tempButton,LinearLayout.LayoutParams.MATCH_PARENT, AppUtils.Dp2Px(getContext(),50));
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("i",i+"");
                defealt=i-1;
            }
        });
    }
    private RadioButton buildRadioButton(){
        RadioButton tempButton = new RadioButton(getContext());
        tempButton.setPaddingRelative(80,0,0,0);
        return tempButton;
    }

}
