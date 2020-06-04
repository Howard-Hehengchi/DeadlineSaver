package com.deadlinesaver.android.worker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.activities.MainActivity;
import com.deadlinesaver.android.db.Deadline;
import com.deadlinesaver.android.fragments.PersonalizedSettingsFragment;
import com.deadlinesaver.android.util.Utility;
import com.deadlinesaver.android.util.VibrateUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class DeadlineAlarmWorker extends Worker {

    public static final String deadlineIdKey = "DeadlineId";

    private Context mContext;
    private static List<String> channelIdList = new ArrayList<>();

    public DeadlineAlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data inputData = getInputData();
            long deadlineId = inputData.getLong(deadlineIdKey, -1);
            if (deadlineId != -1) {
                Deadline deadline = LitePal.find(Deadline.class, deadlineId);
                if (deadline != null) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationManager manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
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
                        channel.setShowBadge(true);
                        //判断自定义设置-铃声
                        if (!(boolean) PersonalizedSettingsFragment.getCertainSetting(PersonalizedSettingsFragment.SettingName.ringWhenNotification)) {
                            channel.setSound(null, null);
                        } else {
                            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
                        }
                        manager.createNotificationChannel(channel);
                        channelIdList.add(channelId);
                        builder = new NotificationCompat.Builder(mContext, channelId);
                    } else {
                        builder = new NotificationCompat.Builder(mContext);
                    }

                    builder.setContentTitle(getNotificationTitle(deadline))
                            .setContentText(getNotificationMsg(deadline))
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.ic_notification)
                            .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                            .setContentIntent(pi)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setAutoCancel(true);
                    Notification notification = builder.build();
                    manager.notify(1, notification);
                    //判断自定义设置-震动
                    if ((boolean) PersonalizedSettingsFragment.getCertainSetting(PersonalizedSettingsFragment.SettingName.vibrateWhenNotification)) {
                        VibrateUtil.vibrate(mContext, new long[] {0, 300, 300, 300}, false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
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
