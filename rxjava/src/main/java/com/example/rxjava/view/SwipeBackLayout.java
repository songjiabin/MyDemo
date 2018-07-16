package com.example.rxjava.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * author : 宋佳
 * time   : 2018/07/16
 * desc   :
 * version: 1.0.0
 */

public class SwipeBackLayout extends FrameLayout {


    private Activity mActivity;
    private ViewDragHelper mDragHelper;

    private ViewGroup mDecorView;
    private View mRootView;

    private int mScreenWidth;
    private int mScreenHeight;


    //触发退出当前Activity的宽度
    private float mSlideWidth;


    //画笔，用来绘制阴影效果
    private Paint mPaint;
    //用于记录当前滑动距离
    private int curSlideX;


    public SwipeBackLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //必须是传入Activity
        mActivity = (Activity) context;
        mDragHelper = ViewDragHelper.create(this, new SwipeBackDragCallback());

        // 设置从左边缘捕获view
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);


        //初始化画笔
        mPaint = new Paint();
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GRAY);
    }


    //绑定方法，在Activity的DecorView下插入当前ViewGroup,原来的RootView放于当前ViewGroup下
    public void bind() {
        mDecorView = (ViewGroup) mActivity.getWindow().getDecorView();
        mRootView = mDecorView.getChildAt(0);
        mDecorView.removeView(mRootView);
        this.addView(mRootView);
        mDecorView.addView(this);

        //计算屏幕宽度
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mSlideWidth = dm.widthPixels * 0.28f;
    }


    //拦截事件  并传递给 ViewDragHelper 处理
    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    ///触摸事件  交个 ViewDragHelper 处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }


    @Override
    public void computeScroll() {
        //使用settleCapturedViewAt方法是，必须重写computeScroll方法，传入true
        //持续滚动期间，不断刷新ViewGroup
        if (mDragHelper.continueSettling(true)) {
            invalidate();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    // 重写  dispatchDraw 方法
    // onDraw()方法有的时候 在ViewGroup中不会被调用
    @Override
    protected void dispatchDraw(Canvas canvas) {
        //进行阴影绘制,onDraw（）方法在ViewGroup中不一定会执行
        drawShadow(canvas);
        super.dispatchDraw(canvas);

    }

    private void drawShadow(Canvas canvas) {
        canvas.save();
        //构造一个渐变
        Shader mShader = new LinearGradient(curSlideX - 40, 0, curSlideX, 0,
                new int[]{Color.parseColor("#00dddddd"), Color.parseColor("#33666666"), Color.parseColor("#90666666")},
                null, Shader.TileMode.REPEAT);
        //设置着色器
        mPaint.setShader(mShader);
        //绘制时，注意向左边偏移
        RectF rectF = new RectF(curSlideX - 40, 0, curSlideX, mScreenHeight);
        canvas.drawRect(rectF, mPaint);
        canvas.restore();
    }


    public class SwipeBackDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return false;
        }


        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //当前回调，松开手时触发，比较触发条件和当前的滑动距离
            int left = releasedChild.getLeft();
            if (left <= mSlideWidth) {
                //缓慢滑动的方法,小于触发条件，滚回去
                mDragHelper.settleCapturedViewAt(0, 0);
            } else {
                //大于触发条件，滚出去...
                mDragHelper.settleCapturedViewAt(mScreenWidth, 0);
            }
            //需要手动调用更新界面的方法
//            invalidate();
            ViewCompat.postInvalidateOnAnimation(SwipeBackLayout.this);
        }


        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            curSlideX = left;
            //当滑动位置改变时，刷新View,绘制新的阴影位置
            invalidate();
            //当滚动位置到达屏幕最右边，则关掉Activity
            if (changedView == mRootView && left >= mScreenWidth) {
                mActivity.finish();
                mActivity.overridePendingTransition(0, 0);
            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //限制左右拖拽的位移
            left = left >= 0 ? left : 0;
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //上下不能移动，返回0
            return 0;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            //触发边缘时，主动捕捉mRootView
            mDragHelper.captureChildView(mRootView, pointerId);
        }


    }


}
