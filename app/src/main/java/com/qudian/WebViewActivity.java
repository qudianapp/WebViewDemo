package com.qudian;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {
    public static final String DEFAULT_URL = "file:///android_asset/test.html";
    // public static final String DEFAULT_URL = "http://lfqshop.lfqstandard.test3.qudian.com/v3/union/loan?_unionApp=changba";
    public static void launch(Context context, String url) {
        try {
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra("url", url);
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }
    ProgressWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        mWebView = findViewById(R.id.webview);
        String url = getIntent().getStringExtra("url");
        // 加载的链接 放到onCreate中 deepLink进来不会重新加载
        mWebView.loadUrl(url == null ? DEFAULT_URL : url);
        // 初始化ProgressWebView
        mWebView.initWebView(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = intent.getStringExtra("url");
        if(mWebView != null){
            mWebView.loadUrl(url == null ? DEFAULT_URL : url);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mWebView.onActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mWebView.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
