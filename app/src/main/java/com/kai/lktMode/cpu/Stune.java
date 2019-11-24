package com.kai.lktMode.cpu;

import android.util.Log;

import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class Stune {
    RootFile stune;
    public static String stunePath="/dev/stune/";
    List<StuneBean> stuneBeans=new ArrayList<>();
    public static boolean isSupport(){
        return new RootFile(stunePath+"/top-app/schedtune.boost").exists();
    }
    public Stune(){
        stune=new RootFile(stunePath);
        for (RootFile rootFile:stune.listFiles()){
            if (rootFile.isDirectory()){
                stuneBeans.add(new StuneBean(rootFile));
            }
        }
    }

    public List<StuneBean> getStuneBeans() {
        return stuneBeans;
    }
    public int getSize(){
        return stuneBeans.size();
    }
    public class StuneBean{
        RootFile bean;
        RootFile boost;
        RootFile prefer;
        public String getName(){
            switch (bean.getName()){
                case "background":return "后台进程";
                case "foreground":return "前台进程";
                case "rt":return "多线程进程";
                case "top-app":return "当前进程";
                default:return bean.getName();
            }
        }
        public StuneBean(RootFile file){
            bean=file;
            boost= SystemInfo.getChild(file,"schedtune.boost");
            prefer=SystemInfo.getChild(file,"schedtune.prefer_idle");
        }
        public StuneBean(String name){
            bean=new RootFile(stunePath+"/"+name);
            boost= SystemInfo.getChild(bean,"schedtune.boost");
            prefer=SystemInfo.getChild(bean,"schedtune.prefer_idle");
        }
        public boolean isPrefer(){
            return prefer.readFile().equals("1");
        }

        public int getBoost() {
            return Integer.valueOf(boost.readFile());
        }

        public String setBoost(String value) {
            return ShellUtil.modify(boost.getPath(),"'"+value+"'");
        }

        public String setPrefer(Boolean isPrefer) {
            return ShellUtil.modify(prefer.getPath(),"'"+(isPrefer?1:0)+"'");
        }
    }
}
