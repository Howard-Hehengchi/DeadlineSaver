package com.deadlinesaver.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.deadlinesaver.android.Backlog;
import com.deadlinesaver.android.R;
import com.google.android.material.textfield.TextInputEditText;

public class AddBacklogActivity extends BaseActivity {

    public static final String BACKLOG_NAME = "backlog_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_backlog);
        setFinishOnTouchOutside(true);

        Button confirm = (Button) findViewById(R.id.add_backlog_confirm);
        Button cancel = (Button) findViewById(R.id.add_backlog_cancel);
        final TextInputEditText textInput = (TextInputEditText) findViewById(R.id.text_input);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textInput.getText() != null){
                    Intent intent = new Intent();
                    Backlog backlog = new Backlog(textInput.getText().toString());
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
