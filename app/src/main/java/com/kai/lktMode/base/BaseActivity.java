package com.kai.lktMode.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.kai.lktMode.crash.CrashHandler;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler.getInstance().init(getApplicationContext());

    }
    public void messageToActivity(int i){
        Intent intent=new Intent("com.kai.lktMode.Main");

    }
}
