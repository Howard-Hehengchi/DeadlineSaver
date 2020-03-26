package com.deadlinesaver.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.listview.Setting;
import com.deadlinesaver.android.listview.SettingAdapter;

import java.util.ArrayList;
import java.util.List;

public class PersonalizedSettingsFragment extends Fragment {

    private static boolean isInitialize = true;
    private static List<Setting> settings = new ArrayList<>();
    /**
     * 各选项名称，仅可以在初始化时改动
     */
    private static List<String> optionNames = new ArrayList<>();

    private static final String spName = "PersonalSettings";

    private ListView listView;
    private SettingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personalized_settings_fragment, container, false);

        listView = view.findViewById(R.id.personal_settings_list_view);
        adapter = new SettingAdapter(getContext(), R.layout.personal_settings_item, settings);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onPause() {
        //将保存触发条件设置为最高触发率的条件，以保证用户数据即时存储
        super.onPause();
        //保存用户个性化设置
        SharedPreferences.Editor editor =
                getActivity().getSharedPreferences(spName, Context.MODE_PRIVATE).edit();
        Setting tempSetting;
        for (int index = 0; index < settings.size(); index++) {
            tempSetting = settings.get(index);
            editor.putBoolean(tempSetting.getName(), tempSetting.isOn());
        }
        editor.apply();
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
        List<Boolean> optionStatuses = new ArrayList<>();
        for (int index = 0; index < optionNames.size(); index++) {
            optionStatuses.add(sp.getBoolean(optionNames.get(index), false));
            settings.add(new Setting(optionNames.get(index), optionStatuses.get(index)));
        }
    }

    /**
     * 初始化各项设置的名称
     */
    private static void initOptionNames() {
        optionNames.add("悬浮按钮两侧吸附");
    }

    public static void setCertainSetting(SettingType type, boolean settingStatus) {
        switch (type) {
            case isDoubleSidesAttach:
                settings.get(0).setOn(settingStatus);
                break;
        }
    }

    public static boolean getCertainSetting(SettingType type) {
        switch (type) {
            case isDoubleSidesAttach:
                return settings.get(0).isOn();
        }
        return false;
    }

    /**
     * 用户设置各选项，按顺序排列
     * TODO:如需添加设置选项必须在此处声明
     */
    public enum SettingType {
        isDoubleSidesAttach
    }
}
