package com.qudian.webChrome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.text.SimpleDateFormat;


public class ImageUtil {

    private static final String TAG = "ImageUtil";


    /**
     * go for Album.
     */
    public static final Intent choosePicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        return Intent.createChooser(intent, null);
    }

    /**
     * go for camera.
     */
    public static final Intent takeBigPicture(Context context) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newPictureUri(context, getNewPhotoPath()));
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

    public static final String getDirPath() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/UploadImage");
        if (!file.exists()) {
            file.mkdirs();
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/UploadImage";
    }

    private static final String getNewPhotoPath() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
        return getDirPath() + "/" + timeStamp + ".jpg";
    }

    public static final String retrievePath(Context context, Intent sourceIntent, Intent dataIntent) {
        String picPath = null;
        try {
            Uri uri;
            if (dataIntent != null) {
                uri = dataIntent.getData();
                if (uri != null) {
                    picPath = ContentUtil.getPath(context, uri);
                }
                if (isFileExists(picPath)) {
                    return picPath;
                }

                Log.w(TAG, String.format("retrievePath failed from dataIntent:%s, extras:%s", dataIntent, dataIntent.getExtras()));
            }

            if (sourceIntent != null) {
                uri = sourceIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                if (uri != null) {
                    String scheme = uri.getScheme();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (scheme != null && scheme.startsWith("content")) {
                            picPath = getDirPath() + "/" + ContentUtil.getFileNameWithSuffix(uri.getPath());
                        }
                    } else {
                        if (scheme != null && scheme.startsWith("file")) {
                            picPath = uri.getPath();
                        }
                    }
                }
                if (!TextUtils.isEmpty(picPath)) {
                    File file = new File(picPath);
                    if (!file.exists() || !file.isFile()) {
                        Log.w(TAG, String.format("retrievePath file not found from sourceIntent path:%s", picPath));
                    }
                }
            }
            return picPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            Log.d(TAG, "retrievePath(" + sourceIntent + "," + dataIntent + ") ret: " + picPath);
        }
    }

    private static final Uri newPictureUri(Context context, String path) {
        return AndPermission.getFileUri(context, new File(path));
    }

    private static final boolean isFileExists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File f = new File(path);
        return f.exists();
    }
}
