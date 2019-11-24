package com.kai.lktMode.bean;

public class ParentItem extends Object{
    private String title;
    private Object object;
    private int type;
    public ParentItem(String title,Object object,int type){
        this.object=object;
        this.title=title;
        this.type=type;
    }

    public String getTitle() {
        return title;
    }

    public Object getObject() {
        return object;
    }

    public int getType() {
        return type;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
