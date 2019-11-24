package com.kai.lktMode.tune;

import android.content.Context;

import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.cpu.CpuModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Tune {
    JSONArray tune;
    JSONObject object;
    Context context;
    CpuManager cpuManager;
    JSONObject freqs;
    JSONObject governor;
    public Tune(Context context,int mode){
        this.context=context;
        cpuManager=new CpuManager();
    }
    public String getCommand(boolean isAddition){
        return "";
    }
    public static int[] getindexs(Context context){
        CpuModel model=CpuModel.getInstance(context);
        String vendor=model.getVendor();
        if (vendor.equals("exynos")){
            return new int[]{1,2,4,6};
        }else {
            return new int[]{1,3,6,7};
        }
    }
}
