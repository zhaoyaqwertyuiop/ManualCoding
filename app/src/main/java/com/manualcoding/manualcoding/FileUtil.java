package com.manualcoding.manualcoding;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by zhaoya on 2016/12/30.
 */
public class FileUtil {

    /**
     * 检查sd卡是否可用
     *
     * @return
     */
    public static boolean checkSDCardAvaliable() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_REMOVED) {
            return false;
        }
        return true;
    }

    /**
     * 如果SD卡可用,创建文件在SK卡
     * @param fileSuffix 文件后缀名
     * @return
     * @throws IOException
     */
    private static File createMediaFile(Context context, String fileSuffix) throws IOException {
        if (checkSDCardAvaliable()) {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getPackageName());
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            // Create an image file name
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = "PIC_" + timeStamp;
            String fileName = UUID.randomUUID().toString().replace("-", "");
            File mediaFile = new File(mediaStorageDir + File.separator + fileName + fileSuffix);
            return mediaFile;
        }
        return null;
    }

    /**
     * 根据当前时间生成文件,如果有sd卡则优先存储在sd卡,没有则内部存储
     * @param fileSuffix 文件后缀名
     * @return
     */
    public static final File createNewFile(Context context, String fileSuffix) {
        File file = null;
        try { // 在SK卡创建文件
            file = createMediaFile(context, fileSuffix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file == null) { // SK卡创建失败, 则内部存储创建文件
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = "PIC_" + timeStamp;
            String fileName = UUID.randomUUID().toString().replace("-", "");
            file = new File(context.getFilesDir() + File.separator + fileName + fileSuffix);
        }
        return file;
    }

    /** 获取文件后缀名 */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /** 获取不带后缀名的文件名  */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}
