package com.deadlinesaver.android.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.fragments.PersonalizedSettingsFragment;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import java.util.List;

public class SettingAdapter extends ArrayAdapter<Setting> {

    private int resourceId;

    public SettingAdapter(Context context, int viewResourceId, List<Setting> settings) {
        super(context, viewResourceId, settings);
        resourceId = viewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Setting setting = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.settingName = view.findViewById(R.id.personal_settings_text_view);
            viewHolder.mSwitch = view.findViewById(R.id.personal_settings_switch);

            viewHolder.mSwitch.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
                @Override
                public void onStateChange(float process, State state, JellyToggleButton jtb) {
                    boolean isChecked = false;
                    if (state == State.LEFT) {
                        isChecked = false;
                    } else if (state == State.RIGHT){
                        isChecked = true;
                    }

                    setting.setOn(isChecked);
                    PersonalizedSettingsFragment.setCertainSetting
                            (PersonalizedSettingsFragment.SettingType.isDoubleSidesAttach,
                                    setting.isOn());
                }
            });

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.settingName.setText(setting.getName());
        viewHolder.mSwitch.setChecked(setting.isOn());
        return view;
    }

    class ViewHolder {
        TextView settingName;
        JellyToggleButton mSwitch;
    }
}
