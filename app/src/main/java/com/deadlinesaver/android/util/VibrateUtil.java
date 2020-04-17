package com.deadlinesaver.android.util;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

public class VibrateUtil {

    public static void vibrate(Activity activity, long milliseconds) {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(milliseconds);
    }

    public static void vibrate(Activity activity, long[] pattern, boolean isRepeat) {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, isRepeat ? 1 : -1);
    }

}
