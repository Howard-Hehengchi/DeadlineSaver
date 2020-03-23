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

public class Utility {

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

}
