package com.deadlinesaver.android.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.fragments.PersonalizedSettingsFragment;
import com.suke.widget.SwitchButton;

import org.w3c.dom.Text;

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

            viewHolder.mSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
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
        SwitchButton mSwitch;
    }
}
