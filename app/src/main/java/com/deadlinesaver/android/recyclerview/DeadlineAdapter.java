package com.deadlinesaver.android.recyclerview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.activities.EditDeadlineActivity;
import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.db.Deadline;
import com.deadlinesaver.android.fragments.DDLFragment;
import com.deadlinesaver.android.fragments.DoneFragment;
import com.deadlinesaver.android.fragments.UndoneFragment;
import com.deadlinesaver.android.util.ToastUtil;
import com.deadlinesaver.android.util.Utility;
import com.deadlinesaver.android.util.VibrateUtil;
import com.google.android.material.snackbar.Snackbar;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DeadlineAdapter extends CustomBaseAdapter<DeadlineAdapter.ViewHolder> {

    private static final int minutesOfDay = 24 * 60;
    private static final int minutesOfHour = 60;

    private static final String timeFormat_day = "%d天";
    private static final String timeFormat_hour_minute = "%02d:%02d";

    public static final String timeOver = "--:--";
    public static final String timeOverNameSuffix = "(已逾期)";

    private Activity mActivity;
    private View mView;
    private List<Deadline> mDeadlineList;

    //用于记录右极值时的左边距，以及判断是否已经固定了左边距
    private static int fixedLeftMargin; private static boolean isLeftMarginFixed = false;

    //用于记录最近一次删除的DDL
    private String lastDeadlineName;
    private long lastDeadlineDueTime;
    private long lastDeadlineTotalTime;

    @Override
    public void onItemMove(int fromPos, int toPos) {
        Collections.swap(mDeadlineList, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
    }

    @Override
    public void onItemRemove(final int position) {
        SweetAlertDialog dialog = new SweetAlertDialog(mActivity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("您准备删除这个DDL")
                .setContentText("确定执行该操作吗？")
                .setConfirmText("是的，我很确定！")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        removeItem(position);

                        sweetAlertDialog.setTitleText("DDL已删除！")
                                .setContentText("如果反悔请点击下方按钮撤销")
                                .setConfirmText("好的没事了")
                                .setConfirmClickListener(null)
                                .setCancelText("快撤销！")
                                .showCancelButton(true)
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        restoreItem(lastDeadlineDueTime);
                                        sweetAlertDialog.dismissWithAnimation();
                                        ToastUtil.showToast(mActivity, "已撤销", Toast.LENGTH_SHORT);
                                    }
                                })
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        notifyItemRangeChanged(0, mDeadlineList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView deadlineNameTextView;
        public RadioButton radioButton;
        public TextView deadlineTimeTextView;
        public SeekBar seekBar;

        public ViewHolder(View view) {
            super(view);
            deadlineNameTextView = view.findViewById(R.id.ddl_name_text_view);
            deadlineTimeTextView = view.findViewById(R.id.ddl_left_time_text_view);
            radioButton = view.findViewById(R.id.ddl_is_done);
            seekBar = view.findViewById(R.id.ddl_progress);
        }
    }

    public DeadlineAdapter(Activity activity, View view, List<Deadline> deadlines) {
        mActivity = activity;
        mView = view;
        mDeadlineList = deadlines;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deadline_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                VibrateUtil.vibrate(mActivity, 50);
                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position >= 0) {
                    Deadline deadline = LitePal.find(Deadline.class, mDeadlineList.get(position).getId());
                    UndoneFragment.removeBacklogOfName(deadline.getDdlName());
                    DoneFragment.removeBacklogOfName(deadline.getDdlName());
                    EditDeadlineActivity.startActivity(mActivity, deadline);
                }
            }
        });

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int maxWidth = parent.getWidth();
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)
                                holder.deadlineTimeTextView.getLayoutParams();
                        int progress = holder.seekBar.getProgress();
                        String text = holder.deadlineTimeTextView.getText().toString();
                        float textWidth = holder.deadlineTimeTextView.getPaint().measureText(text);

                        //计算理论上的左边距
                        int newLeftMargin = (int) ((maxWidth - textWidth) * (float) progress / 100) - (int) (textWidth / 3.6f);

                        if (newLeftMargin <= 5) {
                            params.leftMargin = 5;
                        } else if (newLeftMargin + Math.pow(textWidth, 0.7) * 7.6f >= maxWidth - 5) {
                            if (!isLeftMarginFixed) {
                                isLeftMarginFixed = true;
                                fixedLeftMargin = newLeftMargin;
                            }
                            params.leftMargin = fixedLeftMargin;
                        } else {
                            params.leftMargin = newLeftMargin;
                            isLeftMarginFixed = false;
                        }

                        holder.deadlineTimeTextView.setLayoutParams(params);
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position >= 0) {//防止连续点击出现返回值为-1的情况
                    removeItem(position);

                    if (holder.deadlineTimeTextView.getText().toString().equals(timeOver)) {
                        //如果此时DDL已经过期
                        SweetAlertDialog dialog = new SweetAlertDialog(mActivity, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("很遗憾，你超时了");
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.show();
                    } else {
                        //如果尚未过期
                        SweetAlertDialog dialog = new SweetAlertDialog(mActivity, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("完成DDL啦！")
                                .setConfirmText("好的")
                                .setConfirmClickListener(null)
                                .setCancelText("诶没好呢")
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        restoreItem(lastDeadlineDueTime);

                                        sweetAlertDialog.setTitleText("DDL又回来了...")
                                                .showCancelButton(false)
                                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                    }
                                });
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.show();
                    }
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Deadline deadline = LitePal.find(Deadline.class, mDeadlineList.get(position).getId());
        holder.radioButton.setChecked(false);

        String timeText = getTimeText(deadline.getDueTime());
        holder.deadlineTimeTextView.setText(timeText);

        String deadlineName = deadline.getDdlName();
        if (timeText.equals(timeOver)) {
            deadlineName += timeOverNameSuffix;
        }
        holder.deadlineNameTextView.setText(deadlineName);

        //获取时间进度
        long timeLeft = deadline.getDueTime() - Utility.getCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
        int progress = (int) (((deadline.getTotalTime() - timeLeft) / (float) deadline.getTotalTime()) * 100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.seekBar.setProgress(progress, true);
        } else {
            holder.seekBar.setProgress(progress);
        }
    }

    @Override
    public int getItemCount() {
        return mDeadlineList.size();
    }

    public static String getTimeText(long dueTime) {
        Calendar currentTimeCalendar = Utility.getCalendar();
        long timeLeft = dueTime - currentTimeCalendar.getTimeInMillis()/Utility.millisecondsInMinute;
        String timeText = "";
        if (timeLeft > 0) {
            if (timeLeft > minutesOfDay) {
                timeLeft /= minutesOfDay;
                timeText = String.format(timeFormat_day, timeLeft);
            } else {
                int hourLeft = (int) timeLeft / minutesOfHour;
                timeLeft %= minutesOfHour;
                timeText = String.format(timeFormat_hour_minute, hourLeft, timeLeft);
            }
        } else {
            timeText = timeOver;
        }
        return timeText;
    }

    private void removeItem(int position) {
        //储存被删除DDL的数据
        Deadline lastDeadline = LitePal.find(Deadline.class, mDeadlineList.get(position).getId());
        lastDeadlineName = lastDeadline.getDdlName();
        lastDeadlineDueTime = lastDeadline.getDueTime();
        lastDeadlineTotalTime = lastDeadline.getTotalTime();

        //把DDL对应的代办事件删除
        UndoneFragment.removeBacklogOfName(lastDeadline.getDdlName());
        DoneFragment.removeBacklogOfName(lastDeadline.getDdlName());

        //删除DDL
        mDeadlineList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mDeadlineList.size());
        LitePal.delete(Deadline.class, lastDeadline.getId());
    }

    private void restoreItem(long dueTime) {
        Deadline lastDeadline = new Deadline(lastDeadlineName, dueTime, lastDeadlineTotalTime);
        DDLFragment.addDeadline(lastDeadline, false);
        notifyItemRangeChanged(0, mDeadlineList.size());
        lastDeadline.save();

        //判断是否需要向今日待办事项中添加该DDL
        long timeLeft = lastDeadline.getDueTime() - Utility.getTodayCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
        if (timeLeft <= Utility.minutesInDay) {
            Backlog backlog = new Backlog(lastDeadline.getDdlName());
            backlog.save();
            UndoneFragment.addBacklog(backlog, false);
        }
    }
}
