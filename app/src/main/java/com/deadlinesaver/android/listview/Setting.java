package com.deadlinesaver.android.listview;

public class Setting {

    private String name;

    private boolean isOn;

    public Setting(String name, boolean isOn) {
        this.name = name;
        this.isOn = isOn;
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
}
