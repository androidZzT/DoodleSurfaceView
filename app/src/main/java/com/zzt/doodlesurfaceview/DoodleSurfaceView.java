package com.zzt.doodlesurfaceview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * DoodleView 使用onActionDoneListener监听每次发生的动作
 */
public class DoodleSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = DoodleSurfaceView.class.getSimpleName();

    private SurfaceHolder mSurfaceHolder = null;

    /**
     * 记录每次绘画动作
     */
    private ArrayList<DoodleAction> mDoodleActionList;

    /**
     * 当前绘制颜色
     */
    private int mCurColor;

    /**
     * 画板颜色
     */
    private int mBackgroundColor;

    /**
     * 当前笔的size
     */
    private float mCurStrokeWidth;

    /**
     * 当前绘制动作
     */
    private DoodleAction mCurAction;

    /**
     * 绘制类型
     */
    private DoodleShapeType mType = DoodleShapeType.Path;

    /**
     * 涂鸦功能是否可用
     */
    private boolean mIsDoodleEnabled;

    /**
     * 手指按下时坐标
     */
    private float mDownX, mDownY;

    public DoodleSurfaceView(Context context) {
        this(context, null, 0);
    }

    public DoodleSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoodleSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        restorePreAction(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        postDelayed(new Runnable() { //解决surfaceView restore不完全的问题
            @Override
            public void run() {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                restorePreAction(canvas);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }, 100);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mIsDoodleEnabled) return false;
                mDownX = x;
                mDownY = y;
                setCurDoodleAction(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                Canvas canvas = mSurfaceHolder.lockCanvas();
                restorePreAction(canvas);//首先恢复之前绘制的内容
                mCurAction.move(x, y);
                mCurAction.draw(canvas); //绘制当前Action
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                break;
            case MotionEvent.ACTION_UP:
                if (x == mDownX && y == mDownY) {
                    //目前 ACTION_DOWN --> ACTION_UP 不做任何处理，如想处理可加回调
                } else {
                    //只有手指完成滑动动作 才会添加并发送动作
                    mDoodleActionList.add(mCurAction);//添加当前动作
                }
                mCurAction = null;
                break;
        }
        return true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.setDoodleActionList(mDoodleActionList);
        ss.setStart(mIsDoodleEnabled);
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mDoodleActionList = ss.getDoodleActionList();
        mIsDoodleEnabled = ss.isStart();
        mSurfaceHolder = getHolder();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                restorePreAction(canvas);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }, 160);
    }


    private void init(AttributeSet attributeSet) {
        mIsDoodleEnabled = true;
        mDoodleActionList = new ArrayList<>();
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        this.setFocusable(true);
        this.setZOrderOnTop(true);//设置背景透明
        mBackgroundColor = Color.WHITE;
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        TypedArray a = getResources().obtainAttributes(attributeSet, R.styleable.DoodleSurfaceView);
        mCurColor = a.getColor(R.styleable.DoodleSurfaceView_paintColor, Color.BLACK);
        mCurStrokeWidth = a.getFloat(R.styleable.DoodleSurfaceView_paintStrokeWidth, 10.0f);//目前默认笔size写死
        a.recycle();
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 重新加载之前绘制的内容
     *
     * @param canvas 画布
     */
    private void restorePreAction(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //加载之前内容前清空画布
        if (mDoodleActionList != null && mDoodleActionList.size() > 0) {
            for (DoodleAction action : mDoodleActionList) {
                action.draw(canvas);
            }
        }
    }

    /**
     * 设置当前绘制动作类型
     *
     * @param startX 初始X坐标
     * @param startY 初始Y坐标
     */
    private void setCurDoodleAction(float startX, float startY) {
        switch (mType) {
            case Path:
                mCurAction = new DoodlePath(startX, startY);
                break;
            case Eraser:
                //TODO 添加Eraser
                break;
        }
        mCurAction.setColor(mCurColor);
        mCurAction.setStrokeWidth(mCurStrokeWidth);
    }

    public void enableDoodle(boolean enable) {
        mIsDoodleEnabled = enable;
    }

    public void setDoodleShapeType(DoodleShapeType type) {
        mType = type;
    }

    public void setCurStrokeWidth(float strokeWidth) {
        mCurStrokeWidth = strokeWidth;
    }

    public ArrayList<DoodleAction> getDoodleActionList() {
        return mDoodleActionList;
    }

    public void setPaintColor(int color) {
        mCurColor = color;
    }

    public void undoAction() {
        int size = mDoodleActionList.size();
        if (size > 0) {
            mDoodleActionList.remove(size - 1);
            Canvas canvas = mSurfaceHolder.lockCanvas();
            restorePreAction(canvas);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void cleanWhiteBoard() {
        if (mDoodleActionList != null && mDoodleActionList.size() > 0) {
            mDoodleActionList.clear();
                Canvas canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void recycle() {
        if (mDoodleActionList != null) {
            mDoodleActionList.clear();
            mDoodleActionList = null;
        }
    }

    /**
     * 绘制动作类型
     */
    public enum DoodleShapeType {
        Path,
        Eraser
    }

    private static class SavedState extends BaseSavedState {
        private boolean isStart;
        private ArrayList<DoodleAction> doodleActionList;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(doodleActionList);
            out.writeInt(isStart ? 1 : 0);
        }

        public static Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private void setDoodleActionList(ArrayList<DoodleAction> list) {
            doodleActionList = list;
        }

        private ArrayList<DoodleAction> getDoodleActionList() {
            return doodleActionList;
        }

        private boolean isStart() {
            return isStart;
        }

        private void setStart(boolean isStart) {
            this.isStart = isStart;
        }

        private SavedState(Parcel in) {
            super(in);
            in.readList(doodleActionList, List.class.getClassLoader());
            setDoodleActionList(doodleActionList);
            isStart = in.readInt() == 1;
        }
    }
}
