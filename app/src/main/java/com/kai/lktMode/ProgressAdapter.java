package com.kai.lktMode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.kai.lktMode.fragment.MainFragment;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ViewHolder> {
    private List<String> items=new ArrayList<>();
    private int[] freq=new int[MainFragment.getCpuAmount()];
    public int[] progress=new int[MainFragment.getCpuAmount()];
    private int sum=0;
    private ShellUtil shellUtil;
    static class ViewHolder extends RecyclerView.ViewHolder{
        ArcProgress progress;
        public ViewHolder(View v){
            super(v);
            progress=v.findViewById(R.id.arc_progress);
        }
    }
    public void setFreq(int[] freq){
        this.freq=freq;
    }
    public void setProgress(int[] progress){
        this.progress=progress;
    }
    public ProgressAdapter(List<String> items){
        this.items=items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cpu_progress,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (freq==null){
            return;
        }
        holder.progress.setBottomText(freq[position]+"mhz");
        if (freq[position]==0){
            holder.progress.setProgress(0);
            return;
        }
        holder.progress.setProgress(progress[position]);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public static int getMinCpuFreq(String cpu,ProcessBuilder cmd){
        String result = "0";
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/"+cpu+"/cpufreq/cpuinfo_min_freq"};
            //cmd = new ProcessBuilder();
            cmd.command(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = new String(re);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = "10";
        }
        Log.d("cpuMin",Integer.parseInt(result.trim())/1000+"");
        return Integer.parseInt(result.trim())/1000;
    }


}

