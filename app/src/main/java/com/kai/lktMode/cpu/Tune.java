package com.kai.lktMode.cpu;

import android.content.Context;

import com.kai.lktMode.selinux.SElinux;

public class Tune {
    private CpuModel model;
    private CpuManager manager;
    private Context context;
    private boolean isBoostSupport=false;
    private boolean isAddition=false;
    private Tune(Context context){
        this.context=context;
        this.model=CpuModel.getInstance(context);
        this.manager=new CpuManager();
        isBoostSupport=CpuBoost.isSupport();
        isAddition=CpuBoost.isAddition();
    }
    public int getFreqByPercentage(int min,int devision,double percentage){
        return min+(int)(devision*(percentage));
    }
    public int getFreqAbout(int[] freqs,int freq){
        for (int f:freqs){
            if (f<freq)
                continue;
            return f;
        }
        return freq;
    }
    public String hmpTune(int num){
        StringBuilder builder=new StringBuilder();
        for (int group=0;group<manager.getKernelCount().length;group++){
            int[] groupId=manager.getKernelCount()[group];
            for (int index=0;index<groupId.length;index++){
                int cpuId=groupId[index];
                //获取该核心的限制频率挡位
                CpuManager.Kernel kernel=manager.getKernel(cpuId);
                int[] freqs=kernel.getAvailable_ferqs();
                //设置该核心在线
                builder.append(kernel.terminalOnline(true));
                //设置该核心挡位
                CpuManager.Governor governor=kernel.getGovernor("interactive");
                //获取该核心最高频率
                int max=freqs[freqs.length-1];
                int min=freqs[0];
                int devision=max-min;
                switch (num){
                    case 0:
                        builder.append(kernel.terminalScaling_governor("interactive"));
                        if (group==0){

                        }
                }
            }

        }
        return builder.toString();
    }

