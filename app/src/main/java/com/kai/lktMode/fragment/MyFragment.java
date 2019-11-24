package com.kai.lktMode.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kai.lktMode.activity.MainActivity;

public class MyFragment extends Fragment {
    private LocalBroadcastManager localBroadcastManager;
    private Context context;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Nullable
    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        localBroadcastManager= LocalBroadcastManager.getInstance(getContext());
    }

    public MainActivity getMain(){
        return (MainActivity)getActivity();
    }
    public void Refresh(){

    }
    public boolean isPassed(){
        return false;
    }
    public void next(){

    }

}
