package com.deadlinesaver.android.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import androidx.annotation.Nullable;

import com.deadlinesaver.android.fragments.PersonalizedSettingsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DraggableFab extends FloatingActionButton {

    private float mLastRawX;
    private float mLastRawY;
    private boolean isDrag = false;
    private int mRootMeasuredWidth = 0;
    private int mRootMeasuredHeight = 0;
    private int mRootTopY = 0;

    public DraggableFab(Context context) {
        super(context);
    }

    public DraggableFab(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableFab(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //当前手指的坐标
        float mRawX = event.getRawX();
        float mRawY = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://手指按下
                isDrag = false;
                //记录按下的位置
                mLastRawX = mRawX;
                mLastRawY = mRawY;
                ViewGroup mViewGroup = (ViewGroup) getParent();
                if (mViewGroup != null) {
                    int[] location = new int[2];
                    mViewGroup.getLocationInWindow(location);
                    //获取父布局的高度
                    mRootMeasuredHeight = mViewGroup.getMeasuredHeight();
                    mRootMeasuredWidth = mViewGroup.getMeasuredWidth();
                    //获取父布局顶点的坐标
                    mRootTopY = location[1];
                }
                break;
            case MotionEvent.ACTION_MOVE://手指滑动
                if (mRawX >= 0 && mRawX <= mRootMeasuredWidth && mRawY >= mRootTopY && mRawY <= (mRootMeasuredHeight + mRootTopY)) {
                    //手指X轴滑动距离
                    float differenceValueX = mRawX - mLastRawX;
                    //手指Y轴滑动距离
                    float differenceValueY = mRawY - mLastRawY;
                    //判断是否为拖动操作
                    if (!isDrag) {
                        isDrag = !(Math.sqrt(differenceValueX * differenceValueX + differenceValueY * differenceValueY) < 2);
                    }
                    //获取手指按下的距离与控件本身X轴的距离
                    float ownX = getX();
                    //获取手指按下的距离与控件本身Y轴的距离
                    float ownY = getY();
                    //理论中X轴拖动的距离
                    float endX = ownX + differenceValueX;
                    //理论中Y轴拖动的距离
                    float endY = ownY + differenceValueY;
                    //X轴可以拖动的最大距离
                    float maxX = mRootMeasuredWidth - getWidth();
                    //Y轴可以拖动的最大距离
                    float maxY = mRootMeasuredHeight - getHeight();
                    //X轴边界限制
                    endX = endX < 0 ? 0 : Math.min(endX, maxX);
                    //Y轴边界限制
                    endY = endY < 0 ? 0 : Math.min(endY, maxY);
                    //开始移动
                    setX(endX);
                    setY(endY);
                    //记录位置
                    mLastRawX = mRawX;
                    mLastRawY = mRawY;
                }

                break;
            case MotionEvent.ACTION_UP://手指离开
                //判断是否为点击事件
                if (isDrag) {
                    if (PersonalizedSettingsFragment.getCertainSetting
                            (PersonalizedSettingsFragment.SettingType.isDoubleSidesAttach)) {
                        float center = mRootMeasuredWidth / 2;
                        //自动贴边
                        if (mLastRawX <= center) {
                            //向左贴边
                            DraggableFab.this.animate()
                                    .setInterpolator(new BounceInterpolator())
                                    .setDuration(500)
                                    .x(40)
                                    .start();
                        } else {
                            //向右贴边
                            DraggableFab.this.animate()
                                    .setInterpolator(new BounceInterpolator())
                                    .setDuration(500)
                                    .x(mRootMeasuredWidth - getWidth() - 40)
                                    .start();
                        }
                    } else {
                        //向右贴边
                        DraggableFab.this.animate()
                                .setInterpolator(new BounceInterpolator())
                                .setDuration(500)
                                .x(mRootMeasuredWidth - getWidth() - 40)
                                .start();
                    }
                }
                break;
        }
        return isDrag || super.onTouchEvent(event);
    }
}
