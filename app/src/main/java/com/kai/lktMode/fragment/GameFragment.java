package com.kai.lktMode.fragment;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.AppUtils;
import com.kai.lktMode.AutoService;
import com.kai.lktMode.GameBoostActivity;
import com.kai.lktMode.Item;
import com.kai.lktMode.Preference;
import com.kai.lktMode.R;
import com.kai.lktMode.SleepSettingActivity;

import java.util.ArrayList;
import java.util.List;

public class GameFragment extends MyFragment {
    private ListLabAdapter adapter;
    private List<Item> items=new ArrayList<>();
    private List<Item> gameItems=new ArrayList<>();
    private String[] checks={"gameMode"};
    private ListGameAdapter gameAdapter;;
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_lab,container,false);
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
                switch (i){
                    case 0:showDialog("功能设计来源于一加游戏模式\n游戏加速功能只能加速限5个横屏游戏；选择进入修改则可以自定义游戏模式的调度和操作。", "进入修改", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent1=new Intent(getContext(), GameBoostActivity.class);
                            startActivity(intent1);
                        }
                    }, "忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });break;
                }
            }
        });
        adapter.setOnItemCheck(new ListLabAdapter.OnItemCheck() {
            @Override
            public void onCheck(int i, Boolean isChecked) {
                Preference.save(getContext(),checks[i],isChecked);
                switch (i){

                    case 0:
                        if (isChecked){
                            if (hasPermission()){
                                Intent intent=new Intent(getContext(),AutoService.class);
                                intent.setAction("gameOn");
                                getContext().startService(intent);
                            }else {
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                //intent.setData(Uri.parse(getActivity().getPackageName()));
                                startActivityForResult(intent,10);
                            }

                        }else {
                            Intent intent=new Intent(getContext(),AutoService.class);
                            intent.setAction("gameOff");
                            getContext().startService(intent);
                        }
                        break;
                }
            }
        });
        updateList();
        initGame();
    }
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getActivity().getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getContext().getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    private void updateList(){
        if (!hasPermission()){
            Preference.save(getContext(),"gameMode",false);
        }
        items.get(0).setChecked((Boolean)Preference.get(getContext(),"gameMode","Boolean"));
        adapter.notifyDataSetChanged();
    }
    private void initList(){
        items.clear();
        Item item2=new Item("游戏加速",false);
        items.add(item2);
    }
    private void initGame(){
        gameItems=new ArrayList<>();
        RecyclerView recyclerView=view.findViewById(R.id.gameList);
        for (String s:Preference.getGames(getContext())){
            gameItems.add(new Item(s,false));
        }
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        gameAdapter=new ListGameAdapter(getContext(),gameItems,0,false);
        recyclerView.setAdapter(gameAdapter);
        gameAdapter.setBottomClick(new ListGameAdapter.OnBottomClick() {
            @Override
            public void onClick() {
                Intent intent=new Intent(getContext(), AddActivity.class);
                intent.setAction("games");
                startActivity(intent);
            }
        });
        gameAdapter.setRemoveClick(new ListGameAdapter.OnItemRemoveClick() {
            @Override
            public void onRemoveClick(int i) {
                Preference.gameRemove(getContext(),gameItems.get(i).getTitle());
                gameAdapter.notifyItemRemoved(i);
                gameItems.remove(i);
                gameAdapter.notifyItemRangeChanged(i, items.size() - i);
            }
        });
    }
    private void upcateGames(){
        gameItems.clear();
        for (String s:Preference.getGames(getContext())){
            if (AppUtils.getAppName(getContext(),s)==null){
                Preference.softwareRemove(getContext(),s);
                continue;
            }
            gameItems.add(new Item(s,false));
        }
        gameAdapter.notifyDataSetChanged();
    }

    @Override
    public void Refresh() {
        super.Refresh();
        updateList();
        initGame();
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
        upcateGames();
    }
    public void closeGame(){
        if (hasPermission()){
            Intent intent=new Intent(getContext(), AutoService.class);
            intent.setAction("gameOn");
            getContext().startService(intent);
        }else {
            Toast.makeText(getContext(),"需要使用情况访问权限！",Toast.LENGTH_LONG).show();
            items.get(0).setChecked(false);
            adapter.notifyItemChanged(0);
            Preference.save(getContext(),checks[0],false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==10){
            closeGame();
        }
    }
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==10){
            if (hasPermission()){
                Intent intent=new Intent(getContext(), AutoService.class);
                intent.setAction("gameOn");
                getContext().startService(intent);
            }else {
                Toast.makeText(getContext(),"需要使用情况访问权限！",Toast.LENGTH_LONG).show();
                items.get(2).setChecked(false);
                adapter.notifyItemChanged(2);
            }
        }
    }*/
}
