package com.kai.lktMode.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.bean.Item;
import com.kai.lktMode.R;

import java.util.List;

public class ListLabAdapter extends RecyclerView.Adapter<ListLabAdapter.ViewHolder> {
    private OnItemClick onItemClick=null;
    private OnItemCheck onItemCheck=null;
    private List<Item> items;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        Switch swicth;
        public ViewHolder(View v){
            super(v);
            title=v.findViewById(R.id.list_title);
            swicth=v.findViewById(R.id.swicth);
        }
    }
    public ListLabAdapter(List<Item> items){
        this.items=items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_lab,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }
    public void setOnItemClick(OnItemClick click){
        onItemClick=click;
    }
    public void setOnItemCheck(OnItemCheck check){
        onItemCheck=check;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Item item=items.get(position);
        holder.title.setText(item.getTitle());
        holder.swicth.setChecked(item.getChecked());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick.onClick(position);
            }
        });
        holder.swicth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onItemCheck.onCheck(position,b);
            }
        });
        if (getItemCount()==1){
            holder.itemView.setBackgroundResource(R.drawable.item_selector);
        }
        else if (position==0){
            holder.itemView.setBackgroundResource(R.drawable.item_selector_top);
        }
        else if (position==getItemCount()-1){
            holder.itemView.setBackgroundResource(R.drawable.item_selector_bottom);
        }else {
            holder.itemView.setBackgroundResource(R.drawable.item_selector_none);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public interface OnItemClick{
        public void onClick(int i);
    }
    public interface OnItemCheck{
        public void onCheck(int i,Boolean isChecked);
    }
}

