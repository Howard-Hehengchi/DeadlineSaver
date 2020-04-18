package com.deadlinesaver.android.db;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class Deadline extends LitePalSupport implements Serializable {

    private int id;

    private String ddlName;

    private long dueTime;

    private long totalTime;

    private String ddlContent;

    private boolean isDone = false;

    public Deadline(String ddlName, long dueTime, long totalTime) {
        this.ddlName = ddlName;
        this.dueTime = dueTime;
        this.totalTime = totalTime;
        ddlContent = "";
    }

    public Deadline(String ddlName, long dueTime, long totalTime, boolean isDone) {
        this.ddlName = ddlName;
        this.dueTime = dueTime;
        this.totalTime = totalTime;
        this.isDone = isDone;
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
}
