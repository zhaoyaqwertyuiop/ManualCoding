package com.manualcoding.manualcoding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhaoya on 2016/12/8.
 */
public class BitmapFileUtil {

    String filePath="c:/01.jpg";
    // file转bitmap
    Bitmap bitmap = BitmapFactory.decodeFile(filePath,getBitmapOption(2)); //将图片的长和宽缩小为原来的1/2

    private BitmapFactory.Options getBitmapOption(int inSampleSize){
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }

    /** 把Bitmap对象保存为图片文件 */
    public boolean saveBitmapFile(File file, Bitmap bitmap) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 将两张图片合并成一张图片
    public Bitmap mergeBitmap(Bitmap firstBitmap, Bitmap secondBitmap) {
        Bitmap bitmap = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(),
                firstBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(firstBitmap, new Matrix(), null);
        int x = (firstBitmap.getWidth() - secondBitmap.getWidth()) / 2;
        int y = (firstBitmap.getHeight() - secondBitmap.getHeight()) / 2;
        canvas.drawBitmap(secondBitmap, x, y, null);
        return bitmap;
    }

}
