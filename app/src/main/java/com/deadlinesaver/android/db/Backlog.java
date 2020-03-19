package com.deadlinesaver.android.db;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class Backlog extends LitePalSupport implements Serializable {

    private int id;

    private String backlogName;

    private boolean isDone = false;

    public Backlog(String backlogName) {
        this.backlogName = backlogName;
    }

    public Backlog(String backlogName, boolean isDone) {
        this.backlogName = backlogName;
        this.isDone = isDone;
    }

    public String getBacklogName() {
        return backlogName;
    }

    public void setBacklogName(String backlogName) {
        this.backlogName = backlogName;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
