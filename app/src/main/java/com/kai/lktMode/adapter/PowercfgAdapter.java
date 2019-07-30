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

import com.kai.lktMode.bean.Item;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PowercfgAdapter extends RecyclerView.Adapter<PowercfgAdapter.ViewHolder> implements View.OnTouchListener {
    List<EditText> editTexts=new ArrayList<>();
    private OnItemClick onItemClick=null;
    private OnImportClick onImportClick=null;
    private List<Item> items;
    private int[] colors=new int[4];
    private Context context;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        EditText edit;
        ImageButton run;
        ImageButton extra;
        public ViewHolder(View v){
            super(v);
            title=v.findViewById(R.id.title);
            edit=v.findViewById(R.id.edit);
            run=v.findViewById(R.id.run);
            extra=v.findViewById(R.id.extra);
        }
    }
    public PowercfgAdapter(Context context, List<Item> items, int[] colors){

        this.context=context;
        this.items=items;
        this.colors=colors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_powercfg,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }
    public void setOnItemClick(OnItemClick click){
        onItemClick=click;
    }

    public void setOnImportClick(OnImportClick onImportClick) {
        this.onImportClick = onImportClick;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        editTexts.add(position,holder.edit);
        String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/powercfg/powercfg.sh";
        File file=new File(sdcard);
        if (!file.exists()){
            holder.edit.setEnabled(false);
        }else {
            holder.edit.setEnabled(true);
        }
        Item item=items.get(position);
        holder.title.setText(item.getTitle());
        holder.edit.setText(item.getSubtitle());
        holder.edit.setOnTouchListener(this);
        holder.run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClick!=null)
                onItemClick.onClick(position,holder.edit.getText().toString());
            }
        });
        holder.extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onImportClick.onImport(position,holder.edit,holder.extra);
            }
        });
    }

    public void saveAll(){
        String sdcard= Environment.getExternalStorageDirectory().getAbsolutePath();
        for (int i=0;i<4;i++){
            Preference.save(context,"code"+(i+1),"sh "+sdcard+"/lktMode/powercfg/powercfg.sh "+editTexts.get(i).getText().toString());
        }
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
    public interface OnItemClick{
        public void onClick(int i, String a);
    }
    public interface OnImportClick{
        public void onImport(int i, EditText e, ImageButton self);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId()==R.id.edit&&canVerticalScroll((EditText)view)){
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return false;
    }
    private boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() -editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if(scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }

}

