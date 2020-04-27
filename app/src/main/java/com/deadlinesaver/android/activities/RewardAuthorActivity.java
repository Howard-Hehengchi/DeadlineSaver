package com.deadlinesaver.android.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.personalData.ImportantData;
import com.deadlinesaver.android.util.ToastUtil;
import com.hanks.htextview.base.AnimationListener;
import com.hanks.htextview.base.HTextView;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class RewardAuthorActivity extends BaseActivity {

    private View screenView;

    private HTextView hTextView;
    private Button button;

    private ImageView imageView;

    private ViewGroup switchableLayout;

    /**
     * 一个用于指示当前界面类型的boolean值
     * true: 支付宝
     * false: 微信
     */
    private static boolean isAlipay = true;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_author);

        Toolbar toolbar = findViewById(R.id.reward_author_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        switchableLayout = findViewById(R.id.reward_author_switchable_layout);

        screenView = findViewById(R.id.reward_author_view);
        screenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAlipay = !isAlipay;
                if (isAlipay) {
                    screenView.setBackgroundColor(getResources().getColor(R.color.themeColorLight));

                    LinearLayout layout = (LinearLayout) LayoutInflater.from(RewardAuthorActivity.this).inflate(R.layout.alipay_layout, switchableLayout, false);
                    switchableLayout.removeAllViews();
                    switchableLayout.addView(layout);

                    button = layout.findViewById(R.id.reward_author_button);
                    button.setVisibility(View.INVISIBLE);

                    hTextView = layout.findViewById(R.id.alipay_text_view);
                    hTextView.animateText(getString(R.string.alipay_hint_text));
                    hTextView.setAnimationListener(new SimpleAnimationListener(RewardAuthorActivity.this));

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (openAlipayPayPage(ImportantData.alipayQRurl)) {
                                ToastUtil.showToast(RewardAuthorActivity.this, "启动成功", Toast.LENGTH_SHORT);
                            } else {
                                ToastUtil.showToast(RewardAuthorActivity.this, "哎呀……出错了，要不再试试？", Toast.LENGTH_SHORT);
                            }
                        }
                    });
                } else {
                    screenView.setBackgroundColor(getResources().getColor(R.color.themeColorContrastLight));

                    LinearLayout layout = (LinearLayout) LayoutInflater.from(RewardAuthorActivity.this).inflate(R.layout.wechat_pay_layout, switchableLayout, false);
                    switchableLayout.removeAllViews();
                    switchableLayout.addView(layout);

                    imageView = layout.findViewById(R.id.reward_author_image_view);
                    imageView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            ToastUtil.showToast(RewardAuthorActivity.this, "正在跳转...", Toast.LENGTH_SHORT);
                            startWeChatPay(RewardAuthorActivity.this, imageView);
                            return true;
                        }
                    });
                }
            }
        });

        LinearLayout layout = (LinearLayout) LayoutInflater.from(RewardAuthorActivity.this).inflate(R.layout.alipay_layout, switchableLayout, false);
        switchableLayout.removeAllViews();
        switchableLayout.addView(layout);

        button = layout.findViewById(R.id.reward_author_button);
        button.setVisibility(View.INVISIBLE);

        hTextView = layout.findViewById(R.id.alipay_text_view);
        hTextView.animateText(getString(R.string.alipay_hint_text));
        hTextView.setAnimationListener(new SimpleAnimationListener(this));


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (openAlipayPayPage(ImportantData.alipayQRurl)) {
                    ToastUtil.showToast(RewardAuthorActivity.this, "启动成功", Toast.LENGTH_SHORT);
                } else {
                    ToastUtil.showToast(RewardAuthorActivity.this, "哎呀……出错了，要不再试试？", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private boolean openAlipayPayPage(String qrCode) {
        try {
            qrCode = URLEncoder.encode(qrCode, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + qrCode;
            openUri(alipayqr + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void openUri(String s) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
        startActivity(intent);
    }

    private static void startWeChatPay(Context context, final View view) {
        File dir = context.getExternalFilesDir("pay_img");
        if (dir != null &&
                !dir.exists() && !dir.mkdirs()) {
            return;
        } else {
            File[] f = dir.listFiles();
            for (File file : f) {
                file.delete();
            }
        }

        String fileName = System.currentTimeMillis() + "weixin_qa.png";
        final File file = new File(dir, fileName);

        new AsyncTask<Context, String, String>() {
            Context context;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Context... c) {
                context = c[0];
                snapShot(context, file, view);
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                startWechat(context);
            }
        }.execute(context);
    }

    private static void snapShot(Context context, @NonNull File file, @NonNull View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        view.draw(canvas);

        FileOutputStream fos = null;
        boolean isSuccess = false;
        try {
            fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(fos);
        }
        if (isSuccess) {
            ContentResolver contentResolver = context.getContentResolver();
            ContentValues values = new ContentValues(4);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.ORIENTATION, 0);
            values.put(MediaStore.Images.Media.TITLE, "打赏");
            values.put(MediaStore.Images.Media.DESCRIPTION, "打赏码");
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
            Uri url = null;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.grantUriPermission(context.getPackageName(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                url = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); //其实质是返回 Image.Meida.DATA中图片路径path的转变而成的uri
                OutputStream imageOut = contentResolver.openOutputStream(url);
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOut);
                } finally {
                    closeIO(imageOut);
                }

                long id = ContentUris.parseId(url);
                MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);//获取缩略图

            } catch (Exception e) {
                if (url != null) {
                    contentResolver.delete(url, null, null);
                }
            }
        }
    }

    private static void startWechat(Context context) {
        try {
            //利用Intent打开微信
            //Uri uri = Uri.parse("weixin://dl/scan");
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            context.startActivity(intent);
        } catch (Exception e) {
            //若无法正常跳转，在此进行错误处理
            ToastUtil.showToast(context, "无法跳转到微信，请检查是否安装了微信", Toast.LENGTH_SHORT);
        }
    }

    private static void closeIO(Closeable target) {
        try {
            if (target != null)
                target.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SimpleAnimationListener implements AnimationListener {

        private Context context;

        public SimpleAnimationListener(Context context) {
            this.context = context;
        }

        @Override
        public void onAnimationEnd(HTextView hTextView) {
            button.setVisibility(View.VISIBLE);
        }
    }
}
