package com.qudian.webChrome;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.qudian.ProgressWebView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CustomChromeClient extends WebChromeClient {

    private OpenFileChooserCallBack mOpenFileChooserCallBack;
    private static final String TAG = CustomChromeClient.class.getSimpleName();
    public static final int REQUEST_CODE_PICK_IMAGE = 10003;
    public static final int REQUEST_CODE_IMAGE_CAPTURE = 10004;
    public static final int REQUEST_CODE_VIDEO_CAPTURE = 10005;
    // permission Code
    private static final int P_CODE_PERMISSIONS = 101;

    private Intent mSourceIntent;
    private ValueCallback<Uri> mUploadMsg;
    public ValueCallback<Uri[]> mUploadMsgForAndroid5;
    private Activity mContext;

    public boolean isStartLoadingNewWebview = false;
    private WebView webView;

    public CustomChromeClient(Activity context, WebView webView) {
        mContext = context;
        this.webView = webView;
        mOpenFileChooserCallBack = new OpenFileChooserCallBack() {
            @Override
            public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
                showOptions();
                mUploadMsg = uploadMsg;
            }

            @Override
            public boolean openFileChooserCallBackAndroid5(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                showOptions();
                mUploadMsgForAndroid5 = filePathCallback;
                return true;
            }
        };
    }


    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        try {
            if (((ProgressWebView) webView).onWebViewInterfaceListener != null) {
                ((ProgressWebView) webView).onWebViewInterfaceListener.onReceivedTitle(view, title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {

        super.onProgressChanged(view, newProgress);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        mOpenFileChooserCallBack.openFileChooserCallBack(uploadMsg, acceptType);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "");
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        openFileChooser(uploadMsg, acceptType);
    }

    /**
     * for android 5.0+
     * @param webView
     * @param filePathCallback
     * @param fileChooserParams
     * @return
     */
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        return mOpenFileChooserCallBack.openFileChooserCallBackAndroid5(webView, filePathCallback, fileChooserParams);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (mUploadMsg != null) {
                mUploadMsg.onReceiveValue(null);
            }

            if (mUploadMsgForAndroid5 != null) {         // for android 5.0+
                mUploadMsgForAndroid5.onReceiveValue(null);
            }
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_IMAGE_CAPTURE:
            case REQUEST_CODE_PICK_IMAGE:
            case REQUEST_CODE_VIDEO_CAPTURE: {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        if (mUploadMsg == null) {
                            return;
                        }

                        String sourcePath = ImageUtil.retrievePath(mContext, mSourceIntent, data);

                        if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
                            break;
                        }
                        Uri uri = Uri.fromFile(new File(sourcePath));
                        mUploadMsg.onReceiveValue(uri);

                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (mUploadMsgForAndroid5 == null) {        // for android 5.0+
                            return;
                        }
                        String sourcePath = ImageUtil.retrievePath(mContext, mSourceIntent, data);
                        if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
                            if (mUploadMsgForAndroid5 != null) {         // for android 5.0+
                                mUploadMsgForAndroid5.onReceiveValue(null);
                            }
                            return;
                        }
                        Uri uri = Uri.fromFile(new File(sourcePath));
                        mUploadMsgForAndroid5.onReceiveValue(new Uri[]{uri});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }


    /**
     * 根据自己业务去做自定义
     */
    public void showOptions() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setOnCancelListener(new DialogOnCancelListener());
        alertDialog.setTitle("请选择操作");
        // gallery, camera.
        String[] options = {"相册", "拍照", "录像(人脸识别测试)"};
        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    Toast.makeText(mContext,
                                            "请去\"设置\"中开启本应用的图片媒体访问权限",
                                            Toast.LENGTH_SHORT).show();

                                    restoreUploadMsg();
                                    requestPermissionsAndroidM();
                                    return;
                                }

                            }

                            try {
                                mSourceIntent = ImageUtil.choosePicture();
                                mContext.startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(mContext,
                                        "请去\"设置\"中开启本应用的图片媒体访问权限",
                                        Toast.LENGTH_SHORT).show();
                                restoreUploadMsg();
                            }

                        } else if (which == 1) {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    Toast.makeText(mContext,
                                            "请去\"设置\"中开启本应用的图片媒体访问权限",
                                            Toast.LENGTH_SHORT).show();

                                    restoreUploadMsg();
                                    requestPermissionsAndroidM();
                                    return;
                                }

                                if (!PermissionUtil.isPermissionValid(mContext, Manifest.permission.CAMERA)) {
                                    Toast.makeText(mContext,
                                            "请去\"设置\"中开启本应用的相机权限",
                                            Toast.LENGTH_SHORT).show();

                                    restoreUploadMsg();
                                    requestPermissionsAndroidM();
                                    return;
                                }
                            }

                            try {
                                mSourceIntent = ImageUtil.takeBigPicture(mContext);
                                mContext.startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);
                            } catch (Exception e) {
                                Log.w("woshangdan", "onClick: e === " + e.getMessage());
                                e.printStackTrace();
                                Toast.makeText(mContext,
                                        "请去\"设置\"中开启本应用的相机和图片媒体访问权限",
                                        Toast.LENGTH_SHORT).show();
                                restoreUploadMsg();
                            }
                        } else if (which == 2) {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    Toast.makeText(mContext,
                                            "请去\"设置\"中开启本应用的图片媒体访问权限",
                                            Toast.LENGTH_SHORT).show();

                                    restoreUploadMsg();
                                    requestPermissionsAndroidM();
                                    return;
                                }

                                if (!PermissionUtil.isPermissionValid(mContext, Manifest.permission.CAMERA)) {
                                    Toast.makeText(mContext,
                                            "请去\"设置\"中开启本应用的相机权限",
                                            Toast.LENGTH_SHORT).show();

                                    restoreUploadMsg();
                                    requestPermissionsAndroidM();
                                    return;
                                }
                            }

                            try {
                                mSourceIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                mSourceIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                                mContext.startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);
                            } catch (Exception e) {
                                Log.w("woshangdan", "onClick: e === " + e.getMessage());
                                e.printStackTrace();
                                Toast.makeText(mContext,
                                        "请去\"设置\"中开启本应用的相机和图片媒体访问权限",
                                        Toast.LENGTH_SHORT).show();
                                restoreUploadMsg();
                            }
                        }
                    }
                }
        );

        alertDialog.show();
    }

    private void requestPermissionsAndroidM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needPermissionList = new ArrayList<>();
            needPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.CAMERA);
            PermissionUtil.requestPermissions(mContext, P_CODE_PERMISSIONS, needPermissionList);
        } else {
            return;
        }
    }

    private void restoreUploadMsg() {
        if (mUploadMsg != null) {
            mUploadMsg.onReceiveValue(null);
            mUploadMsg = null;

        } else if (mUploadMsgForAndroid5 != null) {
            mUploadMsgForAndroid5.onReceiveValue(null);
            mUploadMsgForAndroid5 = null;
        }
    }


    private class DialogOnCancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            restoreUploadMsg();
        }
    }


    public interface OpenFileChooserCallBack {
        void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType);

        boolean openFileChooserCallBackAndroid5(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                                FileChooserParams fileChooserParams);
    }

}
