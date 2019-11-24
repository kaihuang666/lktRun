package com.kai.lktMode.cpu;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.bean.Utils;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.util.local.AppUtils;
import com.kai.lktMode.tool.util.local.ShellUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CpuModel {
    private String vendor="unknown";
    private String model="unknown";
    private String vendor_name="未知厂商";
    private String model_name="未知型号";
    private String hardware=SystemInfo.getHardware().toLowerCase();
    //private String[] tune_type=new String[]{"diy"};
    private Context context;
    private static HashMap<String,String> map=new HashMap<>();
    List<String> tune_type_true=new ArrayList<>();
    static {
        map.put("lkt","LKT调度模块");
        map.put("yc","YC调度");
        map.put("diy","自定义调度");
        map.put("ycv2","YC调度(新版)");
        map.put("855tune","YC调度(EAS专版)");
    }
    private static HashMap<String,String> getMap(Context context,List<String> tune_type){
        List<String> tune_type_true=new ArrayList<>();
        //eas内核都应该支持LKT
        boolean isEas=new CpuManager().isEasKernel();
        Log.d("tune",tune_type.size()+"");
        for (String type:tune_type){
            if (type.equals("lkt")&&!ShellUtil.isMagiskInstall()) {
                continue;
            }
            if (isEas&&(type.equals("yc")||type.equals("ycv2"))){
                continue;
            }
            tune_type_true.add(type);
            if (type.equals("lkt")&&ShellUtil.isInstalled("lkt")){
                map.put("lkt","LKT调度模块(已安装)");
            }
        }
        return map;
    }
    public static List<String> getItem(Context context){
        List<String> tune_type_true=new ArrayList<>();
        String[] tune_type=getTuneTypes(context);
        //eas内核都应该支持LKT
        boolean isEas=new CpuManager().isEasKernel();
        //Log.d("tune",tune_type.length()+"");
        for (String type:tune_type){
            if (type.equals("lkt")&&!ShellUtil.isMagiskInstall()) {
                continue;
            }
            if (isEas&&(type.equals("yc")||type.equals("ycv2"))){
                continue;
            }
            if (type.equals("lkt")&&ShellUtil.isInstalled("lkt")){
                map.put("lkt","LKT调度模块(已安装)");
            }
            tune_type_true.add(map.get(type));
        }
        return tune_type_true;
    }
    public static List<String> getLastestTypes(Context context){
        List<String> tune_type_true=new ArrayList<>();
        String[] tune_type=getTuneTypes(context);
        //eas内核都应该支持LKT
        boolean isEas=new CpuManager().isEasKernel();
        //Log.d("tune",tune_type.length()+"");
        for (String type:tune_type){
            if (type.equals("lkt")&&!ShellUtil.isMagiskInstall()) {
                continue;
            }
            if (isEas&&(type.equals("yc")||type.equals("ycv2"))){
                    continue;
            }
            if (type.equals("lkt")&&ShellUtil.isInstalled("lkt")){
                map.put("lkt","LKT调度模块(已安装)");
            }
            tune_type_true.add(type);
        }
        return tune_type_true;
    }

    private CpuModel(Context context){
        this.context=context;



    }
    public void initialize(){
        getVendorFormSys();
        try {
            if (vendor.equals("unknown")||vendor.equals("other"))
                return;
            Preference.saveString(context,"vendor",vendor);
            Preference.saveString(context,"vendor_name",vendor_name);
            JSONArray cpu_models = new JSONArray(Utils.readAssetFile(context, vendor+".json"));
            for (int i=0;i<cpu_models.length();i++){
                JSONObject cpu_model=cpu_models.getJSONObject(i);
                String board=cpu_model.getString("board");

                if (SystemInfo.getHardware().contains(board)){
                    model=cpu_model.getString("model");
                    Preference.saveString(context,"model",model);
                    model_name=cpu_model.getString("model_name");
                    Preference.saveString(context,"model_name",model_name);
                    //tune_type=cpu_model.getString("tune").split("\\|");
                    //Log.d("sss",cpu_model.getString("eas-tune").split("\\|")[0]+"sss");
                    return;
                }
            }



        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String getYcUrl(Context context){
        try {
            String vendor=(String)Preference.getString(context,"vendor");
            String model=(String)Preference.getString(context,"model");
            JSONArray cpu_models = new JSONArray(Utils.readAssetFile(context, vendor+".json"));
            for (int i=0;i<cpu_models.length();i++){
                JSONObject cpu_model=cpu_models.getJSONObject(i);
                String modelv1=cpu_model.getString("model");
                if (model.contains(modelv1)){
                    return cpu_model.getString("yc_url");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public static String[] getTuneTypes(Context context){
        try {
            String vendor=(String)Preference.getString(context,"vendor");
            String model=(String)Preference.getString(context,"model");
            JSONArray cpu_models = new JSONArray(Utils.readAssetFile(context, vendor+".json"));
            for (int i=0;i<cpu_models.length();i++){
                JSONObject cpu_model=cpu_models.getJSONObject(i);
                String modelv1=cpu_model.getString("model");
                if (model.contains(modelv1)){
                    return cpu_model.getString("tune").split("\\|");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new String[]{"diy"};
    }
    public static CpuModel getInstance(Context context){
        return new CpuModel(context);
    }


    private void getVendorFormSys(){

        // Log.d("sss",Pattern.compile("(Qualcomm)",Pattern.CASE_INSENSITIVE).matcher("QUALCOMM").group(1));
        if (hardware.contains("qualcomm")){
             vendor="qualcomm";
             vendor_name="骁龙";
        }
        else if (hardware.contains("kirin")){
            vendor = "kirin";
            vendor_name="麒麟";
        }
        else if (hardware.contains("exynos")||hardware.contains("samsung")){
            vendor = "exynos";
            vendor_name="猎户座";
        }
        else if (hardware.contains("mt")){
            vendor = "mt";
            vendor_name="联发科";
        }
        else {
            vendor = "other";
            vendor_name="其他";
        }
    }

    public String getHardware() {
        return hardware;
    }

    public String getModel() {
        String v1=Preference.getString(context,"model");
        return v1.isEmpty()?model:v1;
    }

    public String getModel_name() {
        String v1=(String)Preference.getString(context,"model_name");
        return v1.isEmpty()?model_name:v1;
    }

    public String getVendor() {
        String v1=Preference.getString(context,"vendor");
        return v1.isEmpty()?vendor:v1;
    }

    public String getVendor_name() {
        String v1=Preference.getString(context,"vendor_name");
        return v1.isEmpty()?vendor_name:v1;
    }
}
