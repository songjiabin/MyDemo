package com.example.mydemo.event;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * author : 宋佳
 * time   : 2018/07/12
 * desc   :
 * version: 1.0.0
 */

public class MyViewPager extends ViewPager {


    private int mLastXIntercept;
    private int mLastYIntercept;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * 拦截事件  改成不拦截
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                //调用ViewPager的onInterceptTouchEvent方法初始化mActivePointerId
                super.onInterceptTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                //横坐标位移增量
                int deltaX = x - mLastXIntercept;
                //纵坐标位移增量
                int deltaY = y - mLastYIntercept;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                break;
        }

        mLastXIntercept = x;
        mLastYIntercept = y;



        return intercepted;


    }
}
