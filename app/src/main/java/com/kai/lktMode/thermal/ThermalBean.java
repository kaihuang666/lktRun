package com.kai.lktMode.thermal;

public class ThermalBean {
    String name;
    boolean enable;
    EnableClick click;
    public ThermalBean(String name,boolean enable,EnableClick click){
        this.enable=enable;
        this.name=name;
        this.click=click;
    }


    public String getName() {
        return name;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
        if (click!=null)
            click.click(enable);
    }

    public interface EnableClick{
        void click(Boolean b);
    }
}
