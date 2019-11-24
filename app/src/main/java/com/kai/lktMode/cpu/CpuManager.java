package com.kai.lktMode.cpu;

import android.os.Build;
import android.util.Log;

import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.util.local.CpuUtil;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.stericson.RootShell.execution.Shell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CpuManager {
    private RootFile cpu=new RootFile(SystemInfo.cpuKernelPath);
    private List<Kernel> kernels=new ArrayList<>();
    public CpuManager(){
        if (cpu.exists()){
            List<RootFile> cpus=cpu.listFiles();
            for (RootFile c:cpus){
                Matcher matcher= Pattern.compile("cpu([0-9])+").matcher(c.getName());
                if (matcher.matches()){
                    int i=Integer.valueOf(matcher.group(1));
                    kernels.add(i,new Kernel(c));
                }
            }

        }
    }
    public String backup(){
        StringBuilder builder=new StringBuilder("");
        CpuBoost cpuBoost=new CpuBoost();
        boolean isAddition=CpuBoost.isAddition();
        if (CpuBoost.isSupport()){
            builder.append(ShellUtil.chown(CpuBoost.INPUT_BOOST_FREQ,ShellUtil.getChown(CpuBoost.INPUT_BOOST_FREQ)));
            HashMap<String,String> map=CpuBoost.getBoostFreqences(cpuBoost.getFreq());
            builder.append(ShellUtil.chown(CpuBoost.INPUT_BOOST_MS,ShellUtil.getChown(CpuBoost.INPUT_BOOST_MS)));
            builder.append(CpuBoost.setMs(cpuBoost.getMs()));
            builder.append("chmod 666 "+SystemInfo.SystemModule$cpuboost+"/input_boost_freq\n");
            for (String key:map.keySet()){
                builder.append("echo '"+key+":"+map.get(key)+"'>"+SystemInfo.SystemModule$cpuboost+"/input_boost_freq\n");
            }
            builder.append("chmod 444 "+SystemInfo.SystemModule$cpuboost+"/input_boost_freq\n");
        }
        for (int i=0;i<this.getCounts();i++){
            //保存当前所有核心的最高最低频率
            CpuManager.Kernel kernel=this.getKernel(i);
            //保存当前所有核心的在线/离线状态
            if (kernel.isOnline()){
                builder.append(kernel.terminalOnline(kernel.isOnline()));
                builder.append(kernel.chownOnline());
            }
            else
                continue;
            builder.append(kernel.terminalScaling_max_freq(kernel.getScaling_max_freq()+"",isAddition));
            builder.append(kernel.chownScaling_max_freq());
            builder.append(kernel.terminalScaling_min_freq(kernel.getScaling_min_freq()+"",isAddition));
            builder.append(kernel.chownScaling_min_freq());
            //保存当前使用的调速器
            builder.append(kernel.terminalScaling_governor(kernel.getScaling_governor()));
            builder.append(kernel.chownScaling_governor());
            CpuManager.Governor governor=kernel.getGovernor(kernel.getScaling_governor());
            for (String key:governor.keySet){
                builder.append(governor.setValue(key,governor.getValue(key)));
                builder.append(governor.chown(key));
            }



        }
        return builder.toString();
    }
    public int getCurFreq(int i){
        return kernels.get(i).getScaling_cur_freq()/1000;
    }
    public boolean isEasKernel(){
        List<String> governors=Arrays.asList(getKernel(0).getAvailable_governors());
        return governors.contains("schedutil");
    }
    public Kernel[][] getCpuGroup(){
        //单独适配骁龙710、730、670、675；麒麟810
        String cpu=SystemInfo.getHardware().toLowerCase();
        if (cpu.contains("sm7150")||cpu.contains("sdm710")||cpu.contains("sdm670")||cpu.contains("sdm675")||(cpu.contains("810")&&!cpu.contains("9810"))||cpu.contains("mt6785")){
            return new Kernel[][]{getKernels(new int[]{0,1,2,3,4,5}),getKernels(new int[]{6,7})};
        }
        //单独适配骁龙855,1 huge 3big 4little
        if (cpu.contains("sm8150")){
            return new Kernel[][]{getKernels(new int[]{0,1,2,3}),getKernels(new int[]{4,5,6}),getKernels(new int[]{7})};
        }

        //单独适配麒麟980
        if (cpu.contains("980")||cpu.contains("9820")||cpu.contains("9825")){
            return new Kernel[][]{getKernels(new int[]{0,1,2,3}),getKernels(new int[]{4,5,}),getKernels(new int[]{6,7})};
        }
        //单独适配骁龙820、821
        if (cpu.contains("8996")){
            return new Kernel[][]{getKernels(new int[]{0,1}),getKernels(new int[]{2,3})};
        }
        //特殊的十核心架构，一般为2 huge 4big 4little
        if (getCounts()==10){
            return new Kernel[][]{getKernels(new int[]{0,1,2,3}),getKernels(new int[]{4,5,6,7}),getKernels(new int[]{8,9})};
        }
        //常见的八核心架构为4 big 4 little
        if (getCounts()==8){
            return new Kernel[][]{getKernels(new int[]{0,1,2,3}),getKernels(new int[]{4,5,6,7})};
        }
        //常见的六核心架构为2 big 4 little
        if (getCounts()==6){
            return new Kernel[][]{getKernels(new int[]{0,1,2,3}),getKernels(new int[]{4,5})};
        }
        //常见的四核心架构为4 little
        if (getCounts()==4){
            return new Kernel[][]{getKernels(new int[]{0,1,2,3})};
        }
        if (getCounts()==2){
            return new Kernel[][]{getKernels(new int[]{0,1})};
        }
        if (getCounts()==1){
            return new Kernel[][]{getKernels(new int[]{0})};
        }
        return null;
    }
    public Kernel[] getKernels(int[] count){
        List<Kernel> kernels=new ArrayList<>();
        for (int c:count){
            kernels.add(getKernel(c));
        }
        return kernels.toArray(new Kernel[]{});

    }
    public int[][] getKernelCount(){
        String cpu=SystemInfo.getHardware().toLowerCase();
        //适配联发科G90、G90T
        if (cpu.contains("sm7150")||cpu.contains("sdm710")||cpu.contains("sdm670")||cpu.contains("sdm675")||(cpu.contains("810")&&!cpu.contains("9810"))||cpu.contains("mt6785")){
            return new int[][]{new int[]{0,1,2,3,4,5},new int[]{6,7}};
        }
        //单独适配骁龙855,1 huge 3big 4little
        if (cpu.contains("sm8150")){
            return new int[][]{new int[]{0,1,2,3},new int[]{4,5,6},new int[]{7}};
        }

        //单独适配麒麟980
        if (cpu.contains("980")||cpu.contains("9820")||cpu.contains("9825")){
            return new int[][]{new int[]{0,1,2,3},new int[]{4,5,},new int[]{6,7}};
        }
        //单独适配骁龙820、821
        if (cpu.contains("8996")){
            return new int[][]{new int[]{0,1},new int[]{2,3}};
        }
        //特殊的十核心架构，一般为2 huge 4big 4little
        if (getCounts()==10){
            return new int[][]{new int[]{0,1,2,3},new int[]{4,5,6,7},new int[]{8,9}};
        }
        //常见的八核心架构为4 big 4 little
        if (getCounts()==8){
            return new int[][]{new int[]{0,1,2,3},new int[]{4,5,6,7}};
        }
        //常见的六核心架构为2 big 4 little
        if (getCounts()==6){
            return new int[][]{new int[]{0,1,2,3},new int[]{4,5}};
        }
        //常见的四核心架构为4 little
        if (getCounts()==4){
            return new int[][]{new int[]{0,1,2,3}};
        }
        if (getCounts()==2){
            return new int[][]{new int[]{0,1}};
        }
        if (getCounts()==1){
            return new int[][]{new int[]{0}};
        }
        return null;
    }
    public Kernel getKernel(int count){
        return kernels.get(count);
    }
    public int getCounts(){
        return kernels.size();
    }
    public String[] getArgs(RootFile file){
        String[] args=file.readFile().split("\\s");
        return args;
    }
    public class Kernel{
        RootFile kernel;
        RootFile cpuFreq;
        int max=0;
        int min=0;
        int division=0;
        int[] freqs;
        public Kernel(RootFile kernel){
            this.kernel=kernel;
            this.cpuFreq=SystemInfo.getChild(kernel,"cpufreq");
            this.max=getCpuinfo_max_freq();
            this.min=getCpuinfo_min_freq();
            this.division=max-min;
            this.freqs=getAvailable_ferqs();
        }
        public String tuneMaxFreq(double percentage,boolean isAddition){
            return terminalScaling_max_freq(getFreqAbout(getFreqByPercentage(percentage))+"",isAddition);
        }
        public String tuneMinFreq(double percentage,boolean isAddition){
            return terminalScaling_min_freq(getFreqAbout(getFreqByPercentage(percentage))+"",isAddition);
        }
        public int getFreqByPercentage(double percentage){
            return min+(int)(division*(percentage));
        }
        public int getFreqAbout(int freq){
            for (int f:freqs){
                if (f<freq)
                    continue;
                return f;
            }
            return freq;
        }
        public int getCount(){
            String kernelName=kernel.getName();
            Pattern pattern=Pattern.compile("cpu(\\d+)");
            Matcher m=pattern.matcher(kernelName);
            if (m.find()){
                return Integer.valueOf(m.group(1));
            }else {
                return 0;
            }
        }
        public boolean isOnline(){
            RootFile online=SystemInfo.getChild(kernel,"online");
            int isOnline= Integer.valueOf(online.readFile().isEmpty()?"0":online.readFile());
            if (isOnline==0&&!cpuFreq.exists())
                return false;
            else if (!cpuFreq.exists())
                return false;
            else
                return true;
        }
        public String chownOnline(){
            String online=SystemInfo.getChild(kernel,"online").getPath();
            return ShellUtil.chown(online,ShellUtil.getChown(online));
        }
        public String terminalOnline(boolean online){
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.P)
                return "";
            String path=SystemInfo.getChild(kernel,"online").getPath();
            return ShellUtil.modify(path,"'"+(online?"1":"0")+"'");
        }
        public String getKernelCount(){
            String name=kernel.getName();
            return name.substring(3);
        }
        public Governor getGovernor(String name){
            RootFile file=SystemInfo.getChild(cpuFreq,name);
            boolean g=name.equals("interactive")||name.equals("ondemand")||name.equals("conservative");
            if (g&&!file.exists()){
                return new Governor(new RootFile(SystemInfo.cpufreqGroupPath+"/"+name),Kernel.this);
            }

            return new Governor(file,Kernel.this);
        }


        public int getScaling_max_freq(){
            return getIntParam("scaling_max_freq");
        }
        public int getCpuinfo_max_freq(){
            return getIntParam("cpuinfo_max_freq");
        }
        public int getCpuinfo_min_freq(){
            return getIntParam("cpuinfo_min_freq");
        }
        public int getScaling_min_freq(){
            return getIntParam("scaling_min_freq");
        }
        public int getScaling_cur_freq(){
            return getIntParam("scaling_cur_freq");
        }
        private int getIntParam(String name){
            RootFile file=SystemInfo.getChild(cpuFreq,name);
            String str=file.readFile();
            if (str.isEmpty()){
                return 0;
            }
            return Integer.valueOf(file.readFile());
        }

        public String terminalParam(String name,String value){
            String path=SystemInfo.getChild(cpuFreq,name).getPath();
            return ShellUtil.modify(path,"'"+value+"'");
        }
        public String chownParam(String name){
            String path=SystemInfo.getChild(cpuFreq,name).getPath();
            return ShellUtil.chown(path,ShellUtil.getChown(path));
        }
        public String terminalScaling_min_freq(String value,boolean isAddition){
            StringBuilder builder=new StringBuilder("");
            if (isAddition)
                builder.append(terminalAdditionFreq("cpu_min_freq",value));
            builder.append(terminalParam("scaling_min_freq",value));
            return builder.toString();
        }
        public String chownScaling_min_freq(){
            return chownParam("scaling_min_freq");
        }
        private String terminalAdditionFreq(String key,String value){
            return ShellUtil.modify("/sys/module/msm_performance/parameters/"+key,"'"+getKernelCount()+":"+value+"'");
        }

        public String terminalScaling_max_freq(String value,boolean isAddition){
            StringBuilder builder=new StringBuilder("");
            if (isAddition)
                builder.append(terminalAdditionFreq("cpu_max_freq",value));
            builder.append(terminalParam("scaling_max_freq",value));
            return builder.toString();
        }
        public String chownScaling_max_freq(){
            return chownParam("scaling_max_freq");
        }
        public String terminalScaling_governor(String value){
            return terminalParam("scaling_governor",value);
        }
        public String chownScaling_governor(){
            return chownParam("scaling_governor");
        }
        public int[] getAvailable_ferqs(){
            String[] freqstr=getArgs(SystemInfo.getChild(cpuFreq,"scaling_available_frequencies"));
            int[] freqs=new int[freqstr.length];
            for (int i=0;i<freqstr.length;i++){
                if (freqstr[i].isEmpty())
                    continue;
                freqs[i]=Integer.valueOf(freqstr[i]);
            }
            int[] boosts=getBoost_freqs();
            if (boosts!=null){
                freqs=ShellUtil.concat(freqs,boosts);
            }
            if (freqs.length>2){
                if (freqs[0]>freqs[1])
                    freqs=ShellUtil.reverse(freqs);
            }
            return freqs;
        }
        public int[] getRelated_cpus(){
            String[] cpustr=getArgs(SystemInfo.getChild(cpuFreq,"related_cpus"));
            int[] cpus=new int[cpustr.length];
            for (int i=0;i<cpustr.length;i++){
                if (cpustr[i].isEmpty())
                    continue;
                cpus[i]=Integer.valueOf(cpustr[i]);
            }

            return cpus;
        }
        //额外的加速频率表
        public int[] getBoost_freqs(){
            RootFile file=SystemInfo.getChild(cpuFreq,"scaling_boost_frequencies");
            if (!file.exists())
                return null;
            String boost=file.readFile();
            if (boost.isEmpty())
                return null;
            String[] boosts=getArgs(file);
            int[] freqs=new int[boosts.length];
            for (int i=0;i<boosts.length;i++){
                if (boosts[i].isEmpty())
                    continue;
                freqs[i]=Integer.valueOf(boosts[i]);
            }
            return freqs;
        }
        public String[] getAvailable_governors(){
            String[] freqstr=getArgs(SystemInfo.getChild(cpuFreq,"scaling_available_governors"));
            return freqstr;
        }
        public String getScaling_governor(){
            RootFile file=SystemInfo.getChild(cpuFreq,"scaling_governor");
            return file.readFile();
        }
    }
    public class Governor{
        public List<String> keySet=new ArrayList<>();
        private RootFile dir;
        Kernel kernel;
        public Governor(RootFile dir,Kernel kernel){
            this.dir=dir;
            this.kernel=kernel;
            if (!dir.exists()){
                return;
            }
            for (RootFile f:dir.listFiles()){
                if (f.getName().contains("freqvar"))
                    continue;
                keySet.add(f.getName());
            }
        }
        public String getName(){
            return dir.getName();
        }
        public Kernel getParentKernel(){
            return kernel;
        }
        public String getValue(String key){
            return SystemInfo.getChild(dir,key).readFile();
        }
        public String setValue(String key,String value){
            String path=SystemInfo.getChild(dir,key).getPath();
            if (!kernel.isOnline())
                return "";
            return ShellUtil.lock(path,"'"+value+"'");
        }
        public String chown(String key){
            String path=SystemInfo.getChild(dir,key).getPath();
            return ShellUtil.chown(path,ShellUtil.getChown(path));
        }

    }
}
