package com.deadlinesaver.android.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.manager.DownloadManager;
import com.azhon.appupdate.utils.ApkUtil;
import com.deadlinesaver.android.MyApplication;
import com.deadlinesaver.android.UI.DraggableFab;
import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.R;
import com.deadlinesaver.android.db.Deadline;
import com.deadlinesaver.android.fragments.DDLFragment;
import com.deadlinesaver.android.fragments.DoneFragment;
import com.deadlinesaver.android.fragments.PersonalizedSettingsFragment;
import com.deadlinesaver.android.fragments.ToDoListFragment;
import com.deadlinesaver.android.fragments.UndoneFragment;
import com.deadlinesaver.android.gson.ApkInfo;
import com.deadlinesaver.android.services.DeadlineAlarmService;
import com.deadlinesaver.android.util.HttpUtil;
import com.deadlinesaver.android.UI.NoScrollViewPager;
import com.deadlinesaver.android.util.ToastUtil;
import com.deadlinesaver.android.util.Utility;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private final static String spName = "MainActivitySP";
    private final static String lastUpdateTimeName = "lastUpdateTime";
    private final static String sortWayName = "sortWay";

    private final static String address = "http://coldgoats.nat123.cc/ApkDownloader.json";
    private final static String oldVersionApkName_1 = "/TODOList.apk";
    private final static String oldVersionApkName_2 = "/DeadlineSaver.apk";
    private ApkInfo apkInfo;

    private NoScrollViewPager noScrollViewPager;
    private BottomNavigationView bottomNavigationView;
    private List<Fragment> fragmentList = new ArrayList<>();
    private Toolbar toolbar;
    private List<String> titles = new ArrayList<>();
    private DraggableFab fab;

    private MenuItem ddlSortByNameItem;
    private MenuItem ddlSortByTimeItem;
    public static SortWay sortWay;

    /**
     * 用于记录当前fragment类型，便于弹窗事件的触发
     */
    private FragmentType currentFragment = FragmentType.BacklogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //每日第一次打开app时更新今日待办
        Calendar todayCalendar = Utility.getTodayCalendar();
        SharedPreferences sharedPreferences = getSharedPreferences(spName, Context.MODE_PRIVATE);
        long lastUpdateTime = sharedPreferences.getLong(lastUpdateTimeName, todayCalendar.getTimeInMillis() - 1);
        //顺便也把用户指定的排序顺序给取出来
        sortWay = SortWay.values()[sharedPreferences.getInt(sortWayName, SortWay.SortByTime.ordinal())];
        if (lastUpdateTime <= todayCalendar.getTimeInMillis()) {
            //如果上次更新时间早于今日00：00的毫秒数，即今日尚未更新
            //进行更新操作
            LitePal.deleteAll(Backlog.class);
            List<Deadline> deadlineList = LitePal.findAll(Deadline.class);
            for (Deadline deadline : deadlineList) {
                long timeLeft = deadline.getDueTime() - Utility.getTodayCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
                if (timeLeft <= Utility.minutesInDay) {
                    Backlog backlog = new Backlog(deadline.getDdlName());
                    backlog.save();
                }
            }
            //保存本次更新时间
            lastUpdateTime = Utility.getCalendar().getTimeInMillis();
            SharedPreferences.Editor editor = getSharedPreferences(spName, Context.MODE_PRIVATE).edit();
            editor.putLong(lastUpdateTimeName, lastUpdateTime);
            editor.apply();
        }

        initOperations();

        //使DDLFragment开始计时
        DDLFragment.hasCreatedTimer = false;

        //启动定时提醒服务
        Intent intent = new Intent(this, DeadlineAlarmService.class);
        startService(intent);

        checkUpdate();

        //删除旧版本安装包
        boolean b = ApkUtil.deleteOldApk(this, getExternalCacheDir().getPath() + oldVersionApkName_1);
        boolean b1 = ApkUtil.deleteOldApk(this, getExternalCacheDir().getPath() + oldVersionApkName_2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case START_ADD_BACKLOG_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Backlog backlog = (Backlog) data.getSerializableExtra(BACKLOG_NAME);
                    UndoneFragment.addBacklog(backlog, false);
                }
                break;
            case START_ADD_DEADLINE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Deadline deadline = (Deadline) data.getSerializableExtra(DEADLINE_NAME);
                    DDLFragment.addDeadline(deadline, false);

                    //判断是否需要向今日待办事项中添加该DDL
                    long timeLeft = deadline.getDueTime() - Utility.getTodayCalendar().getTimeInMillis() / Utility.millisecondsInMinute;
                    if (timeLeft <= Utility.minutesInDay) {
                        Backlog backlog = new Backlog(deadline.getDdlName());
                        backlog.save();
                        UndoneFragment.addBacklog(backlog, false);
                    }

                    //强迫服务做一次检查
                    Intent intent = new Intent(this, DeadlineAlarmService.class);
                    startService(intent);
                }
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_toolbar, menu);
        ddlSortByNameItem = menu.findItem(R.id.ddl_sort_by_name);
        ddlSortByTimeItem = menu.findItem(R.id.ddl_sort_by_time);

        ddlSortByNameItem.setVisible(false);
        ddlSortByTimeItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ddl_sort_by_name:
                ddlSortByNameItem.setVisible(false);
                ddlSortByTimeItem.setVisible(true);
                sortWay = SortWay.SortByTime;
                ToastUtil.showToast(MainActivity.this, "按时间排序", Toast.LENGTH_SHORT);
                break;
            case R.id.ddl_sort_by_time:
                ddlSortByTimeItem.setVisible(false);
                ddlSortByNameItem.setVisible(true);
                sortWay = SortWay.SortByName;
                ToastUtil.showToast(MainActivity.this, "按名称排序", Toast.LENGTH_SHORT);
                break;
        }
        DDLFragment.sortDDL();

        //保存用户指定的排序方式
        SharedPreferences.Editor editor = getSharedPreferences(spName, Context.MODE_PRIVATE).edit();
        editor.putInt(sortWayName, sortWay.ordinal());
        editor.apply();

        return true;
    }

    /**
     * 所有初始化操作
     */
    private void initOperations() {
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_open_drawer);
        }

        initTitles();

        initBottomNavigationView();

        fab = findViewById(R.id.fab);
        //添加待办事项
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentFragment) {
                    case BacklogFragment:
                        Intent intent_backlog = new Intent
                            (MainActivity.this, AddBacklogActivity.class);
                        startActivityForResult(intent_backlog, BaseActivity.START_ADD_BACKLOG_ACTIVITY);
                        break;
                    case DeadlineFragment:
                        Intent intent_deadline = new Intent
                                (MainActivity.this, AddDeadlineActivity.class);
                        startActivityForResult(intent_deadline, BaseActivity.START_ADD_DEADLINE_ACTIVITY);
                        break;
                }
            }
        });
        fab.show();

        PersonalizedSettingsFragment.initializeSettingsData(MainActivity.this);

        initBacklogs();
        initDeadlines();
    }

    /**
     * 初始化应用各界面的标题
     */
    private void initTitles() {
        titles.add("今日待办事项");
        titles.add("我的DDL");
        titles.add("个性化设置");
    }

    /**
     * 底部导航栏的初始化
     */
    private void initBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        noScrollViewPager = findViewById(R.id.no_scroll_view_pager);

        fragmentList.add(new ToDoListFragment());
        fragmentList.add(new DDLFragment());
        fragmentList.add(new PersonalizedSettingsFragment());

        noScrollViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_today_todo:
                        noScrollViewPager.setCurrentItem(0);
                        toolbar.setTitle(titles.get(0));
                        currentFragment = FragmentType.BacklogFragment;
                        fab.show();
                        //隐藏排序按钮
                        ddlSortByNameItem.setVisible(false);
                        ddlSortByTimeItem.setVisible(false);
                        return true;
                    case R.id.item_all_ddl:
                        noScrollViewPager.setCurrentItem(1);
                        toolbar.setTitle(titles.get(1));
                        currentFragment = FragmentType.DeadlineFragment;
                        fab.show();
                        //显示排序按钮
                        switch (sortWay) {
                            case SortByName:
                                ddlSortByNameItem.setVisible(true);
                                ddlSortByTimeItem.setVisible(false);
                                break;
                            case SortByTime:
                                ddlSortByTimeItem.setVisible(true);
                                ddlSortByNameItem.setVisible(false);
                                break;
                        }
                        return true;
                    case R.id.item_personalized_settings:
                        noScrollViewPager.setCurrentItem(2);
                        toolbar.setTitle(titles.get(2));
                        fab.hide();
                        //隐藏排序按钮
                        ddlSortByNameItem.setVisible(false);
                        ddlSortByTimeItem.setVisible(false);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * 从数据库读取待办事项信息并写入
     */
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

    /**
     * 从数据库读取DDL信息并写入
     */
    private void initDeadlines() {
        List<Deadline> deadlineList = LitePal.findAll(Deadline.class);
        for (Deadline deadline : deadlineList) {
            DDLFragment.addDeadline(deadline, true);
        }
    }

    /**
     * 联网检测更新
     */
    private void checkUpdate() {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(MainActivity.this, "获取更新出错", Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                apkInfo = Utility.handleApkResponse(responseText);
                if (apkInfo == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MainActivity.this, "服务器还没开……",
                                    Toast.LENGTH_SHORT);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int versionCode = 0;
                            try {
                                Context context = MyApplication.getContext();
                                versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                                if (versionCode < apkInfo.versionCode) {
                                    updateNewApk();
                                } else {
                                    ToastUtil.showToast(MainActivity.this, "当前已是最新版本", Toast.LENGTH_LONG);
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                ToastUtil.showToast(MainActivity.this, "出错...", Toast.LENGTH_LONG);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 下载安装新版apk
     */
    private void updateNewApk() {
        UpdateConfiguration configuration = new UpdateConfiguration()
                //输出错误日志
                .setEnableLog(true)
                //设置自定义的下载
                //.setHttpManager()
                //下载完成自动跳动安装页面
                .setJumpInstallPage(true)
                //设置对话框背景图片 (图片规范参照demo中的示例图)
                //.setDialogImage(R.drawable.ic_dialog)
                //设置按钮的颜色
                //.setDialogButtonColor(Color.parseColor("#E743DA"))
                //设置对话框强制更新时进度条和文字的颜色
                //.setDialogProgressBarColor(Color.parseColor("#E743DA"))
                //设置按钮的文字颜色
                .setDialogButtonTextColor(Color.WHITE)
                //设置是否显示通知栏进度
                .setShowNotification(true)
                //设置是否提示后台下载toast
                .setShowBgdToast(true)
                //设置强制更新
                .setForcedUpgrade(false);
                //设置对话框按钮的点击监听
                //.setButtonClickListener(this)
                //设置下载过程的监听
                //.setOnDownloadListener(this)

        DownloadManager manager = DownloadManager.getInstance(MainActivity.this);
        manager.setApkName(apkInfo.fileName)
                .setApkUrl(apkInfo.apkUrl)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setConfiguration(configuration)
                .setShowNewerToast(true)
                .setApkVersionCode(apkInfo.versionCode)
                .setApkVersionName(apkInfo.versionName)
                .setApkSize(apkInfo.apkSize)
                .setApkDescription(apkInfo.versionInfo)
                .download();
    }

    /**
     * 用于指示当前Fragment类型
     */
    private enum FragmentType {
        BacklogFragment,
        DeadlineFragment
    }

    /**
     * 用于指示当前排序方式
     */
    public enum SortWay {
        SortByName,
        SortByTime
    }
}