package com.deadlinesaver.android.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.db.Deadline;
import com.deadlinesaver.android.util.Utility;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UpdateBacklogService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteOldBacklogs();
                updateNewBacklogs();
            }
        }).start();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long millisInHour = 60 * 60 * 1000;
        long triggerAtTime = System.currentTimeMillis() + millisInHour;
        Intent i = new Intent(this, UpdateBacklogService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void deleteOldBacklogs() {
        LitePal.deleteAll(Backlog.class);
    }

    private void updateNewBacklogs() {
        List<Deadline> deadlineList = LitePal.findAll(Deadline.class);
        List<Backlog> backlogList = new ArrayList<>();
        for (Deadline deadline : deadlineList) {
            long timeLeft = deadline.getDueTime() - Utility.getTodayCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
            if (timeLeft <= Utility.minutesInDay) {
                backlogList.add(new Backlog(deadline.getDdlName()));
            }
        }

        for (Backlog backlog : backlogList) {
            backlog.save();
        }
    }
}
