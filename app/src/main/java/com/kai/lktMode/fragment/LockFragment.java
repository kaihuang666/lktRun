package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.kai.lktMode.base.MyApplication;
import com.kai.lktMode.bean.App;
import com.kai.lktMode.tool.util.local.AppUtils;
import com.kai.lktMode.service.AutoService;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.activity.SleepSettingActivity;
import com.kai.lktMode.widget.SimplePaddingDecoration;

import java.util.ArrayList;
import java.util.List;

public class LockFragment extends MyFragment {
    private ListLabAdapter adapter;
    private List<Item> items=new ArrayList<>();
    private List<App> gameItems=new ArrayList<>();
    private ListGameAdapter gameAdapter;
    private View contentView;
    private String[] checks=new String[]{"autoLock","autoClean","imClean"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_lock, container, false);
        }
        ViewGroup parent = (ViewGroup) contentView.getParent();
        if (parent != null) {
            parent.removeView(contentView);
        }
        return contentView;
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        RecyclerView recyclerView=contentView.findViewById(R.id.recyclerview);
        initList();
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter=new ListLabAdapter(items);
        recyclerView.addItemDecoration(new SimplePaddingDecoration(getContext()));
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
                Preference.saveBoolean(getContext(),checks[i],isChecked);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void updateList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                items.get(0).setChecked((Boolean)Preference.getBoolean(getContext(),"autoLock"));
                items.get(1).setChecked((Boolean)Preference.getBoolean(getContext(),"autoClean"));
                items.get(2).setChecked((Boolean)Preference.getBoolean(getContext(),"imClean"));
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();


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
        RecyclerView recyclerView=contentView.findViewById(R.id.gameList);
        for (String s:Preference.getSoftwares(getContext())){
            gameItems.add(new App(AppUtils.getAppName(getContext(),s),s,AppUtils.getDrawable(getContext(),s),false));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()){
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 360;
            }
        });
        recyclerView.addItemDecoration(new SimplePaddingDecoration(getContext()));
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
                Preference.softwareRemove(getContext(),gameItems.get(i).getPackage_name());
                gameAdapter.notifyItemRemoved(i);
                gameItems.remove(i);
                gameAdapter.notifyItemRangeChanged(i, items.size() - i);
            }
        });
    }
    private void upcateSoftware(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                gameItems.clear();
                for (String s:Preference.getSoftwares(getContext())){
                    if (AppUtils.getAppName(getContext(),s)==null){
                        Preference.softwareRemove(getContext(),s);
                        continue;
                    }
                    gameItems.add(new App(AppUtils.getAppName(getContext(),s),s,AppUtils.getDrawable(getContext(),s),false));
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        gameAdapter.notifyDataSetChanged();
                    }
                });

            }
        }).start();

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
