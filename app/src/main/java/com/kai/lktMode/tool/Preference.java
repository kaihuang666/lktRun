package com.kai.lktMode.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Preference {
    public static void saveBoolean(Context context,String key,Boolean value){
        SharedPreferences.Editor editor=context.getSharedPreferences("db",Context.MODE_PRIVATE).edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public static void saveString(Context context,String key,String value){
        SharedPreferences.Editor editor=context.getSharedPreferences("db",Context.MODE_PRIVATE).edit();
        editor.putString(key,value);
        editor.apply();
    }
    public static void saveInt(Context context,String key,int value){
        SharedPreferences.Editor editor=context.getSharedPreferences("db",Context.MODE_PRIVATE).edit();
        editor.putInt(key,value);
        editor.apply();
    }
    public static void saveStringSet(Context context,String key,Set<String> value){
        SharedPreferences.Editor editor=context.getSharedPreferences("db",Context.MODE_PRIVATE).edit();
        editor.putStringSet(key,value);
        editor.apply();
    }
    public static void saveList(Context context,String key,List<String> value){
        SharedPreferences.Editor editor=context.getSharedPreferences("db",Context.MODE_PRIVATE).edit();
        editor.putStringSet(key,new TreeSet<>(value));
        editor.apply();
    }
    public static Boolean getBoolean(Context context,String key){
        SharedPreferences preferences=context.getSharedPreferences("db",Context.MODE_PRIVATE);
        return preferences.getBoolean(key,false);
    }
    public static String getString(Context context,String key){
        SharedPreferences preferences=context.getSharedPreferences("db",Context.MODE_PRIVATE);
        return preferences.getString(key,"");
    }
    public static int getInt(Context context,String key){
        SharedPreferences preferences=context.getSharedPreferences("db",Context.MODE_PRIVATE);
        return preferences.getInt(key,0);
    }
    public static Boolean getBoolean(Context context,String key,Boolean defaultValue){
        SharedPreferences preferences=context.getSharedPreferences("db",Context.MODE_PRIVATE);
        return preferences.getBoolean(key,defaultValue);
    }
    public static String getString(Context context,String key,String defaultValue){
        SharedPreferences preferences=context.getSharedPreferences("db",Context.MODE_PRIVATE);
        return preferences.getString(key,defaultValue);
    }
    public static int getInt(Context context,String key,int defaultValue){
        SharedPreferences preferences=context.getSharedPreferences("db",Context.MODE_PRIVATE);
        return preferences.getInt(key,defaultValue);
    }
    public static Set<String> getStringSet(Context context,String key){
        SharedPreferences preferences=context.getSharedPreferences("db",Context.MODE_PRIVATE);
        return preferences.getStringSet(key,new TreeSet<>());
    }
    public static List<String> getList(Context context,String key){
        SharedPreferences preferences=context.getSharedPreferences("db",Context.MODE_PRIVATE);
        return new ArrayList<>(preferences.getStringSet(key,new TreeSet<>()));
    }


    public static void clearAll(Context context){
        SharedPreferences.Editor editor=context.getSharedPreferences("db",Context.MODE_PRIVATE).edit();
        editor.putBoolean("version",false);
        editor.putBoolean("busybox",false);
        editor.apply();
    }
    public static void gameRemove(Context context,String packageName){
        List<String> list = getList(context,"games");
        list.remove(packageName);
        saveList(context,"games",list);
    }
    public static void softwareRemove(Context context,String packageName){
        List<String> list = getList(context,"softwares");
        list.remove(packageName);
        saveList(context,"softwares",list);
    }
    public static void gameAdd(Context context,String packageName){
        List<String> list = getList(context,"games");
        list.add(packageName);
        saveList(context,"games",list);
    }
    public static void softwareAdd(Context context,String packageName){
        List<String> list = getList(context,"softwares");
        list.add(packageName);
        saveList(context,"softwares",list);
    }
    public static List<String> getGames(Context context){
        return getList(context,"games");
    }
    public static List<String> getSoftwares(Context context){
        return getList(context,"softwares");
    }
}
