package com.kai.lktMode.cpu;

import com.kai.lktMode.cpu.CpuBoost;
import com.kai.lktMode.cpu.CpuManager;

public class Group extends Object{
    private CpuManager.Kernel[] kernels;
    private CpuManager.Kernel main;
    private int maxFreq;
    private String maxFreqStr;
    private int minFreq;
    private String minFreqStr;
    private int[] freqs;
    private String[] freqsStr;
    private String governor;
    private String[] governors;
    private int[] counts;
    private CpuBoost cpuBoost;
    public Group(CpuManager.Kernel[] kernels,int[] counts){
        this.kernels=kernels;
        main=getMainKernel();
        maxFreq=main.getScaling_max_freq();
        minFreq=main.getScaling_min_freq();
        maxFreqStr=maxFreq/1000+"MHz";
        minFreqStr=minFreq/1000+"MHz";
        freqs=main.getAvailable_ferqs();
        freqsStr=new String[freqs.length];
        for (int i=0;i<freqs.length;i++){
            freqsStr[i]=freqs[i]/1000+"MHz";
        }
        governors=main.getAvailable_governors();
        governor=main.getScaling_governor();
        this.counts=counts;

    }

    public void setGovernor(String governor) {
        this.governor = governor;
    }

    public int[] getCounts() {
        return counts;
    }

    public void setCpuBoost(CpuBoost cpuBoost) {
        this.cpuBoost = cpuBoost;
    }

    public CpuBoost getCpuBoost() {
        return cpuBoost;
    }

    public int getLength(){
        return counts.length;
    }
    public CpuManager.Kernel getMainKernel(){
        return kernels[0];
    }
    public int getMaxFreq(){
        maxFreq=main.getScaling_max_freq();
        maxFreqStr=maxFreq/1000+"MHz";
        return maxFreq;
    }

    public CpuManager.Kernel[] getKernels() {
        return kernels;
    }

    public int getMinFreq() {
        maxFreq=main.getScaling_max_freq();
        maxFreqStr=maxFreq/1000+"MHz";
        return minFreq;
    }
    public String getMaxFreqStr() {
        getMaxFreq();
        return maxFreqStr;
    }

    public String getMinFreqStr() {
        minFreq=main.getScaling_min_freq();
        minFreqStr=minFreq/1000+"MHz";
        return minFreqStr;
    }

    public int[] getFreqs() {
        return freqs;
    }

    public String[] getFreqsStr() {
        return freqsStr;
    }

    public String[] getGovernors() {
        return governors;
    }

    public String getGovernor() {
        return governor;
    }
}
