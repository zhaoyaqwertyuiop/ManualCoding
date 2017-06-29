package com.pinchimageview;

import android.graphics.Point;
import android.graphics.RectF;
import android.widget.ImageView;

/**
 * Created by zhaoya on 2016/10/8.
 */
public class MyImageSource implements ImageSource {

    private String path;
    private int mOriginWidth;
    private int mOriginHeight;

    public MyImageSource(String path, int originWidth, int originHeight){
        this.path = path;
        this.mOriginWidth = originWidth;
        this.mOriginHeight = originHeight;
    }
    @Override
    public String getUrl(int width, int height) {
        return path;
    }

    @Override
    public Point getSize(int requestWidth, int requestHeight) {
        RectF container = new RectF(0 ,0, requestWidth, requestHeight);
        RectF rectResult = new RectF();
        PinchImageView.MathUtils.calculateScaledRectInContainer(container, mOriginWidth, mOriginHeight, ImageView.ScaleType.FIT_CENTER, rectResult);
        return new Point(Math.round(rectResult.width()), Math.round(rectResult.height()));
    }

    @Override
    public int getOriginWidth() {
        return 0;
    }

    @Override
    public int getOriginHeight() {
        return 0;
    }
}
