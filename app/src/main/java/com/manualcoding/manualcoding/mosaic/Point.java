package com.manualcoding.manualcoding.mosaic;

/**
 * Created by zhaoya on 2017/10/24.
 */

public class Point {
    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x;
    public float y;

    public Point clone() {
        return new Point(x, y);
    }
}
