package com.kai.lktMode.tool;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.kai.lktMode.network.LTE;
import com.kai.lktMode.root.RootUtils;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Settings {
    public static HashMap<String,String> dictionary=new HashMap<>();
    private List<Setting> items=new ArrayList<>();
    private List<String> settings=new ArrayList<>();
    private boolean isLock;
    private String lock="";
    private Context context;
    static {
        dictionary.put("bluetooth","蓝牙");
        dictionary.put("wifi","无线网络");
        dictionary.put("nfc","NFC");
        dictionary.put("data","数据网络");
        dictionary.put("gps","GPS");
    }
    public static List<String> getValues(){
        List<String> values=new ArrayList<>();
        for (String key:dictionary.keySet()){
            values.add(dictionary.get(key));
        }
        return values;
    }
    public static String[] getKeys(){
        return dictionary.keySet().toArray(new String[]{});
    }
    private Settings(Context context,boolean isLock){
        this.isLock=isLock;
        this.context=context;
        lock=isLock?"lockSettings":"unlockSettings";
        settings=Preference.getList(context,lock);
        if (settings.size()==0)
            return;
        for (String s:settings){
            Setting setting=new Setting(context,s);
            items.add(setting);
        }
    }
    public static Settings getInstance(Context context,boolean isLock){
        return new Settings(context,isLock);
    }
    public void add(String key,boolean enable){
        String code=key+":"+(enable?"enable":"disable");
        settings.add(code);
        items.add(new Setting(context,code));
        Preference.saveList(context,lock,settings);
    }
    public void set(int i,boolean enable){
        Setting setting=items.get(i);
        String key=setting.getKey();
        String code=key+":"+(enable?"enable":"disable");
        settings.set(i,code);
        items.set(i,setting);
        Preference.saveList(context,lock,settings);
    }
    public void remove(int i){
        settings.remove(i);
        items.remove(i);
        Preference.saveList(context,lock,settings);
    }

    public List<Setting> getItems() {
        return items;
    }

    public class Setting{
        private String name="";
        private String key="";
        private String value="";
        private String code="";
        private String command="";
        private Context context;
        boolean ishalf=false;
        private int version= Build.VERSION.SDK_INT;
        public Setting(Context context,String code){
            this.context=context;
            this.code=code;
            String[] codes=code.split(":");
            try {
                if (codes.length==2){
                    key=codes[0];
                    value=codes[1];
                    name=dictionary.get(key);
                    command="svc "+key+" "+value;
                }
            }catch (Exception e){
                e.printStackTrace();
            }


        }
        public boolean isEnable(){
            return value.contains("enable");
        }
        public String getName() {
            return name;
        }




        public void command(){
            if (key.equals("gps")){
                String operator=isEnable()?"+":"-";
                if (version>=Build.VERSION_CODES.O)
                    command="settings put secure location_providers_allowed "+operator+"gps,"+operator+"network";
                else
                    command="settings put secure location_providers_allowed "+operator+"gps";
            }
            Log.d("c",command);
            RootUtils.runCommand(command);
        }

        public String getKey() {
            return key;
        }

        public void setEnable(Boolean enable){
            value=enable?"enable":"disable";
        }
    }

}
