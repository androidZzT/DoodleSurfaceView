package com.zzt.doodlesurfaceview;

import android.graphics.Canvas;
import android.os.Parcelable;

/**
 * 绘制动作 目前包括Path ,以后有特殊图形可继承此类
 */
abstract class DoodleAction implements Parcelable {

    protected int color;

    protected float strokeWidth;

    DoodleAction() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Override
    public String toString() {
        return "DoodleAction{" +
                ", color=" + color +
                ", strokeWidth=" + strokeWidth +
                '}';
    }

    /**
     * 绘制当前动作内容
     *
     * @param canvas 新画布
     */
    public abstract void draw(Canvas canvas);


    /**
     * 根据手指移动坐标进行绘制
     *
     * @param x
     * @param y
     */
    public abstract void move(float x, float y);

}
