package com.deadlinesaver.android;

import java.io.Serializable;

public class Backlog implements Serializable {

    private String backlogName;

    private boolean isDone;

    public Backlog(String backlogName) {
        this.backlogName = backlogName;
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
}
