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

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private OnItemClick onItemClick=null;
    private OnItemCheck onItemCheck=null;
    private List<Item> items;
    class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView subtitle;
        public ViewHolder(View v){
            super(v);
            title=v.findViewById(R.id.list_title);
            subtitle=v.findViewById(R.id.list_subtitle);
        }
    }
    class CheckViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        Switch checkBox;
        public CheckViewHolder(View v){
            super(v);
            title=v.findViewById(R.id.list_title);
            checkBox=v.findViewById(R.id.swicth);
        }
    }
    public ListAdapter(List<Item> items){
        this.items=items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0){
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_all,parent,false);
            return (RecyclerView.ViewHolder) new ViewHolder(view);
        }else {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_lab,parent,false);
            return (RecyclerView.ViewHolder) new CheckViewHolder(view);
        }

    }
    public void setOnItemClick(OnItemClick click){
        onItemClick=click;
    }

    public void setOnItemCheck(OnItemCheck onItemCheck) {
        this.onItemCheck = onItemCheck;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, final int position) {
        Item item=items.get(position);
        String sub=item.getSubtitle();
        if (sub!=null){
            ViewHolder holder=(ViewHolder)h;
            holder.title.setText(item.getTitle());
            holder.subtitle.setText(item.getSubtitle());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClick.onClick(position);
                }
            });
        }else {
            CheckViewHolder holder=(CheckViewHolder)h;
            holder.title.setText(item.getTitle());
            holder.checkBox.setChecked(item.getChecked());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    onItemCheck.onCheck(position,b,compoundButton);
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position).getSubtitle()==null){
            return 1;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public interface OnItemClick{
        public void onClick(int i);
    }
    public interface OnItemCheck{
        public void onCheck(int i,Boolean isChecked,CompoundButton compoundButton);
    }
}

