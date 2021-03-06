package com.kai.lktMode.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.lktMode.bean.App;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.tool.Mode;
import com.kai.lktMode.tool.util.local.AppUtils;
import com.kai.lktMode.bean.Item;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class ListGameAdapter extends RecyclerView.Adapter<ListGameAdapter.ViewHolder> {
    private OnItemClick onItemClick=null;
    private OnItemRemoveClick removeClick=null;
    private OnCheckBoxChangListener changListener=null;
    private OnBottomClick bottomClick;
    private List<App> items;
    private Context context;
    private int TYPE;
    private int checked_sum=0;
    private boolean limit;
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
    public ListGameAdapter(Context context,List<App> items,int TYPE,boolean limit){
        this.items=items;
        this.context=context;
        this.TYPE=TYPE;
        this.limit=limit;
    }



    public void setBottomClick(OnBottomClick bottomClick) {
        this.bottomClick = bottomClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_game,parent,false);
        checked_sum= Preference.getGames(context).size();
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
            holder.name.setText("添加应用");
            holder.icon.setImageResource(R.mipmap.add_game);
            holder.remove.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomClick.onClick();
                }
            });
        }else {
            final App item=items.get(position);
            holder.name.setText(item.getName());
            Log.d("app",item.getName());
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Throwable {
                    Drawable drawable=item.getIcon();
                    emitter.onNext(drawable);
                    emitter.onComplete();
                }
            }).subscribe(new Observer<Object>() {
                @Override
                public void onNext(Object o) {
                    holder.icon.setImageDrawable((Drawable)o);
                }
                @Override
                public void onError(Throwable e) {

                }
                @Override
                public void onComplete() {

                }
                @Override
                public void onSubscribe(Disposable d) {

                }
            });
            //holder.icon.setImageDrawable(item.getIcon());
            if (item.getPackage_name().equals("com.tencent.mobileqq")||item.getPackage_name().equals("com.tencent.mm")||item.getPackage_name().equals("com.tencent.tim")){
                holder.name.setText(item.getName()+"(后台服务保留)");
            }
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeClick.onRemoveClick(position);
                }
            });
            if(TYPE==1){
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(item.isChecked());
                holder.checkBox.setTag(items.get(position));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Boolean b=!holder.checkBox.isChecked();
                        holder.checkBox.setChecked(b);
                    }
                });
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            if (++checked_sum > 5&&limit) {
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
        if (position==0){
            holder.itemView.setBackgroundResource(R.drawable.item_selector_top);
        }
        else if (position==getItemCount()-1){
            holder.itemView.setBackgroundResource(R.drawable.item_selector_bottom);
        }else {
            holder.itemView.setBackgroundResource(R.drawable.item_normal_none);
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
    public interface OnItemClick{
        public void onClick(int i);
    }
    public interface OnItemRemoveClick{
        public void onRemoveClick(int i);
    }
    public interface OnCheckBoxChangListener{
        public void onChange(int i, Boolean b);
    }
    public interface OnBottomClick{
        public void onClick();
    }

}

