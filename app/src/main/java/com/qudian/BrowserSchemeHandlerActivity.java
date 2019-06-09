package com.qudian;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;

public class BrowserSchemeHandlerActivity extends Activity {
    private static final String TAG = "BrowserScheme--->";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));
        Uri uri = getIntent() == null ? null : getIntent().getData();

        //h5落地页且当前已正常打开，直接唤起App
        String url = uri == null ? null : uri.getQueryParameter("url");
        if (TextUtils.isEmpty(url) || URLUtil.isNetworkUrl(url)) {
            WebViewActivity.launch(this, url);
        }
        finish();
    }
}