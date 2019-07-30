package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.activity.AddActivity;
import com.kai.lktMode.adapter.ListGameAdapter;
import com.kai.lktMode.adapter.ListLabAdapter;
import com.kai.lktMode.tool.util.local.AppUtils;
import com.kai.lktMode.service.AutoService;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.activity.SleepSettingActivity;

import java.util.ArrayList;
import java.util.List;

public class LockFragment extends MyFragment {
    private ListLabAdapter adapter;
    private List<Item> items=new ArrayList<>();
    private List<Item> gameItems=new ArrayList<>();
    private ListGameAdapter gameAdapter;;
    private View view;
    private String[] checks=new String[]{"autoLock","autoClean","imClean"};
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_lock,null,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView=view.findViewById(R.id.recyclerview);
        initList();
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter=new ListLabAdapter(items);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClick(new ListLabAdapter.OnItemClick() {
            @Override
            public void onClick(int i) {

            }
        });
        updateList();
        initSoftwares();

        adapter.setOnItemCheck(new ListLabAdapter.OnItemCheck() {
            @Override
            public void onCheck(int i, Boolean isChecked) {
                Preference.save(getContext(),checks[i],isChecked);
                if (isChecked&&i==0){
                    Intent intent=new Intent(getContext(), AutoService.class);
                    intent.setAction("lockOn");
                    getActivity().startService(intent);
                    showDialog("锁屏沉睡功能，会在你的手机锁屏后进入超低功耗模式；选择进入修改则可以自定义调度和进入延迟", "进入修改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent=new Intent(getContext(), SleepSettingActivity.class);
                            startActivity(intent);
                        }
                    }, "忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                }else {
                    Intent intent=new Intent(getContext(),AutoService.class);
                    intent.setAction("lockOff");
                    getContext().startService(intent);
                }
            }
        });
    }
    private void updateList(){
        items.get(0).setChecked((Boolean)Preference.get(getContext(),"autoLock","Boolean"));
        items.get(1).setChecked((Boolean)Preference.get(getContext(),"autoClean","Boolean"));
        items.get(2).setChecked((Boolean)Preference.get(getContext(),"imClean","Boolean"));
        adapter.notifyDataSetChanged();
    }
    private void initList(){
        items.clear();
        Item item1=new Item("锁屏沉睡",false);
        Item item2=new Item("锁屏自动清理",false);
        Item item3=new Item("IM耗电优化",true);
        items.add(item1);
        items.add(item2);
        items.add(item3);
    }

    @Override
    public void Refresh() {
        super.Refresh();
        updateList();
        initSoftwares();
    }

    private void initSoftwares(){
        gameItems=new ArrayList<>();
        RecyclerView recyclerView=view.findViewById(R.id.gameList);
        for (String s:Preference.getSoftwares(getContext())){
            gameItems.add(new Item(s,false));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()){
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 360;
            }
        });
        gameAdapter=new ListGameAdapter(getContext(),gameItems,0,false);
        recyclerView.setAdapter(gameAdapter);
        gameAdapter.setBottomClick(new ListGameAdapter.OnBottomClick() {
            @Override
            public void onClick() {
                Intent intent=new Intent(getContext(), AddActivity.class);
                intent.setAction("softwares");
                startActivity(intent);
            }
        });
        gameAdapter.setRemoveClick(new ListGameAdapter.OnItemRemoveClick() {
            @Override
            public void onRemoveClick(int i) {
                Preference.softwareRemove(getContext(),gameItems.get(i).getTitle());
                gameAdapter.notifyItemRemoved(i);
                gameItems.remove(i);
                gameAdapter.notifyItemRangeChanged(i, items.size() - i);
            }
        });
    }
    private void upcateSoftware(){
        gameItems.clear();
        for (String s:Preference.getSoftwares(getContext())){
            if (AppUtils.getAppName(getContext(),s)==null){
                Preference.softwareRemove(getContext(),s);
                continue;
            }
            gameItems.add(new Item(s,false));
        }
        gameAdapter.notifyDataSetChanged();
    }
    private void showDialog(String str, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setNegativeButton("了解",listener)
                .setTitle("功能说明")
                .setMessage(str)
                .create().show();
    }
    private void showDialog(String str, String positive, DialogInterface.OnClickListener p, String negative, DialogInterface.OnClickListener n){
        new AlertDialog.Builder(getContext(),R.style.AppDialog)
                .setNegativeButton(negative,n)
                .setPositiveButton(positive,p)
                .setTitle("功能说明")
                .setMessage(str)
                .create().show();
    }
    

    @Override
    public void onResume() {
        super.onResume();
        upcateSoftware();
    }
}
