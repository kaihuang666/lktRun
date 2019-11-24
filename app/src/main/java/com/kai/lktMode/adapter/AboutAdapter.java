package com.kai.lktMode.adapter;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.R;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.widget.SuperEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {
    List<EditText> editTexts=new ArrayList<>();
    private OnItemClick onItemClick=null;
    private List<Item> items;
    private Context context;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView subtitle;
        public ViewHolder(View v){
            super(v);
            title=v.findViewById(R.id.list_title);
            subtitle=v.findViewById(R.id.list_subtitle);
        }
    }
    public AboutAdapter(Context context, List<Item> items){
        this.context=context;
        this.items=items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_all,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }
    public void setOnItemClick(OnItemClick click){
        onItemClick=click;
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Item item=items.get(position);
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick.onClick(position);
            }
        });
        if (position==0){
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


}

