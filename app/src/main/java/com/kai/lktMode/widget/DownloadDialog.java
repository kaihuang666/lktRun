package com.kai.lktMode.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kai.lktMode.R;
import com.kai.lktMode.tool.util.net.DownloadUtil;
import com.kai.lktMode.tool.util.net.WebUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DownloadDialog extends AlertDialog {
    String dic;
    TextView message;
    String url;
    ProgressBar progress;
    TextView progress_int;
    TextView progress_float;
    private Activity context;
    private OnTaskSuccess onTaskSuccess=null;
    private OnTaskFail onTaskFail=null;
    private String name;
    private Boolean showProgress=true;
    public DownloadDialog(Activity context, String dic,String url,String name,boolean showProgress) {
        super(context,R.style.AppDialog);
        this.context=context;
        this.dic=dic;
        this.url=url;
        this.name=name;
        this.showProgress=showProgress;
    }

    public void setOnTaskSuccess(OnTaskSuccess onTaskSuccess) {
        this.onTaskSuccess = onTaskSuccess;
    }

    public void setOnTaskFail(OnTaskFail onTaskFail) {
        this.onTaskFail = onTaskFail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        View view;
        if (showProgress){
           view =View.inflate(context, R.layout.dialog_progress,null);
        }else {
            view =View.inflate(context, R.layout.dialog_progress_none,null);
        }
        progress=view.findViewById(R.id.progress);
        progress_int=view.findViewById(R.id.progress_int);
        progress_float=view.findViewById(R.id.progress_float);
        setContentView(view);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String realUrl= WebUtil.getRealUrl(url);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("realUrl",realUrl);
                        initWebview(view,realUrl);
                    }
                });
            }
        }).start();
    }
    private void initWebview(View view,String url){
        WebView webView = (WebView) view.findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        // 设置WebView支持JavaScript
        settings.setJavaScriptEnabled(true);
        //支持自动适配
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBlockNetworkImage(true);// 把图片加载放在最后来加载渲染
        settings.setAllowFileAccess(true); // 允许访问文件
        settings.setSaveFormData(true);
        settings.setGeolocationEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //设置不让其跳转浏览器
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

        });

        // 添加客户端支持
        webView.setWebChromeClient(new WebChromeClient(){

        });
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DownloadUtil.get().download(url, dic, name, new DownloadUtil.OnDownloadListener() {
                            @Override
                            public void onDownloadSuccess(File file) {
                                dismiss();
                                if (onTaskSuccess==null)
                                    return;
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onTaskSuccess.onSuccess();
                                    }
                                });
                            }

                            @Override
                            public void onDownloading(int progress) {

                                DownloadDialog.this.progress.setProgress(progress);
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress_int.setText(String.valueOf(progress));
                                        progress_float.setText(String.valueOf(progress));
                                    }
                                });
                            }

                            @Override
                            public void onDownloadFailed(Exception e) {
                                dismiss();
                                if (onTaskFail==null)
                                    return;
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onTaskFail.onFail();
                                    }
                                });
                            }
                        });

                    }
                }).start();
            }
        });
//        mWebView.loadUrl(TEXTURL);

        //不加这个图片显示不出来
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            webView.getSettings().setBlockNetworkImage(false);

//允许cookie 不然有的网站无法登陆
            CookieManager mCookieManager = CookieManager.getInstance();
            mCookieManager.setAcceptCookie(true);
            mCookieManager.setAcceptThirdPartyCookies(webView, true);
        }
        Map<String,String> map=new HashMap<>();
        map.put("User-Agent","Mozilla/5.0 (Android 4.4; Mobile; rv:41.0) Gecko/41.0 Firefox/41.0");
        webView.loadUrl(url,map);
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }
    public interface OnTaskSuccess{
        void onSuccess();
    }
    public interface OnTaskFail{
        void onFail();
    }
    public static class Builder{
        private Activity activity;
        private String url;
        private String parent;
        private boolean enable;
        private String target;
        public Builder(Activity activity){
            this.activity=activity;
        }
        public Builder setDownloadUrl(String url){
            this.url=url;
            return this;
        }
        public Builder setProgressEnable(boolean enable){
            this.enable=enable;
            return this;
        }
        public Builder setParentDic(String parent){
            this.parent=parent;
            return this;
        }
        public Builder setFileName(String target){
            this.target=target;
            return this;
        }
        public DownloadDialog build(){
            return new DownloadDialog(activity,parent,url,target,enable);
        }
    }
}
