package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
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

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.adapter.PowercfgAdapter;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh";
    }

    public void saveAll(){
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
                    DialogProperties properties = new DialogProperties();
                    properties.selection_mode = DialogConfigs.SINGLE_MODE;
                    properties.selection_type = DialogConfigs.FILE_SELECT;
                    properties.root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                    properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                    properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                    properties.extensions = null;
                    FilePickerDialog dialog = new FilePickerDialog(getContext(),properties,R.style.AppDialog);
                    dialog.setTitle("选择一个调度文件");
                    dialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            Log.d("sss",files[0]);
                            if (MainActivity.copyFile(files[0],Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh")){
                                change();
                            }else {
                                Toast.makeText(getContext(),"导入失败",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    dialog.show();

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
        File shell=new File(sdcard);
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
    private void change(){
        if (change.getText().equals("移除")){
            change.setText("导入");
            title.setText("动态脚本：未导入");
            for (int i=1;i<=4;i++){
                Preference.save(getContext(),"code"+i,"");
            }
            Preference.save(getContext(),"custom",false);
            updateList();
        }else {
            Toast.makeText(getContext(),"导入成功",Toast.LENGTH_SHORT).show();
            Preference.save(getContext(),"code1","sh "+sdcard+" powersave");
            Preference.save(getContext(),"code2","sh "+sdcard+" balance");
            Preference.save(getContext(),"code3","sh "+sdcard+" performance");
            Preference.save(getContext(),"code4","sh "+sdcard+" fast");
            change.setText("移除");
            title.setText("动态脚本：已导入");
            Preference.save(getContext(),"custom",true);
            updateList();
        }
        getMain().getFragment(2).Refresh();
        getMain().refreshProp();

    }



    public void updateList(){
        String code1=(String) Preference.get(getContext(),"code1","String");
        items.get(0).setSubtitle(code1.substring(code1.lastIndexOf(" ")+1));
        String code2=(String) Preference.get(getContext(),"code2","String");
        items.get(1).setSubtitle(code2.substring(code2.lastIndexOf(" ")+1));
        String code3=(String) Preference.get(getContext(),"code3","String");
        items.get(2).setSubtitle(code3.substring(code3.lastIndexOf(" ")+1));
        String code4=(String) Preference.get(getContext(),"code4","String");
        items.get(3).setSubtitle(code4.substring(code4.lastIndexOf(" ")+1));;
        adapter.notifyDataSetChanged();
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
            RootTools.getShell(true).add(new Command(2,"sh "+Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh "+str){
                @Override
                public void commandOutput(int id, String line) {
                    super.commandOutput(id, line);
                    if (line.contains("No such file or directory")){
                        line="内核不支持该命令";
                    }if (line.contains("Permission denied")){
                        line="命令无法执行，内核正在被其他调度占用";
                    }
                    output+=line+"\n";
                    Log.d("output",line);
                    alertDialog.setMessage(output);
                    alertDialog.show();
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    super.commandCompleted(id, exitcode);
                    Log.d("output",exitcode+"");
                    output+="脚本运行完成";
                    alertDialog.setMessage(output);
                    alertDialog.show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
