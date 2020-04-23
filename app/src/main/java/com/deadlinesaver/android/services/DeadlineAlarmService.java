package com.deadlinesaver.android.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.activities.MainActivity;
import com.deadlinesaver.android.db.Deadline;
import com.deadlinesaver.android.fragments.DDLFragment;
import com.deadlinesaver.android.fragments.PersonalizedSettingsFragment;
import com.deadlinesaver.android.util.Utility;
import com.deadlinesaver.android.util.VibrateUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class DeadlineAlarmService extends Service {

    private static final String bundleName = "bundleName";
    private static final String deadlineName = "deadlineToAlarm";

    private List<String> channelIdList = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (intent != null) {
                    Bundle bundle = intent.getBundleExtra(bundleName);
                    if (bundle != null) {
                        Deadline deadline = (Deadline) bundle.getSerializable(deadlineName);
                        if (deadline != null && LitePal.find(Deadline.class, deadline.getId()) != null) {
                            Intent intent = new Intent(DeadlineAlarmService.this, MainActivity.class);
                            PendingIntent pi = PendingIntent.getActivity(DeadlineAlarmService.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            NotificationCompat.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String channelName = "DeadlineNotification";
                                String channelId = deadline.getDdlName();
                                if (channelIdList.contains(channelId)) {
                                    manager.deleteNotificationChannel(channelId);
                                    channelIdList.remove(channelId);
                                }
                                NotificationChannel channel = new NotificationChannel
                                        (channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                                //判断自定义设置-铃声
                                if (!(boolean) PersonalizedSettingsFragment.getCertainSetting(PersonalizedSettingsFragment.SettingName.ringWhenNotification)) {
                                    channel.setSound(null, null);
                                } else {
                                    channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
                                }
                                manager.createNotificationChannel(channel);
                                channelIdList.add(channelId);
                                builder = new NotificationCompat.Builder(DeadlineAlarmService.this, channelId);
                            } else {
                                builder = new NotificationCompat.Builder(DeadlineAlarmService.this);
                            }

                             builder.setContentTitle(getNotificationTitle(deadline))
                                    .setContentText(getNotificationMsg(deadline))
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                    .setContentIntent(pi)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setAutoCancel(true);
                            Notification notification = builder.build();
                            manager.notify(1, notification);
                            //判断自定义设置-震动
                            if ((boolean) PersonalizedSettingsFragment.getCertainSetting(PersonalizedSettingsFragment.SettingName.vibrateWhenNotification)) {
                                VibrateUtil.vibrate(DeadlineAlarmService.this, new long[] {0, 300, 300, 300}, false);
                            }
                        }
                    }
                }
            }
        }).start();

        Deadline deadlineToAlarm = DDLFragment.getDeadlineToAlarm();
        if (deadlineToAlarm != null  && !deadlineToAlarm.isAlarmed()) {
            //计算提醒时间
            long alarmTime = deadlineToAlarm.getDueTime() - deadlineToAlarm.getAlarmTimeAhead();
            alarmTime *= Utility.millisecondsInMinute;
            alarmTime = Math.max(alarmTime, Utility.getCalendar().getTimeInMillis());

            //更改DDL的提醒状态并保存
            deadlineToAlarm.setAlarmed(true);
            deadlineToAlarm.save();

            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Bundle bundle = new Bundle();
            bundle.putSerializable(deadlineName, deadlineToAlarm);
            Intent i = new Intent(this, DeadlineAlarmService.class);
            i.putExtra(bundleName, bundle);
            PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.cancel(pi);
            manager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
        } else {
            //如果暂时没有下一个要提醒的DDL，每隔10min自动扫描一次
            //注：此情况只可能当所有DDL均过了提醒时间且最后一个也已经被提醒过才会发生
            long alarmTime = Utility.getCalendar().getTimeInMillis() + Utility.millisecondsInMinute * 10;
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent i = new Intent(this, DeadlineAlarmService.class);
            PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.cancel(pi);
            manager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
        }

        return START_STICKY;
    }

    private String getNotificationTitle(Deadline deadline) {
        StringBuilder builder = new StringBuilder();
        //获取距离DDL截止的剩余时间，以分钟为单位
        long time = deadline.getDueTime() - Utility.getCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
        int minutesInDay = 24 * 60;
        int minutesInHour = 60;
        if (time >= minutesInDay) {
            builder.append(time / minutesInDay).append("天");
        } else {
            if (time >= minutesInHour) {
                builder.append(time / minutesInHour).append("小时");
                time %= minutesInHour;
            }
            if (time > 0) {
                builder.append(time).append("分钟");
            }
        }
        builder.append("后有个DDL要到期啦");

        //*（时间）*后有个DDL要到期啦
        return builder.toString();
    }

    private String getNotificationMsg(Deadline deadline) {
        StringBuilder builder = new StringBuilder();
        builder.append("DDL名称: ").append(deadline.getDdlName());
        builder.append("(点击查看详情)");

        //DDL名称: *（名称）*(点击查看详情)
        return builder.toString();
    }
}
