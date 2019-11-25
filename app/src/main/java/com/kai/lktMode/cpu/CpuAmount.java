package com.kai.lktMode.cpu;

import android.content.Context;

import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.util.local.CpuUtil;

public class CpuAmount {
    private int amount=0;
    private static CpuAmount cpuAmount;
    public static synchronized CpuAmount getInstance(Context context){
        if (cpuAmount==null){
            cpuAmount=new CpuAmount(context);
        }
        return cpuAmount;
    }
    private CpuAmount(Context context){
        amount=Preference.getInt(context,"cpuAmount");
        if (amount==0){
            amount= CpuUtil.getCpuAmount();
            Preference.saveInt(context,"cpuAmount",amount);
        }
    }

    public int getAmount() {
        return amount;
    }
}
