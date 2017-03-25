package com.zzt.doodlesurfaceview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;

/**
 * 自由曲线
 */
class DoodlePath extends DoodleAction {

    private Path mPath;

    private float mPrevX;

    private float mPrevY;

    private Paint mPaint;

    DoodlePath() {
        this(0, 0, 0, 10.0f);
    }

    DoodlePath(float startX, float startY) {
        this(startX, startY, 0, 10.0f);
    }

    DoodlePath(float startX, float startY, int color, float strokeWidth) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        mPath = new Path();
        mPath.moveTo(startX, startY);
        mPrevX = startX;
        mPrevY = startY;
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public void setColor(int color) {
        super.setColor(color);
        mPaint.setColor(color);
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
        super.setStrokeWidth(strokeWidth);
        mPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public void move(float x, float y) {
        mPath.quadTo(mPrevX, mPrevY, (x + mPrevX) / 2, (y + mPrevY) / 2);
        mPrevX = x;
        mPrevY = y;
    }

    public void moveTo(float startX, float startY) {
        mPath.moveTo(startX, startY);
        mPrevX = startX;
        mPrevY = startY;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(color);
        out.writeFloat(strokeWidth);
    }

    public static final Creator<DoodlePath> CREATOR = new Creator<DoodlePath>() {

        @Override
        public DoodlePath createFromParcel(Parcel in) {
            return new DoodlePath(in);
        }

        @Override
        public DoodlePath[] newArray(int size) {
            return new DoodlePath[size];
        }
    };

    private DoodlePath(Parcel in) {
        color = in.readInt();
        strokeWidth = in.readFloat();
    }

}
