package com.manualcoding.manualcoding.mosaic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.pinchimageview.PinchImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoya on 2017/5/10.
 * 手动给图片打马赛克view
 */

public class MosaicView extends PinchImageView {

    private Bitmap mBitmap;
    private final int BLOCK_SIZE = 30; // 马赛克的大小: BLOCK_SIZE*BLOCK_SIZE
    private int[] mSampleColors;
    private float mLastX, mLastY;
    private int mBitmapWidth, mBitmapHeight;
    private int[] mSrcBitmapPixs; // 保留原图的像素数组
    private int[] mTempBitmapPixs; // 用于马赛克的临时像素数组
    private int mRowCount, mColumnCount;
    private final int VALID_DISTANCE = 4; // 滑动的有效距离

    private RectF rectF;
    private boolean isMasking = false; // 正在打码,默认为false

    private List<Mask> maskList = new ArrayList<>();
    private Mask currMask;
    private boolean canEdit = true; // 是否可以允许打码,默认允许

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public MosaicView(Context context) {
        super(context);
    }

    public MosaicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MosaicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        init(); // 把init写在这里就可以支持网路图片
    }

    private void init() {
        Drawable drawable = super.getDrawable();
        if (drawable == null) {
            return;
        }
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


        for (Mask mask : maskList) {
            if (mask.points != null) {
                for (Point point: mask.points) {
                    touchMove(point.x, point.y, false);
                }
            }
        }
        mBitmap.setPixels(mTempBitmapPixs, 0, mBitmapWidth, 0, 0, mBitmapWidth, mBitmapHeight);
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

    private void touchMove(float x, float y, boolean updateBitmap) {
        if (Math.abs(x - mLastX) >= VALID_DISTANCE || Math.abs(y - mLastY) >= VALID_DISTANCE) {
            Point startPoint = new Point(mLastX, mLastY);
            Point endPoint = new Point(x, y);
            mosaic(startPoint, endPoint, updateBitmap);
        }
        mLastX = x;
        mLastY = y;
    }

//    private void mosaic(Point startPoint, Point endPoint) {
//        float startTouchX = startPoint.x;
//        float startTouchY = startPoint.y;
//
//        float endTouchX = endPoint.x;
//        float endTouchY = endPoint.y;
//
//        float minX = Math.min(startTouchX, endTouchX);
//        float maxX = Math.max(startTouchX, endTouchX);
//
//        int startIndexX = (int) minX / BLOCK_SIZE;
//        int endIndexX = (int) maxX / BLOCK_SIZE;
//
//        float minY = Math.min(startTouchY, endTouchY);
//        float maxY = Math.max(startTouchY, endTouchY);
//
//        int startIndexY = (int) minY / BLOCK_SIZE;
//        int endIndexY = (int) maxY / BLOCK_SIZE;//确定矩形的判断范围
//        if (startIndexX < 0 || startIndexY < 0 || endIndexY < 0 || endIndexY < 0) {
//            return;
//        }
//        for (int row = startIndexY; row <= endIndexY; row++) {
//            for (int colunm = startIndexX; colunm <= endIndexX; colunm++) {
//                Rect rect = new Rect(colunm * BLOCK_SIZE, row * BLOCK_SIZE, (colunm + 1) * BLOCK_SIZE, (row + 1) * BLOCK_SIZE);
//                Boolean intersectRect = GeometryHelper.IsLineIntersectRect(startPoint.clone(), endPoint.clone(), rect);
//                if (intersectRect) {//线段与直线相交
//                    int rowMax = Math.min((row + 1) * BLOCK_SIZE, mBitmapHeight);
//                    int colunmMax = Math.min((colunm + 1) * BLOCK_SIZE, mBitmapWidth);
//                    for (int i = row * BLOCK_SIZE; i < rowMax; i++) {
//                        for (int j = colunm * BLOCK_SIZE; j < colunmMax; j++) {
//                            mTempBitmapPixs[i * mBitmapWidth + j] = mSampleColors[row * mColumnCount + colunm];
//                        }
//                    }
//                }
//            }
//        }
//        mBitmap.setPixels(mTempBitmapPixs, 0, mBitmapWidth, 0, 0, mBitmapWidth, mBitmapHeight);
//    }

    /**
     *
     * @param startPoint
     * @param endPoint
     * @param updateBitmap 自动加载时为false, 因为setPixels耗时较长,等计算完之后再调用setPixels一次性画出来
     */
    private void mosaic(Point startPoint, Point endPoint, boolean updateBitmap) {
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
        if(updateBitmap) {
            mBitmap.setPixels(mTempBitmapPixs, 0, mBitmapWidth, 0, 0, mBitmapWidth, mBitmapHeight);
        }
    }

    public void setMasking(boolean masking) {
        isMasking = masking;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!canEdit) {
            return;
        }
        if (mBitmap == null) {
            init();
        }
        if (mBitmap != null) {
            rectF = getImageBound(rectF);
            canvas.clipRect(rectF);
            canvas.drawBitmap(mBitmap, null, rectF, null);
        }
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
            currMask = new Mask();
            invalidate();
            return super.onTouchEvent(event);
        } else if (action == MotionEvent.ACTION_MOVE && getPinchMode() == PINCH_MODE_SCROLL) { // 移动, 把点加入到currMask
//            touchMove(getPathX(Math.abs(x)), getPathY(Math.abs(y)));
            Point point = new Point(getPathX(event.getX()), getPathY(event.getY()));
            currMask.points.add(point);
            touchMove(point.x, point.y, true);
            invalidate();
            return true;
        } else if (action == MotionEvent.ACTION_POINTER_DOWN && getPinchMode() == PINCH_MODE_SCROLL) { // 多点按下,不打码
            currMask = null;
            return super.onTouchEvent(event);
        } else if (action == MotionEvent.ACTION_UP && getPinchMode() == PINCH_MODE_SCROLL) { // 抬起,把currMask加入到maskList
            if(currMask != null && currMask.points.size() > 1) {
                maskList.add(currMask);
                currMask = null;
            }
            return super.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    /**
     * 清除全部打码
     */
    public void clean() {
        mBitmap = null;
        maskList.clear();
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

    public class Mask{
        List<Point> points;
        Mask() {
            this.points = new ArrayList<>();
        }
    }

    public void setMaskList(List<Mask> maskList) {
        this.maskList = maskList;
    }

    public List<Mask> getMaskList() {
        return maskList;
    }
}
