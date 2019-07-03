package com.kai.lktMode;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransTool {
    private Context context;
    private Map<String,String> dictionary=new HashMap<>();
    public  TransTool(Context context){
        this.context=context;
        dictionary.put("brightness","亮度");
        dictionary.put("autoBright","自动亮度");
        dictionary.put("volume","音量");
    }
    public String getTitle(String str){
        return  dictionary.get(str);
    }
    public static void run(Context context){
        Set<String> strings=(Set<String>)Preference.get(context,"gameSettings","StringSet");
        for (String s:strings){
            String[] args=s.split(":");
            switch (args[0]){
                case "brightness":tuneBrightness(args[0],args[1],context);break;
                case "autoBright":tuneBrightness(args[0],args[1],context);break;
                case "volume":tuneVolume(args[1],context);break;
            }
        }
    }
    public static void restore(Context context){
        Set<String> strings=(Set<String>)Preference.get(context,"gameSettings","StringSet");
        for (String s:strings){
            String[] args=s.split(":");
            switch (args[0]){
                case "brightness":Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,(int)Preference.get(context,"brightness",125));break;
                case "autoBright":Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,(int)Preference.get(context,"autoBright",1));break;
                case "volume":AudioManager audio=(AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC,(int)Preference.get(context,"volume",15),0);
                break;
            }
        }


    }
    private static void tuneVolume(String sub,Context context){
        AudioManager audio = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
        int currentVolume =audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC,(int)(30*Integer.parseInt(sub)*0.01),0);
        Preference.save(context,"volume",currentVolume);
        Log.d("ss",currentVolume+"");
    }
    private static void tuneBrightness(String str,String sub,Context context){

        if (str.equals("brightness")){
            try {
                Preference.save(context,"brightness",Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
            }catch (Exception e){
                e.printStackTrace();
            }
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,(int) (255*Integer.parseInt(sub)*0.01));
            return;
        }else {
            try {
                Preference.save(context,"autoBright",Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE));
            }catch (Exception e){
                e.printStackTrace();
            }
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Boolean.parseBoolean(sub)?Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC:Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            return;
        }
    }
    public Item trans(String str){
        Item item;
        String[] args=str.split(":");
        String pattern = "\\d+";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(args[1]);
        if (m.find( )) {
            //Preference.save(MainActivity.this,"version",true);
            //Log.d("数字",m.group(1));
            item=new Item(getTitle(args[0]),m.group(0));
        } else {
            item=new Item(getTitle(args[0]),Boolean.parseBoolean(args[1]));
        }
        return item;
    }
    public String getKey(String value){
        for (String key:dictionary.keySet()){
            if (dictionary.get(key).equals(value)){
                return  key;
            }else continue;
        }
        return null;
    }
    public  List<Item> getItems(List<String> list){
        List<Item> items=new ArrayList<>();
        for (String l:list){
            items.add(trans(l));
        }
        return items;
    }
    public void save(List<Item> items){
        TreeSet<String> all=new TreeSet<>();
        for (Item item:items){
            if(item.getSubtitle()==null){
                all.add(getKey(item.getTitle())+":"+String.valueOf(item.getChecked()));
            }else {
                all.add(getKey(item.getTitle())+":"+item.getSubtitle());
            }
        }
        Preference.save(context,"gameSettings",all);
    }
}
