package com.pksh.myutils.ordinary;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 文件工具类
 * 曹丽超
 * 2020/12/04
 */
public class FileFormat {
    public static String checkPathMessage = null;
    private static final String TAG = "本地文件解析";
    /**
     * 格式化文件大小
     *
     * @param length 文件大小(以Byte为单位)
     * @return String 格式化的常见文件大小(保留两位小数)
     */
    public static String formatFileSize(long length) {
        String result;
        int subString;

        if (length >= 1073741824) { // 如果文件长度大于1GB
            subString = String.valueOf((float) length / 1073741824).indexOf(".");
            result = ((float) length / 1073741824 + "000").substring(0, subString + 3) + "GB";
        } else if (length >= 1048576) { // 如果文件长度大于1MB且小于1GB
            subString = String.valueOf((float) length / 1048576).indexOf(".");
            result = ((float) length / 1048576 + "000").substring(0, subString + 3) + "MB";
        } else if (length >= 1024) { // 如果文件长度大于1KB且小于1MB
            subString = String.valueOf((float) length / 1024).indexOf(".");
            result = ((float) length / 1024 + "000").substring(0, subString + 3) + "KB";
        } else
            result = length + "B";
        return result;
    }

    /**
     * 排序文件列表
     * @param fileList 文件list列表
     */
    public static void sortFileList(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
    }

    /**
     * 检查文件是否存在
     * @return 布尔值，文件是否存在
     */
    public static boolean checkFileIsExist(String filePath){
        File file1 = new File(filePath);
        if (!file1.exists()) {
            return false;
        }
        return true;
    }

    /**
     * 获取文件大小
     * @return 文件大小long类型
     */
    public static long getFileLength(String filePath){
        File file1 = new File(filePath);
        if (!file1.exists()) {
            return filePath.length();
        }
        return 0;
    }

    /**
     * 本地文件路径解析类（最好是先调用getPath（）获取解析后的文件地址，然后调用checkFileIsExist（）检查解析地址是否正确）
     * 返回文件本地绝对路径
     * @param context 上下文
     * @param uri uri
     * @return 文件路径
     */
    @SuppressLint("NewApi")
    public static String getLocalFileAddress(final Context context, final Uri uri) {
        // 系统版本是否是4.4
        @SuppressLint("ObsoleteSdkInt")
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // uri解析
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            Log.d(TAG, "文件格式是isKitKat: ");
            // 外部存储文件解析
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // 下载文件解析
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // 媒体文件解析
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // 媒体存储和常规文件解析
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Log.d(TAG, "文件格式是content");
            // Return the remote address
            if ("com.google.android.apps.photos.content".equals(uri
                    .getAuthority()))
                return uri.getLastPathSegment();
            if ("com.tencent.mtt.fileprovider".equals(uri
                    .getAuthority()))
                return uri.getPath();

            return getDataColumn(context, uri, null, null);
        }
        // 文件
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        Log.d(TAG, "文件格式都不是,未返回文件地址:");
        return null;
    }

    /**
     * 媒体文件uri解析
     * @param context 上下文
     * @param uri 选择文件的uri
     * @param selection 筛选器
     * @param selectionArgs 选择参数，可以为空
     * @return 解析值，一般情况是返回路径
     */
    private static String getDataColumn(Context context, Uri uri,
                                        String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /** 外部存储文件解析
     * @param uri uri
     * @return 返回是否是外部存储文件
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return 是否下载文档
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri Uri
     * @return 是否是媒体文件
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }
}
