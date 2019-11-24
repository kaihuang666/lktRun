package com.kai.lktMode.activity;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.kai.lktMode.R;
import com.kai.lktMode.base.BaseActivity;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar=(Toolbar)findViewById(R.id.simple_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
