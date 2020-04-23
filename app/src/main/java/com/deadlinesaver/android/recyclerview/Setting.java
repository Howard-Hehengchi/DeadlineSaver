package com.deadlinesaver.android.recyclerview;

public class Setting {

    private String name;

    private boolean isOn;

    private int value;

    private SettingType type;

    public Setting(String name, boolean isOn) {
        this.name = name;
        this.isOn = isOn;
        type = SettingType.Switch;
    }

    public Setting(String name, int value) {
        this.name = name;
        this.value = value;
        type = SettingType.Value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public SettingType getType() {
        return type;
    }

    public void setType(SettingType type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public enum SettingType {
        Switch,
        Value
    }
}
