package com.kai.lktMode.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.kai.lktMode.R;
import com.kai.lktMode.bean.ParentItem;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.cpu.CpuBoost;
import com.kai.lktMode.cpu.CpuManager;
import com.kai.lktMode.cpu.Cpuset;
import com.kai.lktMode.cpu.Group;
import com.kai.lktMode.cpu.Stune;
import com.kai.lktMode.gpu.GPUFreq;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.selinux.SElinux;
import com.kai.lktMode.thermal.Thermal;
import com.kai.lktMode.thermal.ThermalBean;
import com.kai.lktMode.tool.util.local.ShellUtil;
import com.kai.lktMode.widget.AnimatedExpandableListView;
import com.kai.lktMode.widget.ParamDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CpuAdvancedManagerAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private CpuManager manager=new CpuManager();
    private List<ParentItem> parentItems=new ArrayList<>();
    public CpuAdvancedManagerAdapter(Context context, List<ParentItem> parentItems){
        this.context=context;
        this.parentItems=parentItems;
        this.mInflater=LayoutInflater.from(context);
    }

    @Override
    public Object getChild(int i, int i1) {
        ParentItem item=(ParentItem)getGroup(i);
        return item.getObject();
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }




    private class TitleHolderView {
        TextView titleView;
        ImageView indicator;
        RelativeLayout layout;
    }
    private class SElinuxHolderView{
        TextView title;
        Switch prefer;
    }
    private class ThermalHolderView{
        TextView title;
        Switch prefer;
    }
    private class FreqHolderView{
        TextView title;
        TextView freq;
        Switch aSwitch;
    }
    private class CommonHolderView{
        TextView title;
        TextView subtitle;
        TextView freq;
    }


    @Override
    public int getRealChildrenCount(int groupPosition) {
        ParentItem parentItem=(ParentItem)getGroup(groupPosition);
        int type=parentItem.getType();
        if (type==SystemInfo.type_cpuset){
            return ((Cpuset)parentItem.getObject()).getSize()+1;
        }
        if (type==SystemInfo.type_selinux)
            return 2;
        if (type==SystemInfo.type_thermal){
            return ((Thermal)parentItem.getObject()).getBeans().size();
        }
        return 0;
    }

    @Override
    public int getRealChildType(int groupPosition, int childPosition) {
        ParentItem parentItem=(ParentItem)getGroup(groupPosition);
        return parentItem.getType();
    }

    @Override
    public int getRealChildTypeCount() {
        return 3;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        TitleHolderView titleHolderView;
        ParentItem parentItem=(ParentItem)getGroup(i);
        if (view==null){
            titleHolderView=new TitleHolderView();
            view=mInflater.inflate(R.layout.list_title, viewGroup, false);
            titleHolderView.titleView=view.findViewById(R.id.list_title);
            titleHolderView.indicator=view.findViewById(R.id.indicator);
            titleHolderView.layout=view.findViewById(R.id.layout);
            view.setTag(titleHolderView);
        }else {
            titleHolderView = (TitleHolderView) view.getTag();
        }
        titleHolderView.titleView.setText(parentItem.getTitle());
        if (b){
            titleHolderView.indicator.setImageResource(R.drawable.indicator_up);
            titleHolderView.layout.setBackgroundResource(R.drawable.item_title_press);
        }else {
            titleHolderView.indicator.setImageResource(R.drawable.indicator_down);
            titleHolderView.layout.setBackgroundResource(R.drawable.item_title_normal);
        }
        return view;
    }


    @Override
    public View getRealChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        int type=parentItems.get(i).getType();
        if (type==SystemInfo.type_cpuset){
            if (i1==0){
                view=mInflater.inflate(R.layout.list_cpuset_title,viewGroup,false);
                int length=manager.getKernelCount().length;
                int[][] group=manager.getKernelCount();
                if (length==1){
                    ((TextView)view.findViewById(R.id.subtitle)).setText("核心分配:0-"+(manager.getCounts()-1)+"(全部核心)");
                }else if (length==2){
                    int devision1=group[0][group[0].length-1];
                    int devision2=group[1][group[1].length-1];
                    ((TextView)view.findViewById(R.id.subtitle)).setText("核心分配:0-"+devision1+"(小核心)"+","+(devision1+1)+"-"+devision2+"(大核心)");
                }else {
                    int devision1=group[0][group[0].length-1];
                    int devision2=group[1][group[1].length-1];
                    int devision3=group[2][group[2].length-1];
                    ((TextView)view.findViewById(R.id.subtitle)).setText("核心分配:0-"+devision1+"(小核心)"+","+(devision1+1)+"-"+devision2+"(大核心)"+","+(devision2+1)+"-"+devision3+"(超大核心)");
                }
                //((TextView)view.findViewById(R.id.subtitle)).setText("核心分配:(核心数-核心数)");
            }
            else {
                Cpuset cpuset=(Cpuset) parentItems.get(i).getObject();
                Cpuset.CpusetBean bean=cpuset.getCpusetBeans().get(i1-1);
                view=mInflater.inflate(R.layout.list_cpuset,viewGroup,false);
                TextView title=(TextView)view.findViewById(R.id.title);
                TextView cpus=(TextView)view.findViewById(R.id.cpus);
                title.setText(bean.getName());
                cpus.setText(bean.getValue());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View view1=View.inflate(context,R.layout.dialog_edittext,null);
                        EditText e=view1.findViewById(R.id.edit);
                        new AlertDialog.Builder(context,R.style.AppDialog)
                                .setView(view1)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {
                                        String value=e.getText().toString();
                                        Pattern pattern=Pattern.compile("[^0-9 \\- ,]+");
                                        Matcher m=pattern.matcher(value);
                                        if (m.find()){
                                            Toast.makeText(context,"你输入了错误字符！",Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        bean.setValue(e.getText().toString());
                                        cpus.setText(e.getText().toString());
                                        notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int index) {

                                    }
                                })
                                .show();
                        e.setText(String.valueOf(bean.getValue()));

                    }
                });
            }
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;
        }
        if (type==SystemInfo.type_selinux){
            SElinuxHolderView holderView;
            if (view==null){
                view=mInflater.inflate(R.layout.list_stune,viewGroup,false);
                holderView=new SElinuxHolderView();
                holderView.title=view.findViewById(R.id.title);
                holderView.prefer=view.findViewById(R.id.prefer);
                view.setTag(holderView);
            }else {
                holderView=(SElinuxHolderView)view.getTag();
            }
            if (i1==0){
                holderView.title.setText("SElinux开关");
                holderView.prefer.setOnCheckedChangeListener(null);
                holderView.prefer.setChecked(SElinux.getEnable());
                holderView.prefer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        SElinux.setEnable(b);
                    }
                });

            }
            if (i1==1){
                holderView.title.setText("SElinux禁用");
                holderView.prefer.setOnCheckedChangeListener(null);
                holderView.prefer.setChecked(false);
                holderView.prefer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    }
                });
            }
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;
        }
        if (type==SystemInfo.type_thermal){
            ThermalHolderView holderView;
            if (view==null){
                view=mInflater.inflate(R.layout.list_stune,viewGroup,false);
                holderView=new ThermalHolderView();
                holderView.title=view.findViewById(R.id.title);
                holderView.prefer=view.findViewById(R.id.prefer);
                view.setTag(holderView);
            }else {
                holderView=(ThermalHolderView)view.getTag();
            }
            Thermal thermal=(Thermal) parentItems.get(i).getObject();
            ThermalBean bean=thermal.getBeans().get(i1);
            holderView.title.setText(bean.getName());
            holderView.prefer.setOnCheckedChangeListener(null);
            holderView.prefer.setChecked(bean.isEnable());
            holderView.prefer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    bean.setEnable(b);
                }
            });
            if (b){
                view.setBackgroundResource(R.drawable.item_selector_bottom);
            }else {
                view.setBackgroundResource(R.drawable.item_selector_none);
            }
            return view;
        }

        return view;
    }

    @Override
    public Object getGroup(int i) {
        return parentItems.get(i);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }
    public interface OnFinishListener{
        void onFinish();
    }
    @Override
    public int getGroupCount() {
        return parentItems.size();
    }

}
