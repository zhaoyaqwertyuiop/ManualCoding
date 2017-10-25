package com.manualcoding.manualcoding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.pinchimageview.PinchImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoya on 2017/5/10.
 * 手动打码view
 */

public class ManualCodingView extends PinchImageView {

    private boolean isMasking = false; // 正在打码,默认为false
    private Paint mPaint;
    private List<Mask> maskList = new ArrayList<>();
    private Mask currMask;
    private RectF rectF;
    private Path mPath = new Path();
    private int penWidth = 30; // 打码笔的宽度

    public ManualCodingView(Context context) {
        super(context);
        initPaint();
    }

    public ManualCodingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public ManualCodingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }

    public void setPenWidth(int penWidth) {
        this.penWidth = penWidth;
    }

    private void initPaint() {
        mPaint = new Paint(); // 创建画笔
        mPaint.setAntiAlias(true); // 抗锯齿,让会话比较平滑
        mPaint.setDither(true); // 设置递色
        mPaint.setColor(0xFFFF0000); // 画笔颜色
        mPaint.setStyle(Paint.Style.STROKE); // 画笔的类型有三种(1.FILL 2.FILL_AND_STROKE 3.STROKE)
        mPaint.setStrokeJoin(Paint.Join.ROUND); // 默认类型是MITER(1.BEVEL 2.MITER 3.ROUND)
        mPaint.setStrokeCap(Paint.Cap.ROUND);//默认类型是BUTT（1.BUTT 2.ROUND 3.SQUARE ）
        mPaint.setStrokeWidth(12); // 设置描边的宽度, 如果设置的值为0,那么是一条极细的线
    }

    public void setMasking(boolean masking) {
        isMasking = masking;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Mask mask : maskList) {
            drawMaskToView(canvas, mask);
        }
        if(currMask != null) {
            drawMaskToView(canvas, currMask);
        }
    }

    /** 在view上画 */
    private void drawMaskToView(Canvas canvas, Mask mask)  {
        if (mask.points.size() <= 1) return;
        canvas.clipRect(getImageBound(rectF));
        mPaint.setStrokeWidth(mask.radius / getScale());
        mPath.reset(); // 重置新的路径。
        MaskPoint first = mask.points.get(0);
        float preX = getViewX(first.x);
        float preY = getViewY(first.y);
        mPath.moveTo(preX, preY); // 设置新的路径“轮廓”的开始

        for (int i = 1; i < mask.points.size(); i++) {
            float x = getViewX(mask.points.get(i).x);
            float y = getViewY(mask.points.get(i).y);
            mPath.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2); // 2阶 贝塞尔曲线
            preX = x;
            preY = y;
        }
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isMasking) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action ==  MotionEvent.ACTION_DOWN) { // 按下,记录currMask
            currMask = new Mask(penWidth * getScale());
            return super.onTouchEvent(event);
        } else if (action == MotionEvent.ACTION_MOVE && getPinchMode() == PINCH_MODE_SCROLL) { // 移动, 把点加入到currMask
            currMask.points.add(new MaskPoint(getPathX(event.getX()), getPathY(event.getY())));
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

    /** 清除全部打码 */
    public void clean() {
        maskList.clear();
        invalidate();
    }

    /** 撤销上一次打码 */
    public void cancelLastMask() {
        if (maskList.isEmpty()) return;
        maskList.remove(maskList.size() - 1);
        invalidate();
    }

    /** 得到打码后的bitmap */
    public Bitmap getMaskedBitmap() {
        Drawable drawable = super.getDrawable();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        for (Mask mask : maskList) {
            drawMaskToBitmap(canvas, mask);
        }
        return bitmap;
    }

    /** 在bitmap上画 */
    private void drawMaskToBitmap(Canvas canvas, Mask mask)  {
        if (mask.points.size() <= 1) return;
        mPaint.setStrokeWidth(mask.radius);
        mPath.reset(); // 重置新的路径。
        MaskPoint first = mask.points.get(0);
        float preX = first.x;
        float preY = first.y;
        mPath.moveTo(preX, preY); // 设置新的路径“轮廓”的开始

        for (int i = 1; i < mask.points.size(); i++) {
            float x = mask.points.get(i).x;
            float y = mask.points.get(i).y;
            mPath.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2); // 2阶 贝塞尔曲线
            preX = x;
            preY = y;
        }
        canvas.drawPath(mPath, mPaint);
    }

    private static class MaskPoint {
        public final float x, y;

        public MaskPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Mask {
        float radius;
        List<MaskPoint> points;

        public Mask(float radius) {
            this.radius = radius;
            this.points = new ArrayList<>();
        }
    }

    public List<Mask> getMasks() {
        return maskList;
    }

    public void setMasks(List<Mask> maskList) {
        this.maskList.clear();
        if (maskList != null) {
            this.maskList.addAll(maskList);
        }
        invalidate();
    }
}
