package com.deadlinesaver.android.fragments;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.activities.MainActivity;
import com.deadlinesaver.android.db.Deadline;
import com.deadlinesaver.android.recyclerview.DeadlineAdapter;
import com.deadlinesaver.android.recyclerview.ItemTouchCallback;
import com.deadlinesaver.android.util.Utility;

import org.litepal.LitePal;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collector;

import static com.deadlinesaver.android.activities.MainActivity.sortWay;

public class DDLFragment extends Fragment {

    private static List<Deadline> deadlineList = new ArrayList<>();

    private static RecyclerView recyclerView;

    private CountDownTimer countDownTimer;
    public static boolean hasCreatedTimer = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.ddl_fragment, container, false);

        recyclerView = view.findViewById(R.id.deadline_recycler_view);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        DeadlineAdapter adapter = new DeadlineAdapter(getActivity(), view, deadlineList);
        recyclerView.setAdapter(adapter);

        ItemTouchCallback callback = new ItemTouchCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        if (!hasCreatedTimer) {
            long maxDueTime = 0;
            for (Deadline deadline : deadlineList) {
                if (deadline.getDueTime() > maxDueTime) {
                    maxDueTime = deadline.getDueTime();
                }
            }
            long maxLeftTime = maxDueTime - Utility.getCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
            long timeLeft = 24 * 60;
            if (maxLeftTime > timeLeft) {
                timeLeft = maxLeftTime;
            }
            timeLeft *= Utility.millisecondsInMinute;
            countDownTimer = new CountDownTimer(timeLeft, 1000 * 60) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        for (int position = 0; position < layoutManager.getChildCount(); position++) {
                            View childView = layoutManager.getChildAt(position);
                            final DeadlineAdapter.ViewHolder viewHolder = (DeadlineAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
                            final String deadlineName = viewHolder.deadlineNameTextView.getText().toString();
                            final Deadline deadline = getDeadlineOfName(deadlineName);
                            if (deadline != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //获取剩余时间的显示文本
                                        String timeText = DeadlineAdapter.getTimeText(deadline.getDueTime());
                                        viewHolder.deadlineTimeTextView.setText(timeText);
                                        //如果超时，显示相应提示
                                        if (timeText.equals(DeadlineAdapter.timeOver)) {
                                            String newDeadlineName = deadlineName + DeadlineAdapter.timeOverNameSuffix;
                                            viewHolder.deadlineNameTextView.setText(newDeadlineName);
                                        }
                                        //获取时间进度
                                        long timeLeft = deadline.getDueTime() - Utility.getCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
                                        int progress = (int) (((deadline.getTotalTime() - timeLeft) / (float) deadline.getTotalTime()) * 100);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            viewHolder.seekBar.setProgress(progress, true);
                                        } else {
                                            viewHolder.seekBar.setProgress(progress);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }

                @Override
                public void onFinish() {

                }
            };
            countDownTimer.start();

            hasCreatedTimer = true;
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        deadlineList.clear();
        deadlineList.addAll(LitePal.findAll(Deadline.class));
        recyclerView.getAdapter().notifyDataSetChanged();
        sortDDL();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public static void addDeadline(final Deadline deadline, boolean isInitialize) {
        for (Deadline deadlineInList : deadlineList) {
            if (deadlineInList.getDdlName().equals(deadline.getDdlName()))
                return;
        }

        //在添加元素时按指定顺序插入
        int index = 0;
        int arrayLength = deadlineList.size();
            switch (sortWay) {
                case SortByName:
                    Collator instance = Collator.getInstance(Locale.CHINA);
                    while (index <= arrayLength - 1 && instance.compare(deadlineList.get(index).getDdlName(), deadline.getDdlName()) < 0) {
                        index++;
                    }
                    break;
                case SortByTime:
                    while (index <= arrayLength - 1 && deadlineList.get(index).getDueTime() < deadline.getDueTime()) {
                        index++;
                    }
                    break;
            }
            if (index > deadlineList.size() - 1) {
                deadlineList.add(deadline);
            } else {
                deadlineList.add(index, deadline);
            }


        if (!isInitialize) {
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.getAdapter().notifyItemRangeChanged(0, deadlineList.size());
        }
    }

    public static void sortDDL() {
        if (deadlineList.size() <= 1) {
            return;
        }

        List<Deadline> tempDeadlineList = new ArrayList<>(deadlineList);
        int arrayLength = tempDeadlineList.size();
        int minIndex;
        switch (sortWay) {
            case SortByName:
                Collator instance = Collator.getInstance(Locale.CHINA);
                for (int i = 0; i < arrayLength - 1; i++) {
                    minIndex = i;
                    for (int j = i + 1; j < arrayLength; j++) {
                        if (instance.compare(tempDeadlineList.get(j).getDdlName(), tempDeadlineList.get(minIndex).getDdlName()) < 0) {
                            minIndex = j;
                        }
                    }
                    if (minIndex > i) {
                        Collections.swap(tempDeadlineList, i, minIndex);
                    }
                }
                break;
            case SortByTime:
                for (int i = 0; i < arrayLength - 1; i++) {
                    minIndex = i;
                    for (int j = i + 1; j < arrayLength; j++) {
                        if (tempDeadlineList.get(j).getDueTime() < tempDeadlineList.get(minIndex).getDueTime()) {
                            minIndex = j;
                        }
                    }
                    if (minIndex > i) {
                        Collections.swap(tempDeadlineList, i, minIndex);
                    }
                }
                break;
        }

        deadlineList.clear();
        deadlineList.addAll(tempDeadlineList);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * 找到最近的需要提醒的DDL（此DDL是从数据库中找到的）
     * @return 若有已过提前提醒但未过期的DDL，则返回该DDL；若无则返回提醒时间最早的DDL。
     */
    public static Deadline getDeadlineToAlarm() {
        //将要被返回的DDL
        Deadline deadlineToAlarm = null;

        List<Deadline> deadlineListOfDataBase = LitePal.findAll(Deadline.class);
        //筛选出尚未过期的DDL
        List<Deadline> validDeadline = new ArrayList<>();
        for (Deadline deadline : deadlineListOfDataBase) {
            if (!deadline.isAlarmed() && deadline.getDueTime() > Utility.getCalendar().getTimeInMillis() / Utility.millisecondsInMinute) {
                validDeadline.add(deadline);
            }
        }

        //筛选出提醒时间已过的DDL
        List<Deadline> alarmTimePastDeadlines = new ArrayList<>();
        for (Deadline deadline : validDeadline) {
            long alarmTime = deadline.getDueTime() - deadline.getAlarmTimeAhead();
            if (alarmTime < Utility.getCalendar().getTimeInMillis() / Utility.millisecondsInMinute) {
                alarmTimePastDeadlines.add(deadline);
            }
        }

        //选择应该寻找的列表
        List<Deadline> chosenList = alarmTimePastDeadlines.size() > 0 ? alarmTimePastDeadlines : validDeadline;
        //寻找提醒时间最早的DDL
        long earliestTime = Long.MAX_VALUE;
        for (Deadline deadline : chosenList) {
            long alarmTime = deadline.getDueTime() - deadline.getAlarmTimeAhead();
            if (alarmTime < earliestTime) {
                earliestTime = alarmTime;
                deadlineToAlarm = deadline;
            }
        }

        return deadlineToAlarm;
    }

    /**
     * 根据指定名称返回相应的DDL
     * @param deadlineName
     * @return
     */
    private Deadline getDeadlineOfName(String deadlineName) {
        for (Deadline deadline : deadlineList) {
            if (deadline.getDdlName().equals(deadlineName)) {
                return deadline;
            }
        }
        return null;
    }
}
