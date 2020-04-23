package com.deadlinesaver.android.recyclerview;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.deadlinesaver.android.R;
import com.deadlinesaver.android.fragments.PersonalizedSettingsFragment;
import com.deadlinesaver.android.util.DensityUtil;
import com.deadlinesaver.android.util.ToastUtil;
import com.deadlinesaver.android.util.Utility;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.deadlinesaver.android.util.Utility.getTimeAheadString;
import static com.deadlinesaver.android.util.Utility.minutesInDay;
import static com.deadlinesaver.android.util.Utility.minutesInHour;

public class SettingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SWITCH = 1;
    private static final int TYPE_VALUE = 2;

    private static final int[] typeArray = {TYPE_SWITCH, TYPE_SWITCH, TYPE_SWITCH, TYPE_VALUE};

    private Activity mActivity;
    private List<Setting> mSettingList;

    public SettingAdapter(Activity activity, List<Setting> settingList) {
        mActivity = activity;
        mSettingList = settingList;
    }

    @Override
    public int getItemViewType(int position) {
        return typeArray[position];
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_SWITCH:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.personal_settings_item_switch, parent, false);
                final ViewHolderSwitch viewHolderSwitch = new ViewHolderSwitch(view);

                viewHolderSwitch.settingSwitch.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
                    @Override
                    public void onStateChange(float process, State state, JellyToggleButton jtb) {
                        boolean isChecked = false;
                        if (state == State.LEFT) {
                            isChecked = false;
                        } else if (state == State.RIGHT){
                            isChecked = true;
                        }

                        Setting setting = mSettingList.get(viewHolderSwitch.getAdapterPosition());

                        setting.setOn(isChecked);
                        PersonalizedSettingsFragment.setCertainSetting
                                (PersonalizedSettingsFragment.SettingName.isDoubleSidesAttach, isChecked);
                    }
                });
                return viewHolderSwitch;
            case TYPE_VALUE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.personal_settings_item_value, parent, false);
                final ViewHolderValue viewHolderValue = new ViewHolderValue(view);

                viewHolderValue.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = viewHolderValue.getAdapterPosition();
                        if (position >= 0) {
                            final Setting setting = mSettingList.get(position);
                            int time = setting.getValue();
                            int selectedDay = 0, selectedHour = 0, selectedMinute = 0;
                            if (time >= minutesInDay) {
                                selectedDay = time / minutesInDay;
                                time %= minutesInDay;
                            }
                            if (time >= minutesInHour) {
                                selectedHour = time / minutesInHour;
                                time %= minutesInHour;
                            }
                            if (time > 0) {
                                selectedMinute = time;
                            }

                            List<String> optionItemsDay = new ArrayList<>();
                            List<String> optionItemsHour = new ArrayList<>();
                            List<String> optionItemsMinute = new ArrayList<>();

                            for (int i = 0; i <= 30; i++) {
                                optionItemsDay.add(String.valueOf(i));
                            }
                            for (int i = 0; i <= 23; i++) {
                                optionItemsHour.add(String.valueOf(i));
                            }
                            for (int i = 0; i <= 59; i++) {
                                optionItemsMinute.add(String.valueOf(i));
                            }

                            OptionsPickerView pvOption = new OptionsPickerBuilder(mActivity, new OnOptionsSelectListener() {
                                @Override
                                public void onOptionsSelect(final int options1, final int options2, final int options3, View v) {
                                    final int timeAhead = options1 * minutesInDay + options2 * minutesInHour + options3;
                                    if (timeAhead == 0) {
                                        ToastUtil.showToast(mActivity, "请选择一个有效时间！", Toast.LENGTH_LONG);
                                    } else {
                                        mActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                viewHolderValue.settingValueTextView.setText(getTimeAheadString(timeAhead));
                                                setting.setValue(timeAhead);
                                                PersonalizedSettingsFragment.setCertainSetting
                                                        (PersonalizedSettingsFragment.SettingName.defaultAlarmTimeAhead, timeAhead);
                                            }
                                        });
                                    }
                                }
                            })
                                    .setTitleText("请选择时间")
                                    .setTitleBgColor(0xFF09DAFF)
                                    .setDividerColor(Color.LTGRAY)
                                    .setTitleColor(Color.WHITE)
                                    .setCancelColor(Color.WHITE)
                                    .setSubmitColor(Color.WHITE)
                                    .isCenterLabel(true)
                                    .setSelectOptions(selectedDay, selectedHour, selectedMinute)
                                    .setLabels("天", "小时", "分钟")
                                    .setCyclic(false, true, true)
                                    .build();
                            pvOption.setNPicker(optionItemsDay, optionItemsHour, optionItemsMinute);
                            pvOption.show();
                        }
                    }
                });
                return viewHolderValue;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Setting setting = mSettingList.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_SWITCH:
                holder = (ViewHolderSwitch) holder;
                ((ViewHolderSwitch) holder).settingNameTextView.setText(setting.getName());
                ((ViewHolderSwitch) holder).settingSwitch.setChecked(setting.isOn());
                break;
            case TYPE_VALUE:
                holder = (ViewHolderValue) holder;
                ((ViewHolderValue) holder).settingNameTextView.setText(setting.getName());
                ((ViewHolderValue) holder).settingValueTextView.setText(getTimeAheadString(setting.getValue()));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mSettingList.size();
    }

    static class ViewHolderSwitch extends RecyclerView.ViewHolder {
        TextView settingNameTextView;
        JellyToggleButton settingSwitch;

        public ViewHolderSwitch(View view) {
            super(view);
            settingNameTextView = view.findViewById(R.id.personal_settings_name_text_view);
            settingSwitch = view.findViewById(R.id.personal_settings_switch);
        }
    }

    static class ViewHolderValue extends RecyclerView.ViewHolder {
        TextView settingNameTextView;
        TextView settingValueTextView;

        public ViewHolderValue(View view) {
            super(view);
            settingNameTextView = view.findViewById(R.id.personal_settings_name_text_view);
            settingValueTextView = view.findViewById(R.id.personal_settings_value_text_view);
        }
    }

}
