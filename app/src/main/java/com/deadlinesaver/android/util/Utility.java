package com.deadlinesaver.android.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.deadlinesaver.android.gson.ApkInfo;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class Utility {

    public static final int millisecondsInMinute = 60 * 1000; //TODO:以分钟计时，如有需要可以更改
    public static final int minutesInDay = 24 * 60;

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
}
