package com.deadlinesaver.android.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewDebug;

import com.deadlinesaver.android.Backlog;
import com.deadlinesaver.android.BacklogAdapter;
import com.deadlinesaver.android.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends BaseActivity {

    private List<Backlog> backlogList = new ArrayList<>();
    RecyclerView recyclerView;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initOperations();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent
                        (MainActivity.this, AddBacklogActivity.class);
                startActivityForResult(intent, START_ADD_BACKLOG_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case START_ADD_BACKLOG_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Backlog backlog = (Backlog) data.getSerializableExtra(AddBacklogActivity.BACKLOG_NAME);
                    backlogList.add(backlog);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 所有初始化操作
     */
    private void initOperations() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        initBacklogRecyclerView();
    }

    private void initBacklogRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.backlog_recycler_view);

        //set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //set adapter
        BacklogAdapter adapter = new BacklogAdapter(backlogList);
        recyclerView.setAdapter(adapter);
    }
}