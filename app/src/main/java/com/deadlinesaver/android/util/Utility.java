package com.deadlinesaver.android.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.activities.EditDeadlineActivity;
import com.deadlinesaver.android.gson.ApkInfo;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.TimeZone;

public class Utility {

    public static final int millisecondsInMinute = 60 * 1000; //TODO:以分钟计时，如有需要可以更改
    public static final int minutesInDay = 24 * 60;
    public static final int minutesInHour = 60;

    public static ApkInfo handleApkResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("ApkInfo");
                String infoContent = jsonArray.getJSONObject(0).toString();
                return new Gson().fromJson(infoContent, ApkInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取当前时间对应的Calendar对象
     * @return
     */
    public static Calendar getCalendar() {
        Calendar currentTimeCalendar = Calendar.getInstance();
        currentTimeCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return currentTimeCalendar;
    }

    /**
     * 根据给定时间获取对应的Calendar对象
     * @param year
     * @param month
     * @param dayOfMonth
     * @param hourOfDay
     * @param minute
     * @return
     */
    public static Calendar getCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        Calendar targetTimeCalendar = Calendar.getInstance();
        targetTimeCalendar.set(year, month, dayOfMonth, hourOfDay, minute);
        return targetTimeCalendar;
    }

    /**
     * 获取当天00：00的Calendar对象
     * @return
     */
    public static Calendar getTodayCalendar() {
        Calendar currentTimeCalendar = Utility.getCalendar();
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(currentTimeCalendar.get(Calendar.YEAR),
                currentTimeCalendar.get(Calendar.MONTH),
                currentTimeCalendar.get(Calendar.DATE), 0, 0);
        return todayCalendar;
    }

    public static void showTextInputBottomDialog(final Activity activity, String title, String defaultText, final TextView textView) {
        final Dialog bottomDialog = new Dialog(activity, R.style.BottomDialog);
        View contentView = LayoutInflater.from(activity).inflate(R.layout.text_input_dialog, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        params.width = activity.getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(activity, 40);
        params.bottomMargin = DensityUtil.dp2px(activity, 24);
        contentView.setLayoutParams(params);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

        //接下来初始化底部弹出Dialog内的控件
        TextView titleTextView = bottomDialog.findViewById(R.id.text_input_dialog_title_text_view);
        final EditText editDeadlineNameEditText = bottomDialog.findViewById(R.id.text_input_dialog_edit_text);
        TextView cancelTextView = bottomDialog.findViewById(R.id.text_input_dialog_cancel_text_view);
        TextView confirmTextView = bottomDialog.findViewById(R.id.text_input_dialog_confirm_text_view);

        titleTextView.setText(title);
        editDeadlineNameEditText.setText(defaultText);
        confirmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String textInput = editDeadlineNameEditText.getText().toString();
                if (!textInput.equals("")) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(textInput);
                        }
                    });
                    bottomDialog.dismiss();
                }
            }
        });

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
            }
        });



        //弹出Dialog
        bottomDialog.show();
    }

    public static String getTimeAheadString(int timeAhead) {
        StringBuilder builder = new StringBuilder();

        if (timeAhead >= minutesInDay) {
            builder.append(timeAhead / minutesInDay).append("天");
            timeAhead %= minutesInDay;
        }
        if (timeAhead >= minutesInHour) {
            builder.append(timeAhead / minutesInHour).append("小时");
            timeAhead %= minutesInHour;
        }
        if (timeAhead > 0) {
            builder.append(timeAhead).append("分钟");
        }

        return builder.toString();
    }
}
