package com.manualcoding.manualcoding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhaoya on 2017/5/8.
 */

public class DemoPath extends View {

    private float mX , mY;
    private Path mPath;
    private Paint mPaint;
    private static final float TOUCH_TOLERANCE = 4;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;

    public DemoPath(Context context) {
        super(context);
        initView();
    }

    public DemoPath(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DemoPath(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mPaint = new Paint(); // 创建画笔
        mPaint.setAntiAlias(true); // 抗锯齿,让会话比较平滑
        mPaint.setDither(true); // 设置递色
        mPaint.setColor(0xFFFF0000); // 画笔颜色
        mPaint.setStyle(Paint.Style.STROKE); // 画笔的类型有三种(1.FILL 2.FILL_AND_STROKE 3.STROKE)
        mPaint.setStrokeJoin(Paint.Join.ROUND); // 默认类型是MITER(1.BEVEL 2.MITER 3.ROUND)
        mPaint.setStrokeCap(Paint.Cap.ROUND);//默认类型是BUTT（1.BUTT 2.ROUND 3.SQUARE ）
        mPaint.setStrokeWidth(12); // 设置描边的宽度, 如果设置的值为0,那么是一条极细的线

        mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888); // 绘制固定大小的bitmap对象
        mCanvas = new Canvas(mBitmap); // 将固定的bitmap对象嵌入到canvas对象中
        mPath = new Path(); // 创建画笔路径
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFAAAAAA); // 画布颜色
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 按下
                mPath.reset(); // 将上次的路径保存起来，并重置新的路径。
                mPath.moveTo(x, y); // 设置新的路径“轮廓”的开始
                mX = x;
                mY = y;
                invalidate(); // 刷新画布，重新运行onDraw（）方法
                break;
            case MotionEvent.ACTION_MOVE: // 移动
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                    mX = x;
                    mY = y;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP: //手指离开屏幕，不再按压屏幕
                mPath.lineTo(mX, mY);//从最后一个指定的xy点绘制一条线，如果没有用moveTo方法，那么起始点表示（0，0）点。
                mCanvas.drawPath(mPath, mPaint);//手指离开屏幕后，绘制创建的“所有”路径。
                mPath.reset();
                invalidate();
                break;
        }
        return true;
    }
}













































