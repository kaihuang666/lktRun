package com.kai.lktMode.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kai.lktMode.activity.MainActivity;

public class MyFragment extends Fragment {
    private LocalBroadcastManager localBroadcastManager;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        localBroadcastManager= LocalBroadcastManager.getInstance(getContext());
    }

    public MainActivity getMain(){
        MainActivity mainActivity=(MainActivity) getActivity();
        return mainActivity;
    }
    public void Refresh(){

    }

}
