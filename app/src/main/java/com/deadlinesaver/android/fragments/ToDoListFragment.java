package com.deadlinesaver.android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.activities.AddBacklogActivity;
import com.deadlinesaver.android.activities.BaseActivity;
import com.deadlinesaver.android.activities.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class ToDoListFragment extends Fragment {

    private View mView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Fragment> fragments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.todolist_fragment, container, false);

        fragmentInitialize();

        return mView;
    }

    private void fragmentInitialize() {

        tabLayout = (TabLayout) mView.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) mView.findViewById(R.id.view_pager);

        initTabLayout();
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
        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
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

}
