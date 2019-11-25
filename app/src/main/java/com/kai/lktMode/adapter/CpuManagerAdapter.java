package com.kai.lktMode.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kai.lktMode.R;
import com.kai.lktMode.cpu.CpuBoost;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.cpu.Group;
import com.kai.lktMode.bean.ParentItem;
import com.kai.lktMode.cpu.Stune;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.gpu.GPU;
import com.kai.lktMode.gpu.GPUFreq;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.selinux.SElinux;
import com.kai.lktMode.service.CommandService;
import com.kai.lktMode.tool.Mode;
import com.kai.lktMode.tool.ToastUtil;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.kai.lktMode.widget.AnimatedExpandableListView;
import com.kai.lktMode.widget.ParamDialog;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class CpuManagerAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
    private Context context;
    private OnFinishListener onFinishListener=null;
    private LayoutInflater mInflater;
    private List<ParentItem> parentItems=new ArrayList<>();
    private int[] freqs=new int[CpuManager.getInstance().getCounts()];
    public CpuManagerAdapter(Context context,List<ParentItem> parentItems){
        this.context=context;
        this.parentItems=parentItems;
        this.mInflater=LayoutInflater.from(context);
    }

    @Override
    public Object getChild(int i, int i1) {
        ParentItem item=(ParentItem)getGroup(i);
        return item.getObject();
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    public void setFreqs(int[] freqs) {
        this.freqs = freqs;
        notifyDataSetChanged();
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public String getCommand(){
        CpuManager manager=CpuManager.getInstance();
        StringBuilder builder=new StringBuilder("");
        CpuBoost cpuBoost=new CpuBoost();
        boolean isAddition=CpuBoost.isAddition();
        if (CpuBoost.isSupport()){
            HashMap<String,String> map=CpuBoost.getBoostFreqences(cpuBoost.getFreq());
            builder.append(CpuBoost.setMs(cpuBoost.getMs()));
            builder.append("chmod 666 "+SystemInfo.SystemModule$cpuboost+"/input_boost_freq\n");
            for (String key:map.keySet()){
                builder.append("echo '"+key+":"+map.get(key)+"'>"+SystemInfo.SystemModule$cpuboost+"/input_boost_freq\n");
            }
            builder.append("chmod 444 "+SystemInfo.SystemModule$cpuboost+"/input_boost_freq\n");
        }
        for (int i=0;i<manager.getCounts();i++){
            //保存当前所有核心的最高最低频率
            CpuManager.Kernel kernel=manager.getKernel(i);
            //保存当前所有核心的在线/离线状态
            if (kernel.isOnline()){
                builder.append(kernel.terminalOnline(kernel.isOnline()));
            }
            else
                continue;
            builder.append(kernel.terminalScaling_max_freq(kernel.getScaling_max_freq()+"",isAddition));
            builder.append(kernel.terminalScaling_min_freq(kernel.getScaling_min_freq()+"",isAddition));
            //保存当前使用的调速器
            builder.append(kernel.terminalScaling_governor(kernel.getScaling_governor()));
            CpuManager.Governor governor=kernel.getGovernor(kernel.getScaling_governor());
            for (String key:governor.keySet){
                builder.append(governor.setValue(key,governor.getValue(key)));
            }



        }
        GPUFreq gpuManager=GPUFreq.getInstance();
        builder.append(gpuManager.setGovernor(gpuManager.getGovernor()));
        builder.append(gpuManager.setMaxFreq(gpuManager.getMaxFreqOri()));
        builder.append(gpuManager.setMinFreq(gpuManager.getMinFreqOri()));
        return builder.toString();
    }

    private class TitleHolderView {
        TextView titleView;
        ImageView indicator;
        RelativeLayout layout;
    }
    private class StuneHolderView{
        TextView title;
        TextView boost;
        Switch prefer;
    }
    private class FreqHolderView{
        TextView title;
        TextView freq;
        Switch aSwitch;
    }
    private class CommonHolderView{
        TextView title;
        TextView subtitle;
        TextView freq;
    }


    @Override
    public int getRealChildrenCount(int groupPosition) {
        ParentItem parentItem=(ParentItem)getGroup(groupPosition);
        int type=parentItem.getType();
        if (type==SystemInfo.type_stune){
            Stune stune=(Stune)parentItem.getObject();
            return stune.getSize()+1;
        }
        if (type==SystemInfo.type_freq){
            Group group=(Group)parentItem.getObject();
            return group.getLength();
        }
        if (type==SystemInfo.type_limit){
            return 2;
        }
        if (type==SystemInfo.type_param){
            return 2;
        }
        if (type==SystemInfo.type_gpu){
            return 3;
        }
        if (type==SystemInfo.type_boost&&groupPosition/4<1){
            return 2;
        }
        if (type==SystemInfo.type_boost&&groupPosition/4==1){
            return 1;
        }
        return 0;
    }

    @Override
    public int getRealChildType(int groupPosition, int childPosition) {
        ParentItem parentItem=(ParentItem)getGroup(groupPosition);
        return parentItem.getType();
    }

    @Override
    public int getRealChildTypeCount() {
        return 6;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        TitleHolderView titleHolderView;
        ParentItem parentItem=(ParentItem)getGroup(i);
        if (view==null){
            titleHolderView=new TitleHolderView();
            view=mInflater.inflate(R.layout.list_title, viewGroup, false);
            titleHolderView.titleView=view.findViewById(R.id.list_title);
            titleHolderView.indicator=view.findViewById(R.id.indicator);
            titleHolderView.layout=view.findViewById(R.id.layout);
            view.setTag(titleHolderView);
        }else {
            titleHolderView = (TitleHolderView) view.getTag();
        }
        titleHolderView.titleView.setText(parentItem.getTitle());
        if (b){
            titleHolderView.indicator.setImageResource(R.drawable.indicator_up);
            titleHolderView.layout.setBackgroundResource(R.drawable.item_title_press);
        }else {
            titleHolderView.indicator.setImageResource(R.drawable.indicator_down);
            titleHolderView.layout.setBackgroundResource(R.drawable.item_title_normal);
        }
        return view;
    }


    @Override
    public View getRealChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        int type=parentItems.get(i).getType();
        if (type==SystemInfo.type_freq){
            Group group=(Group) parentItems.get(i).getObject();
            int[] counts=group.getCounts();
            FreqHolderView holderView;
            if (view==null){
                holderView=new FreqHolderView();
                view=mInflater.inflate(R.layout.list_cpu_kernel,viewGroup,false);
                holderView.title=view.findViewById(R.id.title);
                holderView.freq=view.findViewById(R.id.freq);
                holderView.aSwitch=view.findViewById(R.id.swicth);
                view.setTag(holderView);
            }else {
                holderView=(FreqHolderView)view.getTag();
            }
            holderView.title.setText(String.valueOf(counts[i1]));
            holderView.aSwitch.setOnCheckedChangeListener(null);
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                    Boolean online=group.getKernels()[i1].isOnline();
                    emitter.onNext(online);
                    emitter.onComplete();
                }
            }).subscribe(new Observer<Object>() {
                @Override
                public void onNext(Object o) {
                    holderView.aSwitch.setChecked((Boolean)o);
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    holderView.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (i1==0){
                                ToastUtil.shortAlert(context,"主核心无法离线");
                                return;
                            }
                            CpuManager manager=CpuManager.getInstance();
                            StringBuilder builder=new StringBuilder();
                            builder.append(manager.getKernel(counts[i1]).terminalOnline(b));
                            command(builder.toString());
                        }
                    });

                }

                @Override
                public void onSubscribe(Disposable d) {

                }
            });



            if (freqs[counts[i1]]==0){
                holderView.freq.setText("离线");
                return view;
            }

            holderView.freq.setText(freqs[counts[i1]]+"MHz");
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;
        }
        if (type==SystemInfo.type_limit){
            ParentItem parentItem=(ParentItem)getGroup(i);
            Group group=(Group) parentItem.getObject();
            CommonHolderView holderView;
            holderView=new CommonHolderView();
            view=mInflater.inflate(R.layout.list_limit,viewGroup,false);
            holderView.title=view.findViewById(R.id.title);
            holderView.subtitle=view.findViewById(R.id.subtitle);
            holderView.freq=view.findViewById(R.id.freq);


            if (i1==0){
                holderView.title.setText("cpu 最高频率");
                holderView.subtitle.setText("限制CPU能达到的最高频率");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                        String max=group.getMaxFreqStr();
                        emitter.onNext(max);
                        emitter.onComplete();
                    }
                }).subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        holderView.freq.setText((String)o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });



                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setTitle("选择频率")
                                .setItems(group.getFreqsStr(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        StringBuilder builder=new StringBuilder();
                                        for (CpuManager.Kernel kernel:group.getKernels()){
                                            builder.append(kernel.terminalScaling_max_freq(group.getFreqs()[index]+"",true));
                                        }

                                        //holderView.freq.setText(group.getFreqsStr()[index]+"");
                                        command(builder.toString());
                                    }
                                })
                                .show();
                    }
                });

            }
            if (i1==1){
                holderView.title.setText("cpu 最低频率");
                holderView.subtitle.setText("限制CPU能达到的最低频率");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                        String min=group.getMinFreqStr();
                        emitter.onNext(min);
                        emitter.onComplete();
                    }
                }).subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        holderView.freq.setText((String)o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });

                //holderView.freq.setText(group.getMinFreqStr());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setTitle("选择频率")
                                .setItems(group.getFreqsStr(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        StringBuilder builder=new StringBuilder();
                                        for (CpuManager.Kernel kernel:group.getKernels()){
                                            builder.append(kernel.terminalScaling_min_freq(group.getFreqs()[index]+"",true));
                                        }
                                        //Log.d("sss",builder.toString());
                                        //holderView.freq.setText(group.getFreqsStr()[index]+"");
                                        command(builder.toString());
                                    }
                                })
                                .show();
                    }
                });

            }
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;

        }
        if (type==SystemInfo.type_param){
            ParentItem parentItem=(ParentItem)getGroup(i);
            Group group=(Group) parentItem.getObject();
            CommonHolderView holderView;
            if (view==null){
                holderView=new CommonHolderView();
                view=mInflater.inflate(R.layout.list_limit,viewGroup,false);
                holderView.title=view.findViewById(R.id.title);
                holderView.subtitle=view.findViewById(R.id.subtitle);
                holderView.freq=view.findViewById(R.id.freq);
                view.setTag(holderView);
            }else {
                holderView=(CommonHolderView)view.getTag();
            }

            if (i1==0){
                holderView.title.setText("cpu工作模式");
                holderView.subtitle.setText("决定cpu功耗和速率的调速器");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                        String governor=group.getGovernor();
                        emitter.onNext(governor);
                        emitter.onComplete();
                    }
                }).subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        holderView.freq.setText((String)o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
                view.setOnClickListener(null);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setItems(group.getGovernors(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        holderView.freq.setText(group.getGovernors()[index]);
                                        StringBuilder builder=new StringBuilder();
                                        for (CpuManager.Kernel kernel:group.getKernels()){
                                            builder.append(kernel.terminalScaling_governor(group.getGovernors()[index]));
                                        }
                                        group.setGovernor(group.getGovernors()[index]);
                                        command(builder.toString());
                                    }
                                }).show();
                    }
                });
            }
            if (i1==1){
                holderView.title.setText("参数微调");
                holderView.subtitle.setText("通过对参数的调整达到最佳的效果");
                holderView.freq.setText("");
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(context,kernel.getScaling_governor(),Toast.LENGTH_SHORT).show();
                        new ParamDialog(context,group.getGovernor().trim(),group.getMainKernel().getGovernor(group.getMainKernel().getScaling_governor().trim())).show();
                    }
                });
            }
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;
        }
        if (type==SystemInfo.type_gpu){
            ParentItem parentItem=(ParentItem)getGroup(i);
            GPUFreq gpuManager=(GPUFreq)parentItem.getObject();
            CommonHolderView holderView;
            if (view==null){
                holderView=new CommonHolderView();
                view=mInflater.inflate(R.layout.list_limit,viewGroup,false);
                holderView.title=view.findViewById(R.id.title);
                holderView.subtitle=view.findViewById(R.id.subtitle);
                holderView.freq=view.findViewById(R.id.freq);
                view.setTag(holderView);
            }else {
                holderView=(CommonHolderView)view.getTag();
            }
            if (i1==0){
                holderView.title.setText("gpu工作模式");
                holderView.subtitle.setText("设置gpu调速的工作模式");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                        String governor=gpuManager.getGovernor();
                        emitter.onNext(governor);
                        emitter.onComplete();
                    }
                }).subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        holderView.freq.setText((String)o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });



                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] governors=gpuManager.getAvailableGovernors().toArray(new String[]{});
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setTitle("选择调速器")
                                .setItems(governors, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        command(gpuManager.setGovernor(governors[index]));
                                        holderView.freq.setText(governors[index]);
                                    }
                                })
                                .show();
                    }
                });
            }
            if (i1==1){
                holderView.title.setText("gpu最高频率");
                holderView.subtitle.setText("限制gpu能达到的最高频率");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                        String max=gpuManager.getMaxFreq()+"MHz";
                        emitter.onNext(max);
                        emitter.onComplete();
                    }
                }).subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        holderView.freq.setText((String)o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });



                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<Integer> freqs=gpuManager.getAvailableFreqs();
                        String[] freqStrs=gpuManager.getAdjustedFreqs().toArray(new String[]{});
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setTitle("选择频率")
                                .setItems(freqStrs, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        command(gpuManager.setMaxFreq(freqs.get(index)));
                                        holderView.freq.setText(freqStrs[index]);
                                    }
                                })
                                .show();
                    }
                });
            }
            if (i1==2){
                holderView.title.setText("gpu最低频率");
                holderView.subtitle.setText("限制GPU能达到的最低频率");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                        String min=gpuManager.getMinFreq()+"MHz";
                        emitter.onNext(min);
                        emitter.onComplete();
                    }
                }).subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        holderView.freq.setText((String)o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });


               // holderView.freq.setText(gpuManager.getMinFreq()+"MHz");
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<Integer> freqs=gpuManager.getAvailableFreqs();
                        String[] freqStrs=gpuManager.getAdjustedFreqs().toArray(new String[]{});
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setTitle("选择频率")
                                .setItems(freqStrs, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        command(gpuManager.setMinFreq(freqs.get(index)));
                                        holderView.freq.setText(freqStrs[index]);
                                    }
                                })
                                .show();
                    }
                });

            }
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;
        }
        if (type==SystemInfo.type_boost){
            ParentItem parentItem=(ParentItem)getGroup(i);
            Group group=(Group) parentItem.getObject();
            CpuBoost cpuBoost=group.getCpuBoost();
            CpuManager.Kernel kernel=group.getMainKernel();
            CommonHolderView holderView;
            if (view==null){
                holderView=new CommonHolderView();
                view=mInflater.inflate(R.layout.list_limit,viewGroup,false);
                holderView.title=view.findViewById(R.id.title);
                holderView.subtitle=view.findViewById(R.id.subtitle);
                holderView.freq=view.findViewById(R.id.freq);
                view.setTag(holderView);
            }else {
                holderView=(CommonHolderView)view.getTag();
            }

            if (i1==0){
                holderView.title.setText("升频增益");
                holderView.subtitle.setText("检测到负荷增益的频率");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                        String freq=cpuBoost.getFreq(kernel.getKernelCount())/1000+"MHz";
                        emitter.onNext(freq);
                        emitter.onComplete();
                    }
                }).subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        holderView.freq.setText((String)o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int[] freqs= ShellUtil.concat(new int[]{0},kernel.getAvailable_ferqs());
                        String[] freqs_show=new String[freqs.length];
                        for (int index=0;index<freqs.length;index++){
                            freqs_show[index]=freqs[index]/1000+"MHz";
                        }
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setTitle("选择频率")
                                .setItems(freqs_show, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        holderView.freq.setText(freqs_show[i]);
                                        command(cpuBoost.setFreq(kernel.getCount()+"",freqs[index]+""));
                                        cpuBoost.setFreqMap(kernel.getKernelCount()+"",freqs[index]+"");
                                    }
                                })
                                .show();
                    }
                });
            }
            if (i1==1){
                holderView.title.setText("升频延迟（所有核心）");
                holderView.subtitle.setText("升频延迟的时间(毫秒)");
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                        String result=cpuBoost.getMs().trim();
                        emitter.onNext(result);
                        emitter.onComplete();
                    }
                }).subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        holderView.freq.setText((String)o);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String freq=holderView.freq.getText().toString();
                        View view1=View.inflate(context,R.layout.dialog_edittext,null);
                        EditText e=view1.findViewById(R.id.edit);
                        e.setText(cpuBoost.getMs());
                        AlertDialog dialog=new AlertDialog.Builder(context,R.style.AppDialog)
                                .setView(view1)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        holderView.freq.setText(e.getText().toString()+"ms");
                                        command(cpuBoost.setMs(e.getText().toString()));
                                        //Toast.makeText(context,builder.toString(),Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {

                                    }
                                })
                                .setTitle("设置延迟")
                                .show();

                    }
                });
            }
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;
        }
        if (type==SystemInfo.type_stune){
            if (i1==0){
                view=mInflater.inflate(R.layout.list_stune_title,viewGroup,false);
            }else {
                StuneHolderView holderView;
                Stune stune=(Stune)parentItems.get(i).getObject();
                holderView=new StuneHolderView();
                view=mInflater.inflate(R.layout.list_stune,viewGroup,false);
                holderView.title=view.findViewById(R.id.title);
                holderView.boost=view.findViewById(R.id.boost);
                holderView.prefer=view.findViewById(R.id.prefer);
                List<Stune.StuneBean> stuneBeans=stune.getStuneBeans();
                Stune.StuneBean bean =stuneBeans.get(i1-1);
                holderView.title.setText(bean.getName());
                holderView.boost.setText(String.valueOf(bean.getBoost()));
                holderView.prefer.setOnCheckedChangeListener(null);
                holderView.prefer.setChecked(bean.isPrefer());
                holderView.prefer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        StringBuilder builder=new StringBuilder();
                        builder.append(bean.setPrefer(b));
                        command(builder.toString());
                    }
                });
                holderView.boost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View view1=View.inflate(context,R.layout.dialog_edittext,null);
                        EditText e=view1.findViewById(R.id.edit);
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setView(view1)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        StringBuilder builder=new StringBuilder();
                                        builder.append(bean.setBoost(e.getText().toString()));
                                        command(builder.toString());
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {

                                    }
                                })
                                .show();
                        e.setText(String.valueOf(bean.getBoost()));
                    }
                });

            }
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;

        }

        return view;
    }
    private void command(String args){
        try {
            RootUtils.runCommand(args, new RootUtils.onCommandComplete() {
                @Override
                public void onComplete() {
                    ToastUtil.shortShow(context,"完成");
                }

                @Override
                public void onCrash(String error) {

                }

                @Override
                public void onOutput(String result) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Object getGroup(int i) {
        return parentItems.get(i);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }
    public interface OnFinishListener{
        void onFinish();
    }
    @Override
    public int getGroupCount() {
        return parentItems.size();
    }

}
