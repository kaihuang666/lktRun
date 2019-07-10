package com.kai.lktMode;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class ViewPagerSlide extends ViewPager {

    //是否可以进行滑动
    private boolean isSlide = false;

    public void setSlide(boolean slide) {
        this.isSlide = slide;
    }

    public ViewPagerSlide(Context context) {
        super(context);
    }

    public ViewPagerSlide(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSlide) {
            return isSlide;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (!isSlide){
            return false;
        }else{
            return super.onTouchEvent(arg0);
        }
    }

}

