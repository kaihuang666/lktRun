package com.kai.lktMode.cpu;

import android.util.Log;

import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.util.local.ShellUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cpuset {
    static HashMap<String,String> dictionarty=new HashMap<>();
     static {
        dictionarty.put("audio-app","媒体进程");
        dictionarty.put("background","后台进程");
        dictionarty.put("foreground","前台进程");
        dictionarty.put("system-background","系统后台进程");
        dictionarty.put("top-app","当前进程");
        dictionarty.put("restricted"," 受限进程");
        dictionarty.put("camera-daemon","相机进程");
    }
    RootFile cpuset;
    List<CpusetBean> cpusetBeans=new ArrayList<>();
    public static boolean isSupport(){
        return new RootFile(SystemInfo.cpusetPath).exists();
    }

    public List<CpusetBean> getCpusetBeans() {
        return cpusetBeans;
    }

    public int getSize(){
        return cpusetBeans.size();
    }
    public Cpuset(){
        cpuset=new RootFile(SystemInfo.cpusetPath);
        for (RootFile file:cpuset.listFiles()){
            RootFile cpus=SystemInfo.getChild(file,"cpus");
            if (cpus.exists()){
                RootUtils.runCommand("chown -R root:root "+cpus.getPath());
                RootUtils.runCommand("chmod 644 "+cpus.getPath());
                cpusetBeans.add(new CpusetBean(file));
            }

        }

    }
    public class CpusetBean{
        RootFile bean;
        RootFile cpus;
        public CpusetBean(RootFile bean){
            this.bean=bean;
            cpus=SystemInfo.getChild(bean,"cpus");
        }
        public String getName(){
            String name=dictionarty.get(bean.getName());
            if (name==null||name.isEmpty()){
                return bean.getName();
            }
            return name;
        }
        public String getValue(){

            return ShellUtil.command(new String[]{"cat",cpus.getPath()});
            //return "";
        }
        public void setValue(String value){
            RootUtils.runCommand(ShellUtil.modify(cpus.getPath(),"'"+value+"'"));
        }
    }
}