    public String easTune(int num){
        StringBuilder builder=new StringBuilder();
        for (int group=0;group<manager.getKernelCount().length;group++){
            int[] groupId=manager.getKernelCount()[group];
            //获取所有cpu集群
            for (int index=0;index<groupId.length;index++){
                int cpuId=groupId[index];
                //获取该核心的限制频率挡位
                CpuManager.Kernel kernel=manager.getKernel(cpuId);
                int[] freqs=kernel.getAvailable_ferqs();
                //设置该核心在线
                builder.append(kernel.terminalOnline(true));
                //设置该核心挡位
                CpuManager.Governor governor=kernel.getGovernor("schedutil");
                //获取该核心最高频率
                int max=freqs[freqs.length-1];
                int min=freqs[0];
                int devision=max-min;
                switch (num){
                    //持久续航
                    case 0:
                        builder.append(kernel.terminalScaling_governor("powersave"));//设置调速器为powersave
                        if (group==0){
                            //设置小核心
                            if (model.getVendor().equals("exynos")){
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,1000)+"",isAddition));
                            }else {
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.15))+"",isAddition));//设置频率为最高800+
                            }
                            builder.append(kernel.terminalScaling_min_freq(freqs[1]+"",isAddition));//设置最低频率为最低频率
                        }
                        if (group==1){
                            //设置大核心
                            builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.25))+"",isAddition));//设置频率为最高800+
                            builder.append(kernel.terminalScaling_min_freq(freqs[0]+"",isAddition));//设置最低频率为最低频率
                        }
                        if (group==2){
                            //设置中央核心
                            builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.25))+"",isAddition));//设置频率为最高800+
                            builder.append(kernel.terminalScaling_min_freq(freqs[0]+"",isAddition));//设置最低频率为最低频率
                        }

                        if (isBoostSupport&&index==0){
                            builder.append(CpuBoost.setMs("1000"));
                            builder.append(CpuBoost.setFreq(cpuId,"0"));
                        }
                        break;
                    case 1://智能省电
                        builder.append(kernel.terminalScaling_governor("schedutil"));
                        if (group==0){
                            if (model.getVendor().equals("exynos")){
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,1000)+"",isAddition));
                            }else {
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.15))+"",isAddition));//设置频率为最高800+
                                builder.append(kernel.terminalScaling_min_freq(freqs[0]+"",isAddition));//设置最低频率为最低频率
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","8000"));
                                builder.append(governor.setValue("down_rate_limit_us","1000"));
                            }

                        }
                        if (group>0){
                            builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.25))+"",isAddition));//设置频率为最高800+
                            builder.append(kernel.terminalScaling_min_freq(freqs[0]+"",isAddition));//设置最低频率为最低频率

                            builder.append(governor.setValue("iowait_boost_enable","0"));
                            builder.append(governor.setValue("up_rate_limit_us","6000"));
                            builder.append(governor.setValue("down_rate_limit_us","1200"));
                        }
                        if (isBoostSupport&&index==0){
                            builder.append(CpuBoost.setMs("1000"));
                            builder.append(CpuBoost.setFreq(cpuId,"0"));
                        }

                        break;
                    case 2://沉稳均衡
                        if (group==0){
                            if (model.getVendor().equals("exynos")){
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,1200)+"",isAddition));
                            }else {
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.25))+"",isAddition));//设置频率为最高800+
                                builder.append(kernel.terminalScaling_min_freq(freqs[0]+"",isAddition));//设置最低频率为最低频率
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","4000"));
                                builder.append(governor.setValue("down_rate_limit_us","1000"));
                            }
                            //builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.25))+"",isAddition));//设置频率为最高800+

                        }
                        if (group>0){
                            builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.33))+"",isAddition));//设置频率为最高800+
                            builder.append(kernel.terminalScaling_min_freq(freqs[0]+"",isAddition));//设置最低频率为最低频率

                            builder.append(governor.setValue("iowait_boost_enable","0"));
                            builder.append(governor.setValue("up_rate_limit_us","1200"));
                            builder.append(governor.setValue("down_rate_limit_us","1000"));
                        }
                        if (isBoostSupport&&index==0){
                            builder.append(CpuBoost.setMs("1000"));
                            builder.append(CpuBoost.setFreq(cpuId,"0"));
                        }
                    case 3://智能均衡
                        builder.append(kernel.terminalScaling_governor("schedutil"));
                        if (group==0){
                            if (model.getVendor().equals("exynos")){
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,1300)+"",isAddition));
                            }else {
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.33))+"",isAddition));//设置频率为最高800+
                                builder.append(kernel.terminalScaling_min_freq(freqs[0]+"",isAddition));//设置最低频率为最低频率
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","6000"));
                                builder.append(governor.setValue("down_rate_limit_us","1000"));
                                if (isBoostSupport&&index==0){
                                    builder.append(CpuBoost.setMs("1000"));
                                    builder.append(CpuBoost.setFreq(cpuId,"0"));
                                }
                            }
                            //builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.33))+"",isAddition));//设置频率为最高1000+

                        }
                        if (group>0){
                            builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.33))+"",isAddition));//设置频率为最高1000+
                            builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.05))+"",isAddition));//设置最低频率为400+

                            builder.append(governor.setValue("iowait_boost_enable","0"));
                            builder.append(governor.setValue("up_rate_limit_us","3000"));
                            builder.append(governor.setValue("down_rate_limit_us","1200"));
                            if (isBoostSupport&&index==0){
                                builder.append(CpuBoost.setMs("1500"));
                                builder.append(CpuBoost.setFreq(cpuId,getFreqAbout(freqs,getFreqByPercentage(min,devision,0.1))+""));
                            }
                        }

                        break;
                    case 4://活跃均衡
                        builder.append(kernel.terminalScaling_governor("schedutil"));
                        if (group==0){
                            if (model.getVendor().equals("exynos")){
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,1300)+"",isAddition));
                            }else {
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.33))+"",isAddition));//设置频率为最高800+
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.05))+"",isAddition));//设置最低频率为最低频率
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","4000"));
                                builder.append(governor.setValue("down_rate_limit_us","1200"));
                                if (isBoostSupport&&index==0){
                                    builder.append(CpuBoost.setMs("1000"));
                                    builder.append(CpuBoost.setFreq(cpuId,"0"));
                                }
                            }
                            //builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.33))+"",isAddition));//设置频率为最高1000+

                        }
                        if (group>0){
                            builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.42))+"",isAddition));//设置频率为最高1000+
                            builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.1))+"",isAddition));//设置最低频率为400+

                            builder.append(governor.setValue("iowait_boost_enable","0"));
                            builder.append(governor.setValue("up_rate_limit_us","3000"));
                            builder.append(governor.setValue("down_rate_limit_us","1200"));
                            if (isBoostSupport&&index==0){
                                builder.append(CpuBoost.setMs("1500"));
                                builder.append(CpuBoost.setFreq(cpuId,getFreqAbout(freqs,getFreqByPercentage(min,devision,0.1))+""));
                            }
                        }

                        break;
                    case 5://适度游戏
                        if (model.getVendor().equals("exynos")){
                            if (group==0){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,1300)+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,800)+"",isAddition));
                            }
                            if (group==1){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,1800)+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,800)+"",isAddition));
                            }
                            if (group==2){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,2500)+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,1300)+"",isAddition));
                            }
                        }else {
                            if (group==0){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.48))+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.25))+"",isAddition));
                                if (isBoostSupport&&index==0){
                                    builder.append(CpuBoost.setMs("600"));
                                    builder.append(CpuBoost.setFreq(cpuId,getFreqAbout(freqs,getFreqByPercentage(min,devision,0.4))+""));
                                }
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","1200"));
                                builder.append(governor.setValue("down_rate_limit_us","3000"));
                            }
                            if (group==1){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.48))+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.28))+"",isAddition));
                                if (isBoostSupport&&index==0){
                                    builder.append(CpuBoost.setMs("500"));
                                    builder.append(CpuBoost.setFreq(cpuId,getFreqAbout(freqs,getFreqByPercentage(min,devision,0.42))+""));
                                }
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","1200"));
                                builder.append(governor.setValue("down_rate_limit_us","3000"));
                            }
                            if (group==2){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.55))+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.3))+"",isAddition));
                                if (isBoostSupport&&index==0){
                                    builder.append(CpuBoost.setMs("500"));
                                    builder.append(CpuBoost.setFreq(cpuId,getFreqAbout(freqs,getFreqByPercentage(min,devision,0.5))+""));
                                }
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","1200"));
                                builder.append(governor.setValue("down_rate_limit_us","3000"));
                            }
                        }



                        break;
                    case 6://重度游戏
                        if (model.getVendor().equals("exynos")){
                            if (group==0){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,1300)+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,800)+"",isAddition));
                            }
                            if (group==1){
                                builder.append(kernel.terminalScaling_governor("performance"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,2000)+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,1600)+"",isAddition));
                            }
                            if (group==2){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length-1]+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,1800)+"",isAddition));
                            }
                        }else {
                            if (group==0){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.65))+"",isAddition));//设置频率为倒数第二档
                                if (isBoostSupport&&index==0){
                                    builder.append(CpuBoost.setMs("600"));
                                    builder.append(CpuBoost.setFreq(cpuId,getFreqAbout(freqs,getFreqByPercentage(min,devision,0.4))+""));
                                }
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.25))+"",isAddition));//设置最低频率为400+
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","1200"));
                                builder.append(governor.setValue("down_rate_limit_us","3000"));
                            }
                            if (group==1&&manager.getKernelCount().length==2){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length-1]+"",isAddition));//设置频率为倒数第二档
                                if (isBoostSupport&&index==0){
                                    builder.append(CpuBoost.setMs("500"));
                                    builder.append(CpuBoost.setFreq(cpuId,getFreqAbout(freqs,getFreqByPercentage(min,devision,0.6))+""));
                                }
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,getFreqByPercentage(min,devision,0.5))+"",isAddition));//设置最低频率为400+
                                builder.append(governor.setValue("iowait_boost_enable","0"));
                                builder.append(governor.setValue("up_rate_limit_us","800"));
                                builder.append(governor.setValue("down_rate_limit_us","3000"));
                            }
                        }


                        break;
                    case 7://智能极限
                        if (model.getVendor().equals("exynos")){
                            if (group==0){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length-2]+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,1400)+"",isAddition));
                            }
                            if (group==1){
                                builder.append(kernel.terminalScaling_governor("performance"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length-1]+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,1600)+"",isAddition));
                            }
                            if (group==2){
                                builder.append(kernel.terminalScaling_governor("performance"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length-2]+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,2200)+"",isAddition));
                            }
                        }else {
                            if (group == 0) {
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length - 1] + "", isAddition));//设置频率为倒数第二档
                                if (isBoostSupport && index == 0) {
                                    builder.append(CpuBoost.setMs("600"));
                                    builder.append(CpuBoost.setFreq(cpuId, getFreqAbout(freqs, getFreqByPercentage(min, devision, 0.42)) + ""));
                                }
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs, getFreqByPercentage(min, devision, 0.54)) + "", isAddition));//设置最低频率为400+
                                builder.append(governor.setValue("iowait_boost_enable", "0"));
                                builder.append(governor.setValue("up_rate_limit_us", "800"));
                                builder.append(governor.setValue("down_rate_limit_us", "3000"));
                            }
                            if (group > 1) {
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length - 1] + "", isAddition));//设置频率为倒数第二档
                                if (isBoostSupport && index == 0) {
                                    builder.append(CpuBoost.setMs("500"));
                                    builder.append(CpuBoost.setFreq(cpuId, getFreqAbout(freqs, getFreqByPercentage(min, devision, 0.52)) + ""));
                                }
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs, getFreqByPercentage(min, devision, 0.52)) + "", isAddition));//设置最低频率为400+
                                builder.append(governor.setValue("iowait_boost_enable", "0"));
                                builder.append(governor.setValue("up_rate_limit_us", "800"));
                                builder.append(governor.setValue("down_rate_limit_us", "3000"));
                            }
                        }

                        break;
                    case 8://发烧极限
                        if (model.getVendor().equals("exynos")){
                            if (group==0){
                                builder.append(kernel.terminalScaling_governor("schedutil"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length-1]+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,1400)+"",isAddition));
                            }
                            if (group==1){
                                builder.append(kernel.terminalScaling_governor("performance"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length-1]+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,1800)+"",isAddition));
                            }
                            if (group==2){
                                builder.append(kernel.terminalScaling_governor("performance"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length-2]+"",isAddition));
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs,2300)+"",isAddition));
                            }
                        }else {
                            if (group == 0) {
                                builder.append(kernel.terminalScaling_governor("performance"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length - 1] + "", isAddition));//设置频率为倒数第二档
                                if (isBoostSupport && index == 0) {
                                    builder.append(CpuBoost.setMs("600"));
                                    builder.append(CpuBoost.setFreq(cpuId, getFreqAbout(freqs, getFreqByPercentage(min, devision, 0.6)) + ""));
                                }
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs, getFreqByPercentage(min, devision, 0.5)) + "", isAddition));//设置最低频率为400+
                                builder.append(governor.setValue("iowait_boost_enable", "0"));
                                builder.append(governor.setValue("up_rate_limit_us", "800"));
                                builder.append(governor.setValue("down_rate_limit_us", "3000"));
                            }
                            if (group > 1) {
                                builder.append(kernel.terminalScaling_governor("performance"));
                                builder.append(kernel.terminalScaling_max_freq(freqs[freqs.length - 1] + "", isAddition));//设置频率为倒数第二档
                                if (isBoostSupport && index == 0) {
                                    builder.append(CpuBoost.setMs("500"));
                                    builder.append(CpuBoost.setFreq(cpuId, getFreqAbout(freqs, getFreqByPercentage(min, devision, 0.65)) + ""));
                                }
                                builder.append(kernel.terminalScaling_min_freq(getFreqAbout(freqs, getFreqByPercentage(min, devision, 0.52)) + "", isAddition));//设置最低频率为400+
                                builder.append(governor.setValue("iowait_boost_enable", "0"));
                                builder.append(governor.setValue("up_rate_limit_us", "800"));
                                builder.append(governor.setValue("down_rate_limit_us", "3000"));
                            }
                        }
                        break;
                }
            }
        }
        return builder.toString();
    }
    public class HmpBuilder{
        CpuManager.Kernel kernel;
        CpuManager.Governor governor;
        public StringBuilder builder=new StringBuilder();
        public HmpBuilder(CpuManager.Kernel kernel, CpuManager.Governor governor){
            this.kernel=kernel;
            this.governor=governor;
        }
        public void addTargetLoads(String value){
            builder.append(governor.setValue("target_loads",value));
        }
        public void enableIoIsBusy(boolean enable){
            builder.append(governor.setValue("io_is_busy",enable?"1":"0"));
        }
        public void enablePrediction(boolean enable){
            builder.append(governor.setValue("enable_prediction",enable?"1":"0"));
        }
        public void enableBoost(boolean enable){
            builder.append(governor.setValue("boost",enable?"1":"0"));
        }
        public void enableAlignWindow(boolean enable){
            builder.append(governor.setValue("align_window",enable?"1":"0"));
        }
        public void addMaxFreqHysteresis(String value){
            builder.append(governor.setValue("max_freq_hyteresis",value));
        }
        public void addBoostPulseDuration(String value){
            builder.append(governor.setValue("boost_pulse_duration",value));
        }
    }
}
