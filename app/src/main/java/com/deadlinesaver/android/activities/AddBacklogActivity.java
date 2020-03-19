package com.deadlinesaver.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.R;
import com.google.android.material.textfield.TextInputEditText;

public class AddBacklogActivity extends BaseActivity {

    public static final String BACKLOG_NAME = "backlog_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_backlog);
        setFinishOnTouchOutside(true);

        //设置dialog宽度为屏幕的80%
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (display.getWidth() * 0.8);
        getWindow().setAttributes(lp);

        Button confirm = (Button) findViewById(R.id.add_backlog_confirm);
        Button cancel = (Button) findViewById(R.id.add_backlog_cancel);
        final TextInputEditText textInput = (TextInputEditText) findViewById(R.id.text_input);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!textInput.getText().toString().equals("")){
                    Intent intent = new Intent();
                    Backlog backlog = new Backlog(textInput.getText().toString());
                    backlog.save();
                    intent.putExtra(BACKLOG_NAME, backlog);
                    setResult(RESULT_OK, intent);
                    finish();
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
}
