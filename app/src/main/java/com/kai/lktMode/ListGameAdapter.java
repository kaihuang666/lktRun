package com.kai.lktMode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListGameAdapter extends RecyclerView.Adapter<ListGameAdapter.ViewHolder> {
    private OnItemClick onItemClick=null;
    private OnItemRemoveClick removeClick=null;
    private OnCheckBoxChangListener changListener=null;
    private OnBottomClick bottomClick;
    private List<Item> items;
    private Context context;
    private int TYPE;
    private int checked_sum=0;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView icon;
        Button remove;
        CheckBox checkBox;
        public ViewHolder(View v){
            super(v);
            name=v.findViewById(R.id.gameName);
            icon=v.findViewById(R.id.gameIcon);
            remove=v.findViewById(R.id.remove);
            checkBox=v.findViewById(R.id.checkbox);
        }
    }
    public ListGameAdapter(Context context,List<Item> items,int TYPE){
        this.items=items;
        this.context=context;
        this.TYPE=TYPE;
    }

    public void setBottomClick(OnBottomClick bottomClick) {
        this.bottomClick = bottomClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_game,parent,false);
        checked_sum=Preference.getGames(context).size();
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }
    public void setOnItemClick(OnItemClick click){
        onItemClick=click;
    }

    public void setRemoveClick(OnItemRemoveClick removeClick) {
        this.removeClick = removeClick;
    }

    public void setChangListener(OnCheckBoxChangListener changListener) {
        this.changListener = changListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.itemView.setOnClickListener(null);
        holder.remove.setVisibility(View.VISIBLE);
        holder.remove.setOnClickListener(null);
        if (position==getItemCount()-1&&TYPE==0){
            holder.name.setText("添加游戏加速应用");
            holder.icon.setImageResource(R.mipmap.add_game);
            holder.remove.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomClick.onClick();
                }
            });
        }else {
            final Item item=items.get(position);
            holder.name.setText(AppUtils.getAppName(context, item.getTitle()));
            holder.icon.setImageDrawable(AppUtils.getDrawable(context,item.getTitle()));
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyItemRemoved(position);
                    items.remove(position);
                    notifyItemRangeChanged(position, items.size() - position);
                    Preference.gameRemove(context,item.getTitle());
                }
            });
            if(TYPE==1){
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(item.getChecked());
                holder.checkBox.setTag(items.get(position));
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            if (++checked_sum > 5) {
                                Toast.makeText(context,"最多只能选择5个应用",Toast.LENGTH_SHORT).show();
                                holder.checkBox.setChecked(false);
                                item.setChecked(false);
                                checked_sum--;
                                return;
                            }else
                                item.setChecked(true);
                        }
                        else{
                            item.setChecked(false);
                            checked_sum--;
                        }
                        changListener.onChange(position,b);
                    }
                });
                holder.remove.setVisibility(View.INVISIBLE);
            }
        }
    }



    @Override
    public int getItemCount() {
        switch (TYPE){
            case 0:return items.size()+1;
            case 1:return items.size();
        }
        return 0;
    }
    interface OnItemClick{
        public void onClick(int i);
    }
    interface OnItemRemoveClick{
        public void onRemoveClick(int i);
    }
    interface OnCheckBoxChangListener{
        public void onChange(int i, Boolean b);
    }
    interface OnBottomClick{
        public void onClick();
    }
}

