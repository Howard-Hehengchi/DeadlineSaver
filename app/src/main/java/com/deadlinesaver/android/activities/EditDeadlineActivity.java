package com.deadlinesaver.android.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.db.Deadline;
import com.deadlinesaver.android.fragments.DDLFragment;
import com.deadlinesaver.android.fragments.UndoneFragment;
import com.deadlinesaver.android.util.DensityUtil;
import com.deadlinesaver.android.util.ToastUtil;
import com.deadlinesaver.android.util.Utility;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.litepal.LitePal;

import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditDeadlineActivity extends BaseActivity {

    private final static String deadlineExtraName = "Deadline";

    private final static String dateStringFormat = "%d-%d-%d";
    private final static String timeStringFormat = "%02d:%02d";

    private Toolbar toolbar;

    private TextView dueDateTextView;
    private TextView dueTimeTextView;
    private TextView deadlineNameTextView;
    private EditText deadlineContentEditText;
    private LinearLayout deadlineNameLayout;

    private EditText editDeadlineNameEditText;
    private TextView cancelTextView;
    private TextView confirmTextView;

    private int due_year;
    private int due_month;
    private int due_day;
    private int due_hour;
    private int due_minute;

    private Deadline deadlineInDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deadline);

        initWidgets();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_24dp);
        }

        dueDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog
                            (EditDeadlineActivity.this, 0, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, final int year_new, final int month_new, final int dayOfMonth_new) {
                                    if (!isValid(year_new, month_new, dayOfMonth_new)) {
                                        ToastUtil.showToast(EditDeadlineActivity.this, "请选择一个有效日期！", Toast.LENGTH_SHORT);
                                    } else {
                                        due_year = year_new;
                                        due_month = month_new;
                                        due_day = dayOfMonth_new;
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String dueDateString = String.format(dateStringFormat, due_year, due_month + 1, due_day);
                                            dueDateTextView.setText(dueDateString);
                                        }
                                    });
                                }
                            }, due_year, due_month, due_day);
                    datePickerDialog.show();
                }
            }
        });

        dueTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditDeadlineActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, final int hourOfDay_new, final int minute_new) {
                        if (!isValid(hourOfDay_new, minute_new)) {
                            ToastUtil.showToast(EditDeadlineActivity.this, "请选择一个有效时间！", Toast.LENGTH_SHORT);
                        } else {
                            due_hour = hourOfDay_new;
                            due_minute = minute_new;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String dueTimeString = String.format(timeStringFormat, due_hour, due_minute);
                                dueTimeTextView.setText(dueTimeString);
                            }
                        });
                    }
                }, due_hour, due_minute, true);
                timePickerDialog.show();
            }
        });

        deadlineNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog bottomDialog = new Dialog(EditDeadlineActivity.this, R.style.BottomDialog);
                View contentView = LayoutInflater.from(EditDeadlineActivity.this).inflate(R.layout.edit_deadline_name_dialog, null);
                bottomDialog.setContentView(contentView);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
                params.width = getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(EditDeadlineActivity.this, 40);
                params.bottomMargin = DensityUtil.dp2px(EditDeadlineActivity.this, 24);
                contentView.setLayoutParams(params);
                bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
                bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

                //接下来初始化底部弹出Dialog内的控件
                editDeadlineNameEditText = bottomDialog.findViewById(R.id.edit_deadline_name_dialog_edit_text);
                cancelTextView = bottomDialog.findViewById(R.id.edit_deadline_name_dialog_cancel_text_view);
                confirmTextView = bottomDialog.findViewById(R.id.edit_deadline_name_dialog_confirm_text_view);

                editDeadlineNameEditText.setText(deadlineNameTextView.getText().toString());
                confirmTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newDeadlineName = editDeadlineNameEditText.getText().toString();
                        if (!newDeadlineName.equals("")) {
                            deadlineNameTextView.setText(newDeadlineName);
                            bottomDialog.dismiss();
                        }
                    }
                });

                cancelTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomDialog.dismiss();
                    }
                });

                //弹出Dialog
                bottomDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_deadline_activity_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_deadline_check_icon:
                saveInfo();
                finish();
                break;
            case android.R.id.home:
                exitHint();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        exitHint();
    }

    /**
     * 初始化各组件内容
     */
    private void initWidgets() {
        //获取上个活动传来的Deadline的信息
        Deadline deadline = (Deadline) getIntent().getExtras().get(deadlineExtraName);
        //根据此Deadline寻找数据库中存储的Deadline，防止save时额外增加DDL数量
        deadlineInDataBase = LitePal.find(Deadline.class, deadline.getId());

        toolbar = findViewById(R.id.edit_deadline_toolBar);
        setSupportActionBar(toolbar);

        dueDateTextView = findViewById(R.id.edit_deadline_due_date);
        dueTimeTextView = findViewById(R.id.edit_deadline_due_time);
        deadlineNameTextView = findViewById( R.id.edit_deadline_deadline_name_text_view);
        deadlineContentEditText = findViewById(R.id.edit_deadline_deadline_content_edit_text);
        deadlineNameLayout = findViewById(R.id.edit_deadline_deadline_name_layout);

        //获取截止时间的Calendar对象
        Calendar dueTimeCalendar = Calendar.getInstance();
        dueTimeCalendar.setTimeInMillis(deadline.getDueTime() * Utility.millisecondsInMinute);
        due_year = dueTimeCalendar.get(Calendar.YEAR);
        due_month = dueTimeCalendar.get(Calendar.MONTH);
        due_day = dueTimeCalendar.get(Calendar.DAY_OF_MONTH);
        due_hour = dueTimeCalendar.get(Calendar.HOUR_OF_DAY);
        due_minute = dueTimeCalendar.get(Calendar.MINUTE);

        String dueDateText = String.format(dateStringFormat, due_year, due_month + 1, due_day);
        String dueTimeText = String.format(timeStringFormat, due_hour, due_minute);

        dueDateTextView.setText(dueDateText);
        dueTimeTextView.setText(dueTimeText);

        deadlineNameTextView.setText(deadline.getDdlName());
        if (!deadline.getDdlContent().equals("")) {
            deadlineContentEditText.setText(deadline.getDdlContent());
        }
    }

    /**
     * 根据指定时间判断是否有效
     * @param year
     * @param month
     * @param day
     * @return
     */
    private boolean isValid(int year, int month, int day) {
        Calendar currentTimeCalendar = Utility.getCalendar();
        int currentYear = currentTimeCalendar.get(Calendar.YEAR);
        int currentMonth = currentTimeCalendar.get(Calendar.MONTH) + 1;
        int currentDay = currentTimeCalendar.get(Calendar.DAY_OF_MONTH);
        return currentDay <= day || currentMonth < month || currentYear < year;
    }

    /**
     * 根据指定时间判断是否有效
     * @param hour
     * @param minute
     * @return
     */
    private boolean isValid(int hour, int minute) {
        Calendar currentTimeCalendar = Utility.getCalendar();
        int currentHour = currentTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTimeCalendar.get(Calendar.MINUTE);
        return currentHour < hour || currentMinute <= minute;
    }

    /**
     * 用于保存修改后的DDL信息
     */
    private void saveInfo() {
        deadlineInDataBase.setDdlName(deadlineNameTextView.getText().toString());
        long totalTime = deadlineInDataBase.getTotalTime();
        long dueTime = Utility.getCalendar(due_year, due_month, due_day, due_hour, due_minute).getTimeInMillis() / Utility.millisecondsInMinute;
        totalTime += dueTime - deadlineInDataBase.getDueTime();
        deadlineInDataBase.setDueTime(dueTime);
        deadlineInDataBase.setTotalTime(totalTime);
        deadlineInDataBase.setDdlContent(deadlineContentEditText.getText().toString());
        deadlineInDataBase.save();

        //判断是否需要向今日待办事项中添加该DDL
        long timeLeft = deadlineInDataBase.getDueTime() - Utility.getTodayCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
        if (timeLeft <= Utility.minutesInDay) {
            Backlog backlog = new Backlog(deadlineInDataBase.getDdlName());
            backlog.save();
            UndoneFragment.addBacklog(backlog, false);
        }
    }

    /**
     * 当用户要退出时弹出提示
     */
    private void exitHint() {
        final SweetAlertDialog dialog = new SweetAlertDialog(EditDeadlineActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("是否保存修改后的内容？")
                .setConfirmText("保存")
                .setCancelText("放弃")
                .showCancelButton(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                saveInfo();
                dialog.dismissWithAnimation();
                finish();
            }
        });
        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismissWithAnimation();
                //判断是否需要向今日待办事项中添加该DDL
                long timeLeft = deadlineInDataBase.getDueTime() - Utility.getTodayCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
                if (timeLeft <= Utility.minutesInDay) {
                    Backlog backlog = new Backlog(deadlineInDataBase.getDdlName());
                    backlog.save();
                    UndoneFragment.addBacklog(backlog, false);
                }
                finish();
            }
        });
        dialog.show();
    }

    public static void startActivity(Context context, Deadline deadline) {
        Intent intent = new Intent(context, EditDeadlineActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(deadlineExtraName, deadline);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
