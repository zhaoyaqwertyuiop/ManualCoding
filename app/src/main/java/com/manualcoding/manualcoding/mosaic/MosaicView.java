package com.manualcoding.manualcoding.mosaic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.pinchimageview.PinchImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoya on 2017/5/10.
 * 手动给图片打马赛克view
 */

public class MosaicView extends PinchImageView {

    private Bitmap mBitmap;
    private final int BLOCK_SIZE = 15; // 马赛克的大小: BLOCK_SIZE*BLOCK_SIZE
    private int[] mSampleColors;
    private float mLastX, mLastY;
    private int mBitmapWidth, mBitmapHeight;
    private int[] mSrcBitmapPixs; // 保留原图的像素数组
    private int[] mTempBitmapPixs; // 用于马赛克的临时像素数组
    private int mRowCount, mColumnCount;
    private final int VALID_DISTANCE = 4; // 滑动的有效距离

    private RectF rectF;
    private boolean isMasking = false; // 正在打码,默认为false

    public MosaicView(Context context) {
        super(context);
    }

    public MosaicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MosaicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        Drawable drawable = super.getDrawable();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        mBitmap = bitmap;
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);

        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        mRowCount = (int) Math.ceil((float) mBitmapHeight / BLOCK_SIZE);
        mColumnCount = (int) Math.ceil((float) mBitmapWidth / BLOCK_SIZE);
        mSampleColors = new int[mRowCount * mColumnCount];

        int maxX = mBitmapWidth - 1;
        int maxY = mBitmapHeight - 1;
        mSrcBitmapPixs = new int[mBitmapWidth * mBitmapHeight];
        mTempBitmapPixs = new int[mBitmapWidth * mBitmapHeight];
        mBitmap.getPixels(mSrcBitmapPixs, 0, mBitmapWidth, 0, 0, mBitmapWidth, mBitmapHeight);
        mBitmap.getPixels(mTempBitmapPixs, 0, mBitmapWidth, 0, 0, mBitmapWidth, mBitmapHeight);
        for (int row = 0; row < mRowCount; row++) {
            for (int column = 0; column < mColumnCount; column++) {
                int startX = column * BLOCK_SIZE;
                int startY = row * BLOCK_SIZE;
                mSampleColors[row * mColumnCount + column] = sampleBlock(mSrcBitmapPixs, startX, startY, BLOCK_SIZE, maxX, maxY);
            }
        }
        mBitmap.setPixels(mSrcBitmapPixs, 0, mBitmapWidth, 0, 0, mBitmapWidth, mBitmapHeight);
    }

    private int sampleBlock(int[] pxs, int startX, int startY, int blockSize, int maxX, int maxY) {
        int stopX = startX + blockSize - 1;
        int stopY = startY + blockSize - 1;
        stopX = Math.min(stopX, maxX);
        stopY = Math.min(stopY, maxY);
        int sampleColor = 0;
        int red = 0;
        int green = 0;
        int blue = 0;
        //将该块的所有点的颜色求平均值
        for (int y = startY; y <= stopY; y++) {
            int p = y * mBitmapWidth;
            for (int x = startX; x <= stopX; x++) {
                int color = pxs[p + x];
                red += Color.red(color);
                green += Color.green(color);
                blue += Color.blue(color);
            }
        }
        int sampleCount = (stopY - startY + 1) * (stopX - startX + 1);
        Log.d("sampleCount=", sampleCount + "");
        red /= sampleCount;
        green /= sampleCount;
        blue /= sampleCount;
        sampleColor = Color.rgb(red, green, blue);
        return sampleColor;
    }

    private void touchStart(float x, float y) {
        mLastX = x;
        mLastY = y;
    }

    private void touchMove(float x, float y) {
        if (Math.abs(x - mLastX) >= VALID_DISTANCE || Math.abs(y - mLastY) >= VALID_DISTANCE) {
            Point startPoint = new Point(mLastX, mLastY);
            Point endPoint = new Point(x, y);
            mosaic(startPoint, endPoint);
        }
        mLastX = x;
        mLastY = y;
    }

    private void mosaic(Point startPoint, Point endPoint) {
        float startTouchX = startPoint.x;
        float startTouchY = startPoint.y;

        float endTouchX = endPoint.x;
        float endTouchY = endPoint.y;

        float minX = Math.min(startTouchX, endTouchX);
        float maxX = Math.max(startTouchX, endTouchX);

        int startIndexX = (int) minX / BLOCK_SIZE;
        int endIndexX = (int) maxX / BLOCK_SIZE;

        float minY = Math.min(startTouchY, endTouchY);
        float maxY = Math.max(startTouchY, endTouchY);

        int startIndexY = (int) minY / BLOCK_SIZE;
        int endIndexY = (int) maxY / BLOCK_SIZE;//确定矩形的判断范围
        if (startIndexX < 0 || startIndexY < 0 || endIndexY < 0 || endIndexY < 0) {
            return;
        }
        for (int row = startIndexY; row <= endIndexY; row++) {
            for (int colunm = startIndexX; colunm <= endIndexX; colunm++) {
                Rect rect = new Rect(colunm * BLOCK_SIZE, row * BLOCK_SIZE, (colunm + 1) * BLOCK_SIZE, (row + 1) * BLOCK_SIZE);
                Boolean intersectRect = GeometryHelper.IsLineIntersectRect(startPoint.clone(), endPoint.clone(), rect);
                if (intersectRect) {//线段与直线相交
                    int rowMax = Math.min((row + 1) * BLOCK_SIZE, mBitmapHeight);
                    int colunmMax = Math.min((colunm + 1) * BLOCK_SIZE, mBitmapWidth);
                    for (int i = row * BLOCK_SIZE; i < rowMax; i++) {
                        for (int j = colunm * BLOCK_SIZE; j < colunmMax; j++) {
                            mTempBitmapPixs[i * mBitmapWidth + j] = mSampleColors[row * mColumnCount + colunm];
                        }
                    }
                }
            }
        }
        mBitmap.setPixels(mTempBitmapPixs, 0, mBitmapWidth, 0, 0, mBitmapWidth, mBitmapHeight);
    }

    public void setMasking(boolean masking) {
        isMasking = masking;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rectF = getImageBound(rectF);
        canvas.clipRect(rectF);
        if (mBitmap == null) {
            init();
        }
        canvas.drawBitmap(mBitmap, null, rectF, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isMasking) {
            return super.onTouchEvent(event);
        }
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action ==  MotionEvent.ACTION_DOWN) { // 按下,记录currMask
            touchStart(getPathX(Math.abs(x)), getPathY(Math.abs(y)));
            invalidate();
            return super.onTouchEvent(event);
        } else if (action == MotionEvent.ACTION_MOVE && getPinchMode() == PINCH_MODE_SCROLL) { // 移动
            touchMove(getPathX(Math.abs(x)), getPathY(Math.abs(y)));
            invalidate();
            return true;
        } else if (action == MotionEvent.ACTION_POINTER_DOWN && getPinchMode() == PINCH_MODE_SCROLL) { // 多点按下,不打码
            return super.onTouchEvent(event);
        } else if (action == MotionEvent.ACTION_UP && getPinchMode() == PINCH_MODE_SCROLL) { // 抬起,把currMask加入到masks
            return super.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    /**
     * 清除全部打码
     */
    public void clean() {
        Drawable drawable = super.getDrawable();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        mBitmap = bitmap;
        invalidate();
    }

    /**
     * 得到打码后的bitmap
     */
    public Bitmap getMaskedBitmap() {
        return mBitmap;
    }

    /** 图片缩放比例 */
    private float getScale() {
        rectF = super.getImageBound(rectF);
        return getDrawable().getIntrinsicWidth() / rectF.width();
    }

    /** 屏幕坐标转bitmap坐标 */
    private float getPathX(float x) {
        rectF = super.getImageBound(rectF);
        return (x - rectF.left) * (getDrawable().getIntrinsicWidth() / rectF.width());
    }

    /** 屏幕坐标转bitmap坐标 */
    private float getPathY(float y) {
        return (y - rectF.top) * (getDrawable().getIntrinsicHeight() / rectF.height());
    }

    /** bitmap坐标转屏幕坐标 */
    private float getViewX(float x) {
        rectF = super.getImageBound(rectF);
        return x * (rectF.width() / getDrawable().getIntrinsicWidth()) + rectF.left;
    }

    /** bitmap坐标转屏幕坐标 */
    private float getViewY(float y) {
        return y * (rectF.height() / getDrawable().getIntrinsicHeight()) + rectF.top;
    }
}
