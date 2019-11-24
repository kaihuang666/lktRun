package com.kai.lktMode.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kai.lktMode.R;
import com.kai.lktMode.base.BaseActivity;
import com.kai.lktMode.tool.util.net.DownloadUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends BaseActivity {
    private boolean first=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);


    }
}
