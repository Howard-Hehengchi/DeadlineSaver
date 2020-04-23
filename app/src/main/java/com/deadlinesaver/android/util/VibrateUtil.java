package com.deadlinesaver.android.util;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

public class VibrateUtil {

    public static void vibrate(Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(milliseconds);
    }

    public static void vibrate(Context context, long[] pattern, boolean isRepeat) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, isRepeat ? 1 : -1);
    }

}
