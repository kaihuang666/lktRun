package com.kai.lktMode.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.kai.lktMode.R;
import com.kai.lktMode.fragment.MainFragment;
import com.kai.lktMode.tool.util.local.CpuUtil;
import com.kai.lktMode.tool.util.local.ShellUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ViewHolder> {
    private List<String> items=new ArrayList<>();
    private int[] freq=new int[CpuUtil.getCpuAmount()];
    public int[] progress=new int[CpuUtil.getCpuAmount()];
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
        if (freq==null||freq.length==0){
            return;
        }
        if (freq[position]==0){
            holder.progress.setBottomText("离线");
        }else {
            holder.progress.setBottomText(freq[position]+"mhz");
        }

        if (freq[position]==0){
            holder.progress.setProgress(0);
            return;
        }
        int now=progress[position];
        holder.progress.setProgress(now);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}

