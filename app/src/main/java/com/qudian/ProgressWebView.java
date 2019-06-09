package com.qudian;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qudian.webChrome.CustomChromeClient;

import static com.qudian.webChrome.CustomChromeClient.REQUEST_CODE_IMAGE_CAPTURE;
import static com.qudian.webChrome.CustomChromeClient.REQUEST_CODE_PICK_IMAGE;
import static com.qudian.webChrome.CustomChromeClient.REQUEST_CODE_VIDEO_CAPTURE;


@SuppressLint("NewApi")
public class ProgressWebView extends WebView {
    private static final String TAG = "ProgressWebView";
    public boolean isStartLoadingNewWebview = false;
    public String tempUrl;
    public String mLastFinishedUrl;
    private CustomChromeClient customChromeClient;

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void initWebView(final Activity activity) {
        customChromeClient = new CustomChromeClient(activity, this);
        setWebChromeClient(customChromeClient);
        WebSettings webSettings = getSettings();

        webSettings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setAppCacheEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setTextZoom(100);

        setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (customChromeClient != null) {
                    customChromeClient.isStartLoadingNewWebview = true;
                }
                isStartLoadingNewWebview = true;
                super.onPageStarted(view, url, favicon);

            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (URLUtil.isNetworkUrl(url)) {
                    mLastFinishedUrl = url;
                }
                if (customChromeClient != null) {
                    customChromeClient.isStartLoadingNewWebview = false;
                }
                isStartLoadingNewWebview = false;
                if (onWebViewInterfaceListener != null) {
                    onWebViewInterfaceListener.onPageFinished(view, url);
                }
            }
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return super.shouldInterceptRequest(view,url);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    } catch (Exception e){
                        Log.e(TAG,"WebView start activity fail",e);
                    }

                    return true;
                }

                tempUrl = url;
                if (onWebViewOverrideUrlLoadingListener != null) {
                    return onWebViewOverrideUrlLoadingListener.shouldOverrideUrlLoading(view, url);
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (onWebViewInterfaceListener != null) {
                    onWebViewInterfaceListener.onReceivedError(view, errorCode, description, failingUrl);
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
    }


    public OnWebViewInterfaceListener onWebViewInterfaceListener;
    public OnWebViewOverrideUrlLoadingListener onWebViewOverrideUrlLoadingListener;

    public interface OnWebViewInterfaceListener {
        void onReceivedTitle(WebView view, String title);

        void onPageFinished(WebView view, String url);

        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);
    }

    public interface OnWebViewOverrideUrlLoadingListener {
        boolean shouldOverrideUrlLoading(WebView view, String url);
    }


    public void setOnWebViewOverrideUrlLoadingListener(OnWebViewOverrideUrlLoadingListener onWebViewOverrideUrlLoadingListener) {
        this.onWebViewOverrideUrlLoadingListener = onWebViewOverrideUrlLoadingListener;
    }


    public void setOnWebViewInterfaceListener(OnWebViewInterfaceListener onWebViewInterfaceListener) {
        this.onWebViewInterfaceListener = onWebViewInterfaceListener;
    }


    public void onActivityResult(int requestCode, int resultCode, final Intent data, final Activity activity) {

        if (requestCode == REQUEST_CODE_PICK_IMAGE
                || requestCode == REQUEST_CODE_IMAGE_CAPTURE
                || requestCode == REQUEST_CODE_VIDEO_CAPTURE) {
            customChromeClient.onActivityResult(requestCode, resultCode, data);
        }
    }


}