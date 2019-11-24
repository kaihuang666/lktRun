package com.kai.lktMode.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.R;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.tool.Settings;

import java.util.ArrayList;
import java.util.List;

public class LockSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private OnBottomClick onBottomClick=null;
    private List<Settings.Setting> items=new ArrayList<>();
    private Settings settings;
    private Context context;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView subtitle;
        ImageButton delete;
        public ViewHolder(View v){
            super(v);
            title=v.findViewById(R.id.title);
            subtitle=v.findViewById(R.id.subtitle);
            delete=v.findViewById(R.id.delete);
        }
    }
    static class AddHolder extends RecyclerView.ViewHolder{
        public AddHolder(View view){
            super(view);
        }
    }
    public LockSettingsAdapter(Context context, Settings settings){
        this.context=context;
        this.items=settings.getItems();
        this.settings=settings;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType==0){
            holder=new AddHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lock_add,parent,false));
        }else {
            holder=new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lock,parent,false));
        }

        return holder;
    }
    public void setOnItemClick(OnBottomClick click){
        onBottomClick=click;
    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder h, final int position) {

        if (getItemViewType(position)==0){
            AddHolder holder=(AddHolder)h;
            holder.itemView.setBackgroundResource(R.drawable.item_selector_bottom);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBottomClick.onClick();
                }
            });
        }else {
            Settings.Setting item=items.get(position);
            ViewHolder holder=(ViewHolder) h;
            holder.itemView.setBackgroundResource(R.drawable.item_selector_none);
            holder.title.setText(item.getName());
            holder.subtitle.setText(item.isEnable()?"开":"关");
            holder.subtitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settings.set(position,!item.isEnable());
                    items.get(position).setEnable(!item.isEnable());
                    notifyItemChanged(position);
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settings.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,items.size()-position);
                }
            });



        }

    }

    @Override
    public int getItemCount() {
        return items.size()+1;
    }
    public interface OnBottomClick{
        public void onClick();
    }

    @Override
    public int getItemViewType(int position) {
        if (position==getItemCount()-1){
            return 0;
        }
        return 1;
    }
}

