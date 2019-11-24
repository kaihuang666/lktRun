package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.bean.Item;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.adapter.PowercfgAdapter;
import com.kai.lktMode.bean.Sdcard;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowercfgFragment extends MyFragment {
    private List<Item> items=new ArrayList<>();
    private int[] colors={R.color.colorBattery,R.color.colorBalance,R.color.colorPerformance,R.color.colorTurbo};
    private PowercfgAdapter adapter;
    private String output;
    private View view;
    private TextView title;
    private Button change;
    private String sdcard;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_powercfg,null,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initList();


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sdcard= Sdcard.getPath(getContext()) +"/lktMode/powercfg/powercfg.sh";
    }

    public void saveAll(){
        if (adapter==null){
            Toast.makeText(getContext(),"正在刷新数据，请稍后重试",Toast.LENGTH_SHORT).show();
            Refresh();
            return;
        }
        adapter.saveAll();
    }
    private void initList(){
        items.clear();
        title=view.findViewById(R.id.list_title);
        change=view.findViewById(R.id.change);
        items.add(new Item("省电",""));
        items.add(new Item("均衡",""));
        items.add(new Item("游戏",""));
        items.add(new Item("极限",""));
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview);
        final LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter=new PowercfgAdapter(getContext(),items,colors);
        adapter.setOnItemClick(new PowercfgAdapter.OnItemClick() {
            @Override
            public void onClick(int i, String a) {
                showTerminal(a);
            }
        });
        adapter.setOnImportClick(new PowercfgAdapter.OnImportClick() {
            @Override
            public void onImport(int i, final EditText e, ImageButton self) {
                PopupMenu popup = new PopupMenu(getContext(), self);//第二个参数是绑定的那个view
                //获取菜单填充器
                MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.extramenu, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (!new File(sdcard).exists())
                            return false;
                        switch (menuItem.getItemId()){
                            case R.id.code6:
                                e.setText("level 6");
                                break;
                            case R.id.code5:
                                e.setText("powersave");
                                break;
                            case R.id.code4:
                                e.setText("level 4");
                                break;
                            case R.id.code3:
                                e.setText("balance");
                                break;
                            case R.id.code2:
                                e.setText("level 2");
                                break;
                            case R.id.code1:
                                e.setText("performance");
                                break;
                            case R.id.code0:
                                e.setText("fast");
                                break;

                        }
                        Preference.saveString(getContext(),"code"+(i+1),"sh "+sdcard+" "+e.getText().toString());
                        getMain().getFragment(1).Refresh();
                        return true;
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (change.getText().equals("导入")){
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        startActivityForResult(intent,12);
                        return;
                    }
                    new LFilePicker()
                            .withActivity(getActivity())
                            .withRequestCode(11)
                            .withIconStyle(Constant.ICON_STYLE_BLUE)
                            .withMutilyMode(false)
                            .withStartPath(Sdcard.getPath(getContext()))
                            .withIsGreater(false)
                            .withFileSize(500 * 1024)
                            .start();

                }else {
                    try {
                        File file=new File(sdcard);
                        if (file.delete()){
                            change();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        if (!isImported()){
            //adapter.closeAll();
        }else {
            updateList();
        }

    }

    @Override
    public void Refresh() {
        super.Refresh();
        isImported();
    }

    public boolean  isImported(){
        boolean imported=false;
        Log.d("sdcard",sdcard);
        File shell=new File(sdcard);
        shell.getParentFile().mkdirs();
        if (!shell.exists()){
            change.setText("导入");
            title.setText("动态脚本：未导入");
            imported=false;
            updateList();
        }else {
            change.setText("移除");
            title.setText("动态脚本：已导入");
            imported=true;
            updateList();
        }

        return imported;
    }
    public void change(){
        if (change.getText().equals("移除")){
            change.setText("导入");
            title.setText("动态脚本：未导入");
            for (int i=1;i<=4;i++){
                Preference.saveString(getContext(),"code"+i,"");
            }
            Preference.saveBoolean(getContext(),"custom",false);
            updateList();
        }else {
            Toast.makeText(getContext(),"导入成功",Toast.LENGTH_SHORT).show();
            Preference.saveString(getContext(),"code1","sh "+sdcard+" powersave");
            Preference.saveString(getContext(),"code2","sh "+sdcard+" balance");
            Preference.saveString(getContext(),"code3","sh "+sdcard+" performance");
            Preference.saveString(getContext(),"code4","sh "+sdcard+" fast");
            change.setText("移除");
            title.setText("动态脚本：已导入");
            Preference.saveBoolean(getContext(),"custom",true);
            updateList();
        }
        getMain().getFragment(1).Refresh();
        getMain().refreshProp();

    }

    private String getParam(String str){
        Pattern pattern=Pattern.compile("powercfg.sh\\s+(.*)");
        Matcher m=pattern.matcher(str);
        if (m.find()){
            return m.group(1);
        }else {
            return "";
        }
    }
    public void updateList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code1=(String) Preference.getString(getContext(),"code1");
                String code2=(String) Preference.getString(getContext(),"code2");
                String code3=(String) Preference.getString(getContext(),"code3");
                String code4=(String) Preference.getString(getContext(),"code4");
                items.get(0).setSubtitle(getParam(code1));
                items.get(1).setSubtitle(getParam(code2));
                items.get(2).setSubtitle(getParam(code3));
                items.get(3).setSubtitle(getParam(code4));
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();



    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void showTerminal(String str){
        output="脚本开始:\n";
        final AlertDialog alertDialog=new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setMessage(output)
                .setTitle("运行结果")
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        alertDialog.show();
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RootUtils.runCommand("sh " + Sdcard.getPath(getContext()) + "/lktMode/powercfg/powercfg.sh " + str, new RootUtils.onCommandComplete() {
                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onCrash(String error) {

                        }

                        @Override
                        public void onOutput(String line) {
                            if (line.contains("No such file or directory")){
                                line="内核不支持该命令";
                            }if (line.contains("Permission denied")){
                                line="命令无法执行，内核正在被其他调度占用";
                            }
                            output+=line+"\n";
                            Log.d("output",line);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialog.setMessage(output);
                                    alertDialog.show();
                                }
                            });

                        }
                    }, true);
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
