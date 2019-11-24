package com.kai.lktMode.bean;

import android.graphics.drawable.Drawable;

public class App {
    String name;
    Drawable icon;
    boolean isChecked;
    String package_name;
    public App(String name,String package_name,Drawable icon,boolean isChecked){
        this.name=name;
        this.icon=icon;
        this.package_name=package_name;
        this.isChecked=isChecked;
    }

    public String getName() {
        return name;
    }

    public String getPackage_name() {
        return package_name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
