package com.kai.lktMode.fragment;

import androidx.fragment.app.Fragment;

public class MyFragment extends Fragment {
    public void setToolbar(OnToolbarChange toolbar){

    }
    public interface OnToolbarChange{
        void onchange(String title,String subtitle);

    }
}
