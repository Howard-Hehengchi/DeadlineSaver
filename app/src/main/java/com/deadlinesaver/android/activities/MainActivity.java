package com.deadlinesaver.android.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.R;
import com.deadlinesaver.android.fragments.DoneFragment;
import com.deadlinesaver.android.fragments.UndoneFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fab;
    private List<Fragment> fragments;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initOperations();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case START_ADD_BACKLOG_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Backlog backlog = (Backlog) data.getSerializableExtra(AddBacklogActivity.BACKLOG_NAME);
                    UndoneFragment.addBacklog(backlog, false);
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

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        initTabLayout();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        //添加待办事项
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent
                        (MainActivity.this, AddBacklogActivity.class);
                startActivityForResult(intent, START_ADD_BACKLOG_ACTIVITY);
            }
        });

        initBacklogs();
    }

    private void initTabLayout() {
        //初始化标题名称
        final List<String> titles = new ArrayList<>();
        titles.add("未完成");
        titles.add("已完成");

        //初始化fragment数据
        fragments = new ArrayList<>();
        fragments.add(new UndoneFragment());
        fragments.add(new DoneFragment());

        //初始化PagerAdapter
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }

    private void initBacklogs() {
        List<Backlog> backlogList = LitePal.findAll(Backlog.class);
        for (Backlog backlog : backlogList) {
            if (backlog.isDone()) {
                DoneFragment.addBacklog(backlog, true);
            } else {
                UndoneFragment.addBacklog(backlog, true);
            }
        }
    }
}