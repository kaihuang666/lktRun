package com.kai.lktMode.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.kai.lktMode.R;
import com.kai.lktMode.adapter.AboutAdapter;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.bean.Utils;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.cpu.CpuModel;
import com.kai.lktMode.permission.FloatWindowManager;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.util.local.CpuUtil;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.kai.lktMode.widget.SimplePaddingDecoration;
import com.stericson.RootShell.execution.Shell;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class PreviousSettingv2Fragment extends MyFragment {
    private List<Item> data=new ArrayList<>();
    private static String[] options1Items=new String[]{"骁龙","猎户座","麒麟","联发科"};
    private static String[] vendors=new String[]{"qualcomm","exynos","kirin","mt"};
    private List<List<String>> options2Items=new ArrayList<>();
    private List<List<String>> models=new ArrayList<>();
    AboutAdapter aboutAdapter;
    private CpuModel cpuModel;
    private CpuManager cpuManager;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cpuModel= CpuModel.getInstance(getContext());
        cpuModel.initialize();
        cpuManager=CpuManager.getInstance();
        try {
            for (String vendor:vendors){
                ArrayList<String> options2Item = new ArrayList<>();
                ArrayList<String> model = new ArrayList<>();
                JSONArray objects = new JSONArray(Utils.readAssetFile(getContext(), vendor+".json"));
                for (int i=0;i<objects.length();i++){
                    JSONObject object=objects.getJSONObject(i);
                    options2Item.add(object.getString("model_name"));
                    model.add(object.getString("model"));
                }
                options2Items.add(options2Item);
                models.add(model);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void pickPowercfg(){
        OptionsPickerView pvOptions = new OptionsPickerBuilder(getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                if (data.size()>0){
                    data.get(0).setSubtitle(options1Items[options1]+options2Items.get(options1).get(options2));
                    Preference.saveString(getContext(),"vendor",vendors[options1]);
                    Preference.saveString(getContext(),"vendor_name",options1Items[options1]);
                    Preference.saveString(getContext(),"model_name",options2Items.get(options1).get(options2));
                    Preference.saveString(getContext(),"model",models.get(options1).get(options2));
                    aboutAdapter.notifyItemChanged(0);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_setting,null,false);
        return view;
    }

    @Override
    public boolean isPassed() {
        return !((String)Preference.getString(getContext(),"model")).isEmpty();
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
                if (i==0){
                    pickPowercfg();
                }
            }
        });
        Refresh();
    }


    private void init(){
        data.clear();
        data.add(0,new Item("处理器型号(必选)","点击选择型号"));
        data.add(1,new Item("内核类型","HMP调速器内核"));
        data.add(2,new Item("核心数目","0"));
        if (ShellUtil.isMagiskInstall())
            data.add(3,new Item("Magisk版本",""));
    }

    @Override
    public void Refresh() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                if (!cpuModel.getVendor().equals("unknown")&&!cpuModel.getModel().equals("unknown")){
                    data.get(0).setSubtitle(cpuModel.getVendor_name()+cpuModel.getModel_name());
                }

                data.get(1).setSubtitle(!cpuManager.isEasKernel()?"HMP调速器内核":"EAS调速器内核");
                data.get(2).setSubtitle(String.valueOf(cpuManager.getCounts()));
                if (ShellUtil.isMagiskInstall())
                    data.get(3).setSubtitle(ShellUtil.getMagiskVersion());
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
