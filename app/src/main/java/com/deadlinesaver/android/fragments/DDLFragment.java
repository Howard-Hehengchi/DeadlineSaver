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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Objects;

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
                                        Log.i(TAG, "UIChange: " + progress);
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

    private static final String TAG = "DDLFragment";

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
        deadlineList.add(deadline);
        if (!isInitialize) {
            recyclerView.getAdapter().notifyItemRangeChanged(0, deadlineList.size());
        }
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
