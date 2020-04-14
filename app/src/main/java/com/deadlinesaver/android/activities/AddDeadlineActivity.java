package com.deadlinesaver.android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.db.Deadline;
import com.deadlinesaver.android.util.Utility;

import java.util.Calendar;

public class AddDeadlineActivity extends BaseActivity{

    private EditText deadlineNameInput;
    private TextView dateTextView;
    private ImageView chooseDateIcon;
    private TextView timeTextView;
    private ImageView chooseTimeIcon;
    private Button confirm;
    private Button cancel;

    //用于记录当前时间
    private int year;
    private int month;
    private int dayOfMonth;
    private int hourOfDay;
    private int minute;

    //用于记录最近一次修改的时间
    private int year_last;
    private int month_last;
    private int dayOfMonth_last;
    private int hourOfDay_last;
    private int minute_last;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deadline);
        setFinishOnTouchOutside(true);

        //设置dialog宽度为屏幕的90%
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (display.getWidth() * 0.9);
        getWindow().setAttributes(lp);

        initWidgets();

        refreshTime();

        saveLastTime(year, month, dayOfMonth, hourOfDay, minute);

        //显示当前日期
        setDate(year, month, dayOfMonth);
        //显示当前时间
        setTime(hourOfDay, minute);

        chooseDateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog
                            (AddDeadlineActivity.this, 0, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, final int year_new, final int month_new, final int dayOfMonth_new) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setDate(year_new, month_new, dayOfMonth_new);
                                        }
                                    });
                                    saveLastTime(year_new, month_new, dayOfMonth_new, hourOfDay_last, minute_last);
                                }
                            }, year_last, month_last, dayOfMonth_last);
                    datePickerDialog.show();
                }
            }
        });

        chooseTimeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddDeadlineActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, final int hourOfDay_new, final int minute_new) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTime(hourOfDay_new, minute_new);
                            }
                        });
                        saveLastTime(year_last, month_last, dayOfMonth_last, hourOfDay_new, minute_new);
                    }
                }, hourOfDay_last, minute_last, true);
                timePickerDialog.show();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deadlineName = deadlineNameInput.getText().toString();
                if (!deadlineName.equals("")) {
                    long dueTime = getDueTime(year_last, month_last, dayOfMonth_last, hourOfDay_last, minute_last);
                    if (dueTime != -1) {
                        long totalTime = dueTime - Utility.getCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
                        Intent intent = new Intent();
                        Deadline deadline = new Deadline(deadlineName, dueTime, totalTime);
                        deadline.save();
                        intent.putExtra(DEADLINE_NAME, deadline);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(AddDeadlineActivity.this, "请给DDL设置一个有效时间！", Toast.LENGTH_LONG).show();
                        refreshTime();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setDate(year, month, dayOfMonth);
                                setTime(hourOfDay, minute);
                            }
                        });
                        saveLastTime(year, month, dayOfMonth, hourOfDay, minute);
                    }
                } else {
                    Toast.makeText(AddDeadlineActivity.this, "请给DDL起一个名字！", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * 初始化各控件
     */
    private void initWidgets() {
        deadlineNameInput = findViewById(R.id.add_deadline_edit_text);
        dateTextView = findViewById(R.id.add_deadline_date);
        chooseDateIcon = findViewById(R.id.add_deadline_choose_date);
        timeTextView = findViewById(R.id.add_deadline_time);
        chooseTimeIcon = findViewById(R.id.add_deadline_choose_time);
        confirm = findViewById(R.id.add_deadline_confirm);
        cancel = findViewById(R.id.add_deadline_cancel);
    }

    /**
     * 更新当前时间
     */
    private void refreshTime() {
        Calendar calendars = Utility.getCalendar();
        year = calendars.get(Calendar.YEAR);
        month = calendars.get(Calendar.MONTH);
        dayOfMonth = calendars.get(Calendar.DATE);
        hourOfDay = calendars.get(Calendar.HOUR_OF_DAY);
        minute = calendars.get(Calendar.MINUTE);
    }

    /**
     * 保存最近一次修改的时间
     */
    private void saveLastTime(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        year_last = year;
        month_last = month;
        dayOfMonth_last = dayOfMonth;
        hourOfDay_last = hourOfDay;
        minute_last = minute;
    }

    private void setDate(int year, int month, int dayOfMonth) {
        month++;
        StringBuilder builder = new StringBuilder();
        builder.append(year).append("-").append(month).append("-").append(dayOfMonth);
        if (this.dayOfMonth == dayOfMonth && this.month + 1 == month && this.year == year) {
            builder.append("(今天)");
        }
        dateTextView.setText(builder.toString());
    }

    private void setTime(int hourOfDay, int minute) {
        String hourString = "" + hourOfDay;
        String minuteString = "" + minute;
        if (hourOfDay < 10) {
            hourString = "0" + hourString;
        }
        if (minute < 10) {
            minuteString = "0" + minuteString;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(hourString).append(":").append(minuteString);
        timeTextView.setText(builder.toString());
    }

    /**
     * 通过给定时间换算时间数
     * @param year
     * @param month
     * @param dayOfMonth
     * @param hourOfDay
     * @param minute
     * @return 以分钟为单位的预计截止时间
     */
    private long getDueTime(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        Calendar currentTimeCalendar = Utility.getCalendar();
        long currentTime = currentTimeCalendar.getTimeInMillis();
        currentTime /= Utility.millisecondsInMinute;

        Calendar targetTimeCalendar = Utility.getCalendar(year, month, dayOfMonth, hourOfDay, minute);
        long targetTime = targetTimeCalendar.getTimeInMillis();
        targetTime /= Utility.millisecondsInMinute;

        if (currentTime >= targetTime) {
            return -1;
        } else {
            return targetTime;
        }
    }

    private static final String TAG = "AddDeadlineActivity";
}
