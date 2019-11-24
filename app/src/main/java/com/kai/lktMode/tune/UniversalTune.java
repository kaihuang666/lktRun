package com.kai.lktMode.tune;

import android.content.Context;

import com.kai.lktMode.bean.Utils;
import com.kai.lktMode.cpu.CpuBoost;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.selinux.SElinux;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UniversalTune extends Tune{
    private JSONObject cpuboost;
    boolean boost=false;
    public UniversalTune(Context context, int mode){
        super(context,mode);
        try {
            tune=new JSONArray(Utils.readAssetFile(context, "eas-tune/universal.json"));
            object=tune.getJSONObject(mode);
            governor=object.getJSONObject("governor");
            freqs=object.getJSONObject("freqs");
            cpuboost=object.getJSONObject("cpuboost");
            boost= CpuBoost.isSupport();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public String getCommand(boolean isAddition) {
        StringBuilder builder=new StringBuilder();
        try {
            //设置boost延迟
            if (boost){
                builder.append(CpuBoost.setMs(cpuboost.getString("ms")));
            }
            for (int group=0;group<cpuManager.getCpuGroup().length;group++){
                CpuManager.Kernel[] kernels=cpuManager.getCpuGroup()[group];
                for (CpuManager.Kernel kernel:kernels){
                    //设置小核心参数
                    if (group==0){
                        builder.append(kernel.terminalScaling_governor(governor.getString("small")));
                        builder.append(kernel.tuneMaxFreq(freqs.getDouble("small-max"),isAddition));
                        builder.append(kernel.tuneMinFreq(freqs.getDouble("small-min"),isAddition));
                        //设置调速器参数
                        JSONObject param=object.getJSONObject("kernel-small");
                        Iterator<String> keys = param.keys();
                        CpuManager.Governor governorS=kernel.getGovernor(governor.getString("small"));
                        while (keys.hasNext()){
                            String key=keys.next();
                            builder.append(governorS.setValue(key,param.getString(key)));
                        }
                        if (boost)
                            builder.append(CpuBoost.setFreq(kernel.getCount(),cpuboost.getString("small")));
                    }
                    //设置大核心参数
                    else if (group==1){
                        builder.append(kernel.terminalScaling_governor(governor.getString("large")));
                        builder.append(kernel.tuneMaxFreq(freqs.getDouble("large-max"),isAddition));
                        builder.append(kernel.tuneMinFreq(freqs.getDouble("large-min"),isAddition));
                        //设置调速器参数
                        JSONObject param=object.getJSONObject("kernel-large");
                        Iterator<String> keys = param.keys();
                        CpuManager.Governor governorL=kernel.getGovernor(governor.getString("large"));
                        while (keys.hasNext()){
                            String key=keys.next();
                            builder.append(governorL.setValue(key,param.getString(key)));
                        }
                        if (boost)
                            builder.append(CpuBoost.setFreq(kernel.getCount(),cpuboost.getString("large")));
                    }
                    //设置超大核心参数
                    else if (group==2){
                        builder.append(kernel.terminalScaling_governor(governor.getString("huge")));
                        builder.append(kernel.tuneMaxFreq(freqs.getDouble("huge-max"),isAddition));
                        builder.append(kernel.tuneMinFreq(freqs.getDouble("huge-min"),isAddition));
                        //设置调速器参数
                        JSONObject param=object.getJSONObject("kernel-huge");
                        Iterator<String> keys = param.keys();
                        CpuManager.Governor governorH=kernel.getGovernor(governor.getString("huge"));
                        while (keys.hasNext()){
                            String key=keys.next();
                            builder.append(governorH.setValue(key,param.getString(key)));
                        }
                        if (boost)
                            builder.append(CpuBoost.setFreq(kernel.getCount(),cpuboost.getString("huge")));
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return builder.toString();
        }
    }
    public static List<String> getModeNames(Context context){
        List<String> modes=new ArrayList<>();
        try {
            JSONArray tune = new JSONArray(Utils.readAssetFile(context, "eas-tune/universal.json"));
            for (int i=0;i<tune.length();i++){
                JSONObject object=tune.getJSONObject(i);
                modes.add(object.getString("name"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return modes;
        }
    }
}
