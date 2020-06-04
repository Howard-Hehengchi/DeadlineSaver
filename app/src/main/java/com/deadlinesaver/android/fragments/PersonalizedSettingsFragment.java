package com.deadlinesaver.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.recyclerview.Setting;
import com.deadlinesaver.android.recyclerview.SettingAdapter;

import java.util.ArrayList;
import java.util.List;

public class PersonalizedSettingsFragment extends Fragment {

    public static final String saveDataBroadcast = "com.deadlinesaver.android.REFRESH_PERSONAL_DATA";

    private static boolean isInitialize = true;
    private static List<Setting> settings = new ArrayList<>();
    /**
     * 各选项名称，仅可以在初始化时改动
     */
    private static List<String> optionNames = new ArrayList<>();

    private static final Setting.SettingType settingTypeArray[] =
            {Setting.SettingType.Switch,
                    Setting.SettingType.Switch,
                    Setting.SettingType.Switch,
                    Setting.SettingType.Value};

    private static final String spName = "PersonalSettings";

    private RecyclerView recyclerView;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personalized_settings_fragment, container, false);

        Context context = getContext();
        if (context != null) {
            localBroadcastManager = LocalBroadcastManager.getInstance(context);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(saveDataBroadcast);
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

        recyclerView = view.findViewById(R.id.personal_settings_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        SettingAdapter adapter = new SettingAdapter(getActivity(), settings, localBroadcastManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    public static void initializeSettingsData(Context context) {
        if (isInitialize) {
            initPersonalSettingsData(context);
            isInitialize = false;
        }
    }

    /**
     * 从数据库获取个性化设置数据并加载
     */
    private static void initPersonalSettingsData(Context context) {
        initOptionNames();

        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);

        for (int index = 0; index < optionNames.size(); index++) {
            String settingName = optionNames.get(index);
            switch (settingTypeArray[index]) {
                case Switch:
                    boolean isOn = sp.getBoolean(settingName, false);
                    settings.add(new Setting(settingName, isOn));
                    break;
                case Value:
                    int value = sp.getInt(settingName, 60);
                    settings.add(new Setting(settingName, value));
                    break;
            }
        }
    }

    /**
     * 初始化各项设置的名称
     */
    private static void initOptionNames() {
        optionNames.add("悬浮按钮两侧吸附");
        optionNames.add("提醒时响铃");
        optionNames.add("提醒时震动");
        optionNames.add("默认提醒提前时间");
    }

    public static void setCertainSetting(SettingName name, Object settingParam) {
        switch (name) {
            case isDoubleSidesAttach:
                settings.get(0).setOn((boolean) settingParam);
                break;
            case ringWhenNotification:
                settings.get(1).setOn((boolean) settingParam);
                break;
            case vibrateWhenNotification:
                settings.get(2).setOn((boolean) settingParam);
                break;
            case defaultAlarmTimeAhead:
                settings.get(3).setValue((int) settingParam);
                break;
        }
    }

    public static Object getCertainSetting(SettingName name) {
        switch (name) {
            case isDoubleSidesAttach:
                return settings.get(0).isOn();
            case ringWhenNotification:
                return settings.get(1).isOn();
            case vibrateWhenNotification:
                return settings.get(2).isOn();
            case defaultAlarmTimeAhead:
                return settings.get(3).getValue();
        }
        return false;
    }

    /**
     * 用户设置各选项，按顺序排列
     * TODO:如需添加设置选项必须在此处声明
     */
    public enum SettingName {
        isDoubleSidesAttach,
        ringWhenNotification,
        vibrateWhenNotification,
        defaultAlarmTimeAhead //默认以秒为单位
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //保存用户个性化设置
            SharedPreferences.Editor editor =
                    getActivity().getSharedPreferences(spName, Context.MODE_PRIVATE).edit();
            Setting tempSetting;
            for (int index = 0; index < settings.size(); index++) {
                tempSetting = settings.get(index);
                switch (tempSetting.getType()) {
                    case Switch:
                        editor.putBoolean(tempSetting.getName(), tempSetting.isOn());
                        break;
                    case Value:
                        editor.putInt(tempSetting.getName(), tempSetting.getValue());
                        break;
                }
            }
            editor.apply();
        }
    }
}
