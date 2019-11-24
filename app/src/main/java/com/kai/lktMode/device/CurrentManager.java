package com.kai.lktMode.device;

import android.content.Context;
import android.os.Build;

import com.kai.lktMode.cpu.CpuModel;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.Preference;

public class CurrentManager {
    private Context context;
    private CpuModel cpuModel;
    private String current_path=qm_current_path;
    private static String qm_current_path="/sys/class/power_supply/battery/current_now";
    private static String status="/sys/class/power_supply/battery/status";
    private static String kirin_status="/sys/class/power_supply/Battery/status";
    private static String exynos_current_path="/sys/class/power_supply/battery/current_now";
    private static String mt_current_path="/sys/class/power_supply/battery/current_now";
    private static String kirin_path="/sys/class/power_supply/Battery/current_now";
    public static CurrentManager getInstance(Context context){
        return new CurrentManager(context);
    }
    private CurrentManager(Context context){
        this.context=context;
        cpuModel=CpuModel.getInstance(context);
        switch (cpuModel.getVendor()){
            case "qualcomm": current_path=qm_current_path;break;
            case "exynos":current_path=exynos_current_path;break;
            case "mt":current_path=mt_current_path;break;
            case "kirin":current_path=kirin_path;break;
            default:break;
        }
        if (Preference.getInt(context,"current_offset")==0){
            if (Build.BRAND.equalsIgnoreCase("samsung")||cpuModel.getVendor().equals("kirin")){
                Preference.saveInt(context,"current_offset",1);
            }else if (cpuModel.getVendor().equals("mt")){
                Preference.saveInt(context,"current_offset",10);
            }
        }

    }
    public boolean isCharge(){
        RootFile rootFile=new RootFile(status);
        if (cpuModel.getVendor().equals("kirin")){
            rootFile=new RootFile(kirin_status);
        }
        return rootFile.readFile().equalsIgnoreCase("charging");
    }
    public int getCurrent(){
        RootFile rootFile=new RootFile(current_path);
        try {
            return Math.abs(Integer.parseInt(rootFile.readFile()))/ Preference.getInt(context,"current_offset",1000);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
    public int getRealCurrent(){
        RootFile rootFile=new RootFile(current_path);
        try {
            return Integer.parseInt(rootFile.readFile());
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}
