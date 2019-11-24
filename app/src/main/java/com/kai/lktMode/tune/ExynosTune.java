package com.kai.lktMode.tune;

import android.content.Context;

import com.kai.lktMode.bean.Utils;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.cpu.Stune;
import com.kai.lktMode.selinux.SElinux;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExynosTune extends Tune{
    JSONObject topApp;
    public ExynosTune(Context context,int mode){
        super(context,mode);
        try {
            tune=new JSONArray(Utils.readAssetFile(context, "eas-tune/exynos.json"));
            object=tune.getJSONObject(mode);
            governor=object.getJSONObject("governor");
            freqs=object.getJSONObject("freqs");
            topApp=object.getJSONObject("top-app");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public String getCommand(boolean isAddition) {
        StringBuilder builder=new StringBuilder();
        try {
            Stune.StuneBean stuneBean=new Stune().new StuneBean("top-app");
            builder.append(stuneBean.setPrefer(topApp.getBoolean("prefer")));
            builder.append(stuneBean.setBoost(topApp.getString("boost")));
            for (int group=0;group<cpuManager.getCpuGroup().length;group++){
                CpuManager.Kernel[] kernels=cpuManager.getCpuGroup()[group];
                for (CpuManager.Kernel kernel:kernels){
                    //设置小核心参数
                    if (group==0){
                        builder.append(kernel.terminalScaling_governor(governor.getString("small")));
                        builder.append(kernel.tuneMaxFreq(freqs.getDouble("small-max"),isAddition));
                        builder.append(kernel.tuneMinFreq(freqs.getDouble("small-min"),isAddition));
                        //builder.append(stuneBean.setPrefer(true));
                    }else if (group==1){
                        builder.append(kernel.terminalScaling_governor(governor.getString("large")));
                        builder.append(kernel.tuneMaxFreq(freqs.getDouble("large-max"),isAddition));
                        builder.append(kernel.tuneMinFreq(freqs.getDouble("large-min"),isAddition));
                    }else if (group==2){
                        builder.append(kernel.terminalScaling_governor(governor.getString("huge")));
                        builder.append(kernel.tuneMaxFreq(freqs.getDouble("huge-max"),isAddition));
                        builder.append(kernel.tuneMinFreq(freqs.getDouble("huge-min"),isAddition));
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
            JSONArray tune = new JSONArray(Utils.readAssetFile(context, "eas-tune/exynos.json"));
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
