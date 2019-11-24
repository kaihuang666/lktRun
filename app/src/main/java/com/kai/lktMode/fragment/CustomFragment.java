package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.activity.CpuManagerActivity;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.cpu.CpuBoost;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.adapter.CustomAdapter;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.cpu.CpuModel;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.selinux.SElinux;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.tune.ExynosTune;
import com.kai.lktMode.tune.UniversalTune;
import com.kai.lktMode.widget.TerminalDialog;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomFragment extends MyFragment {
    private List<Item> items=new ArrayList<>();
    private int[] colors={R.color.colorBattery,R.color.colorBalance,R.color.colorPerformance,R.color.colorTurbo};
    private CustomAdapter adapter=new CustomAdapter(getContext(),items,colors);;
    private String output;
    private View view;
    private CpuModel cpuModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_custom,null,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initList();
        cpuModel=CpuModel.getInstance(getContext());
    }
    public void saveAll(){
        //如果adapter未初始化，先初始化它
        if (adapter==null){
            initList();
        }
        adapter.saveAll();
    }
    public String easTune(int num,boolean isAddition){
        CpuModel cpuModel=CpuModel.getInstance(getContext());
        if (cpuModel.getVendor().equals("exynos")){
            ExynosTune tune=new ExynosTune(getContext(),num);
            return tune.getCommand(isAddition);
        }else {
            UniversalTune tune=new UniversalTune(getContext(),num);
            return tune.getCommand(isAddition);
        }
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
    private void initList(){
        items.clear();
        Switch custom=(Switch)view.findViewById(R.id.swicth);
        //prevent the excepe
        custom.setChecked((Boolean) Preference.getBoolean(getContext(),"custom"));
        custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, boolean b) {
                if (b){
                    Preference.saveBoolean(getContext(),"custom",b);
                }else {
                    String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh";
                    File shell=new File(sdcard);
                    if (shell.exists()){
                        Toast.makeText(getContext(),"老铁，正在用动态脚本，不能关啊",Toast.LENGTH_LONG).show();
                        compoundButton.setChecked(true);
                    }else {
                        Preference.saveBoolean(getContext(),"custom",b);
                    }
                }
                getMain().refreshProp();
            }
        });
        items.add(new Item(SystemInfo.modes[0],""));
        items.add(new Item(SystemInfo.modes[1],""));
        items.add(new Item(SystemInfo.modes[2],""));
        items.add(new Item(SystemInfo.modes[3],""));
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview);
        final LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter=new CustomAdapter(getContext(),items,colors);
        adapter.setOnItemClick(new CustomAdapter.OnItemClick() {
            @Override
            public void onClick(int i, String a) {
                showTerminal(a);
            }
        });
        adapter.setOnImportClick(new CustomAdapter.OnImportClick() {
            @Override
            public void onImport(int i, final EditText e, ImageButton self) {
                PopupMenu popup = new PopupMenu(getContext(), self);//第二个参数是绑定的那个view
                //获取菜单填充器
                MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.importmenu, popup.getMenu());
                //绑定菜单项的点击事件
                final String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.importPowercfg:
                                Intent intent=new Intent(getContext(), CpuManagerActivity.class);
                                intent.setAction("creation");
                                intent.putExtra("code",i);
                                startActivityForResult(intent,10);
                                break;
                            case R.id.edit:
                                final TerminalDialog dialog=new TerminalDialog(getContext());
                                dialog.show();
                                dialog.setCancelable(true);
                                dialog.setCancelable(false);
                                dialog.addText("————————————————————\n脚本运行工具 Powered by 凯帝拉克\n————————————————————\n脚本编辑开始\n...");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String[] commands=e.getText().toString().split("\n");
                                        learn(commands,0,0,dialog,i);
                                    }
                                }).start();

                                break;
                            case R.id.clone:
                                new AlertDialog.Builder(getContext(),R.style.AppDialog)
                                        .setTitle("提示")
                                        .setMessage("克隆调度会将手机当前的处理器运行状况全部记录下来，可以间接地从你的面具模块或者其他软件中提取出调度，这需要你确认面具或软件已经运行成功，这并非是黑科技，通过其他软件也可以直接提取，只不过这更加方便。")
                                        .setPositiveButton("立即克隆", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (!SystemInfo.getIsDonated()){
                                                    Toast.makeText(getContext(),"该功能需要解锁捐赠版才能使用",Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                getClone(e);
                                            }
                                        })
                                        .setNegativeButton("取消",null)
                                        .show();
                                break;
                            case R.id.fit:
                                if (!SystemInfo.getIsDonated()){
                                    Toast.makeText(getContext(),"该功能需要捐赠版支持",Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                List<String> items=new ArrayList<>();
                                if (cpuModel.getVendor().equals("exynos"))
                                    items=ExynosTune.getModeNames(getContext());
                                else
                                    items=UniversalTune.getModeNames(getContext());
                                new AlertDialog.Builder(getContext(),R.style.AppDialog)
                                        .setTitle("请选择你需要生成的模式")
                                        .setItems(items.toArray(new String[]{}), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int index) {
                                                if (!new CpuManager().isEasKernel()){
                                                    Toast.makeText(getContext(),"暂只支持eas内核",Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                ProgressDialog progressDialog=new ProgressDialog(getContext(),R.style.AppDialog);
                                                progressDialog.setMessage("生成调度中");
                                                progressDialog.show();
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        String code=easTune(index,CpuBoost.isAddition());
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                e.setText(code);
                                                                Preference.saveString(getContext(),"code"+index,code);
                                                                progressDialog.dismiss();
                                                            }
                                                        });
                                                    }
                                                }).start();
                                            }
                                        }).show();
                                break;

                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        recyclerView.setAdapter(adapter);
        updateList();
    }
    private void getClone(EditText e){
        String[] args=new String[]{"正在分析cpu运行状态","正在克隆cpu核心频率","正在克隆cpu调速器","正在克隆cpu调速器参数","克隆调度完成"};
        TerminalDialog terminalDialog=new TerminalDialog(getContext());
        terminalDialog.setCancelable(false);
        terminalDialog.show();
        Handler handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                int count=msg.arg1;
                terminalDialog.addText(args[count]);
                if (count==args.length-1){
                    terminalDialog.setPositive("确定导入", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            e.setText(cloneCommand());
                            terminalDialog.cancel();
                        }
                    });
                    terminalDialog.setNegtive("取消导入", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            terminalDialog.cancel();
                        }
                    });
                    return;
                }
                Message message=new Message();
                message.arg1=count+1;
                sendMessageDelayed(message,100);
            }
        };
        Message message=new Message();
        message.arg1=0;
        handler.sendMessage(message);
    }
    private String cloneCommand(){
        boolean isAddition= CpuBoost.isAddition();
        CpuManager manager=new CpuManager();
        StringBuilder builder=new StringBuilder("");
        for (int i=0;i<manager.getCounts();i++){
            //dialog.addText("正在处理cpu"+i+"\n");
            //dialog.addText("获取cpu在线/离线状态");
            //保存当前所有核心的最高最低频率
            CpuManager.Kernel kernel=manager.getKernel(i);
            //保存当前所有核心的在线/离线状态
            if (kernel.isOnline()){
                //dialog.addText("cpu"+i+"在线");
                builder.append(kernel.terminalOnline(kernel.isOnline()));
            }
            else{
                //dialog.addText("cpu"+i+"离线，跳过该核心");
                continue;
            }
            //dialog.addText("获取当前的最高/最低频率");
            int max=kernel.getScaling_max_freq();
            int min=kernel.getScaling_min_freq();
            if (max>0)
                builder.append(kernel.terminalScaling_max_freq(max+"",isAddition));
            if (min>0)
                builder.append(kernel.terminalScaling_min_freq(min+"",isAddition));
            //dialog.addText("获取当前核心的所有调节参数");
            //保存当前使用的调速器
            builder.append(kernel.terminalScaling_governor(kernel.getScaling_governor()));
            CpuManager.Governor governor=kernel.getGovernor(kernel.getScaling_governor());
            for (String key:governor.keySet){
                builder.append(governor.setValue(key,governor.getValue(key)));
            }
        }
        //dialog.addText("克隆完毕");
        //dialog.cancel();
        return builder.toString();
    }
    public void updateList(){
        items.get(0).setSubtitle(Preference.getString(getContext(),"code1"));
        items.get(1).setSubtitle(Preference.getString(getContext(),"code2"));
        items.get(2).setSubtitle( Preference.getString(getContext(),"code3"));
        items.get(3).setSubtitle(Preference.getString(getContext(),"code4"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void Refresh() {
        super.Refresh();
        refresh();
    }

    public void refresh(){
        //updateList();
        Switch custom=(Switch)view.findViewById(R.id.swicth);
        custom.setChecked((Boolean) Preference.getBoolean(getContext(),"custom"));
        updateList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data==null){
            return;
        }
        if (requestCode==10){
            adapter.setCode(data.getIntExtra("code",0),data.getStringExtra("passage"));
        }
    }
    private void learn(final String[] args, final int count, final int error, final TerminalDialog dialog,int editID){
        try{
            RootUtils.runCommand(args[count], new RootUtils.onCommandComplete() {
                int errorCount=error;
                @Override
                public void onComplete() {
                    if (count==args.length-1){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                int success=args.length-error;
                                dialog.addText("第"+(count+1)+"条命令量化成功");
                                dialog.addText("\n"+success+"条命令量化成功,"+(error)+"条命令量化失败");
                                dialog.addText("....\n脚本量化完毕");
                                dialog.dismiss();
                                Intent intent=new Intent(getContext(), CpuManagerActivity.class);
                                intent.putExtra("code",editID);
                                intent.setAction("creation");
                                startActivityForResult(intent,10);
                                dialog.setPositive("关闭", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });

                    }else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.addText("第"+(count+1)+"条命令量化成功");
                            }
                        });
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                learn(args,count+1,errorCount,dialog,editID);
                            }
                        }).start();

                        //Log.d("sss",count+"");
                    }
                }

                @Override
                public void onCrash(String errorStr) {
                    if (errorStr.contains("/sys/module/msm_performance/parameters/")){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.addText("第"+(count+1)+"行命令量化忽略");
                            }
                        });

                        return;
                    }
                    errorCount++;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.addText("第"+(count+1)+"行命令执行失败");
                            dialog.addText("————————————————————\n失败原因："+getReason(errorStr));
                            dialog.addText("————————————————————");
                        }
                    });

                }

                @Override
                public void onOutput(String result) {

                }
            }, true);

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private String getReason(String result){
        if (result.contains("schedutil"))
            return "该条命令只能在shedutil工作模式下执行，而你的内核并没有切换到shedutil工作模式";
        if (result.contains("interactive"))
            return "该条命令只能在interactive工作模式下执行，而你的内核并没有切换到interactive工作模式";
        if (result.contains("conservative"))
            return "该条命令只能在conservative工作模式下执行，而你的内核并没有切换到interactive工作模式";
        if (result.contains("ondemand"))
            return "该条命令只能在ondemand工作模式下执行，而你的内核并没有切换到ondemand工作模式";
        if (result.contains("No such file or directory")&&result.contains("chmod")) {
            return "获取权限失败，该参数并不存在";
        }
        if (result.contains("Permission denied")){
            return "没有权限执行";
        }
        return "未知原因";

    }
    private void command(final String[] args, final int count, final int error, final TerminalDialog dialog){
        try{
            RootUtils.runCommand(args[count], new RootUtils.onCommandComplete() {
                int errorCount=error;
                @Override
                public void onComplete() {
                    if (count==args.length-1){
                        int success=args.length-errorCount;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.addText("第"+(count+1)+"条命令运行成功");
                                dialog.addText("\n"+success+"条命令运行成功,"+(error)+"条命令运行失败");
                                dialog.addText("....\n脚本运行完毕");
                                dialog.setPositive("关闭", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });

                    }else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.addText("第"+(count+1)+"条命令运行成功");
                            }
                        });
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                command(args,count+1,errorCount,dialog);
                            }
                        }).start();


                    }
                }

                @Override
                public void onCrash(String errorStr) {
                    if (errorStr.contains("/sys/module/msm_performance/parameters/")){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.addText("第"+(count+1)+"行命令执行忽略");
                            }
                        });

                        return;
                    }
                    errorCount++;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.addText("第"+(count+1)+"行命令执行失败");
                            dialog.addText("————————————————————\n失败原因："+getReason(errorStr));
                            dialog.addText("————————————————————");
                        }
                    });

                }

                @Override
                public void onOutput(String result) {

                }
            }, true);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void showTerminal(String str){
        //output="脚本运行开始:\n";
        final TerminalDialog dialog=new TerminalDialog(getContext());
        dialog.show();
        dialog.addText("————————————————————\n脚本运行工具 Powered by 凯帝拉克\n————————————————————\n脚本运行开始\n...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] commands=str.split("\n");
                command(commands,0,0,dialog);
            }
        }).start();

        //Command command=new Command(0,"");
    }


    private void showDialog(String str, String positive, DialogInterface.OnClickListener p, String negative, DialogInterface.OnClickListener n){
        new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setNegativeButton(negative,n)
                .setPositiveButton(positive,p)
                .setTitle("功能说明")
                .setMessage(str)
                .create().show();
    }

}
