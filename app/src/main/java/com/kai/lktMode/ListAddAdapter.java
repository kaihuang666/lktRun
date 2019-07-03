package com.kai.lktMode;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListAddAdapter extends RecyclerView.Adapter<ListAddAdapter.ViewHolder> {
    private OnDeleteListener deleteListener;
    private List<Item> items=new ArrayList<>();
    private Context context;
    private HashMap<String,String> maps=new HashMap<>();
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        EditText subtitle;
        ImageButton delete;
        public ViewHolder(View v){
            super(v);
            title=v.findViewById(R.id.title);
            subtitle=v.findViewById(R.id.subtitle);
            delete=v.findViewById(R.id.delete);
        }
    }
    public ListAddAdapter(Context context, List<Item> items){
        for(Item item:items){
            add(item);
        }
        this.context=context;
    }
    public void add(Item item){
        if (maps.get(item.getTitle())==null){
            items.add(item);
            maps.put(item.getTitle(),"");
            notifyItemInserted(getItemCount()-1);
        }
    }
    public List<Item> getItems(){
        return items;
    }
    public void setDeleteListener(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_add,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Item item=items.get(position);
        holder.title.setText(item.getTitle());
        holder.subtitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        if (item.getSubtitle()==null){
            holder.subtitle.setFocusable(false);
            holder.subtitle.setText(item.getChecked()?"开启":"关闭");
            holder.subtitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    item.setChecked(!item.getChecked());
                    notifyItemChanged(position);
                }
            });
        }else {
            holder.subtitle.setText(item.getSubtitle());
            holder.subtitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Log.d("text",editable.toString());
                    if (editable.toString().isEmpty()){
                        item.setSubtitle("0");
                        notifyItemChanged(position);
                        return;
                    }
                    else if (Integer.parseInt(editable.toString())>100){
                        item.setSubtitle("100");
                        notifyItemChanged(position);
                        return;
                    }else if(editable.toString().charAt(0)=='0'&&editable.toString().length()>1){
                        item.setSubtitle(editable.toString().substring(1));
                        notifyItemChanged(position);
                        return;
                    }
                    item.setSubtitle(editable.toString());
                }
            });
        }
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //deleteListener.onDelete(position);
                notifyItemRemoved(position);
                items.remove(position);
                notifyItemRangeChanged(position, items.size() - position);
            }
        });
    }



    @Override
    public int getItemCount() {
        return items.size();
    }
    interface OnDeleteListener{
        void onDelete(int i);
    }
}

