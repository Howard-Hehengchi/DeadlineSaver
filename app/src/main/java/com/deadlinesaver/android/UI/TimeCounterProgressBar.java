package com.deadlinesaver.android.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TimeCounterProgressBar extends androidx.appcompat.widget.AppCompatSeekBar {

    public TimeCounterProgressBar(Context context) {
        super(context);
    }

    public TimeCounterProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeCounterProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
