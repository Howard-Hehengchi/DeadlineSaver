package com.deadlinesaver.android.db;

import com.deadlinesaver.android.fragments.PersonalizedSettingsFragment;
import com.deadlinesaver.android.util.Utility;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class Deadline extends LitePalSupport implements Serializable {

    private int id;

    private String ddlName;

    private long dueTime;

    private long totalTime;

    private long alarmTimeAhead;

    private String ddlContent;

    private boolean isDone = false;

    private boolean isAlarmed = false;

    public Deadline(String ddlName, long dueTime, long totalTime) {
        this.ddlName = ddlName;
        this.dueTime = dueTime;
        this.totalTime = totalTime;
        alarmTimeAhead = (int)
                PersonalizedSettingsFragment.getCertainSetting
                        (PersonalizedSettingsFragment.SettingName.defaultAlarmTimeAhead);
        ddlContent = "";
    }

    public Deadline(String ddlName, long dueTime, long totalTime, long alarmTimeAhead, String content) {
        this.ddlName = ddlName;
        this.dueTime = dueTime;
        this.totalTime = totalTime;
        this.alarmTimeAhead = alarmTimeAhead;
        ddlContent = content;
    }

    public Deadline(String ddlName, long dueTime, long totalTime, boolean isDone) {
        this.ddlName = ddlName;
        this.dueTime = dueTime;
        this.totalTime = totalTime;
        this.isDone = isDone;
        alarmTimeAhead = (int)
                PersonalizedSettingsFragment.getCertainSetting
                        (PersonalizedSettingsFragment.SettingName.defaultAlarmTimeAhead);
        ddlContent = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDdlName() {
        return ddlName;
    }

    public void setDdlName(String ddlName) {
        this.ddlName = ddlName;
    }

    public long getDueTime() {
        return dueTime;
    }

    public void setDueTime(long dueTime) {
        this.dueTime = dueTime;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public String getDdlContent() {
        return ddlContent;
    }

    public void setDdlContent(String ddlContent) {
        this.ddlContent = ddlContent;
    }

    public long getAlarmTimeAhead() {
        return alarmTimeAhead;
    }

    public void setAlarmTimeAhead(long alarmTimeAhead) {
        this.alarmTimeAhead = alarmTimeAhead;
    }

    public boolean isAlarmed() {
        return isAlarmed;
    }

    public void setAlarmed(boolean isAlarmed) {
        this.isAlarmed = isAlarmed;
    }
}
