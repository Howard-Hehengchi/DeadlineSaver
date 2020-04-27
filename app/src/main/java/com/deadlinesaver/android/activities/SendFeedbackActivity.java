package com.deadlinesaver.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.personalData.ImportantData;
import com.deadlinesaver.android.util.ToastUtil;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.deadlinesaver.android.personalData.ImportantData.authorizationCode;
import static com.deadlinesaver.android.personalData.ImportantData.fromAddress;
import static com.deadlinesaver.android.personalData.ImportantData.toAddress;

public class SendFeedbackActivity extends BaseActivity {

    private static boolean isFirstExit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //联网程序，需要加上StrictMode
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_feedback);

        Toolbar toolbar = findViewById(R.id.send_feedback_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final EditText editText = findViewById(R.id.send_feedback_edit_text);
        Button button = findViewById(R.id.send_feedback_send_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String text = editText.getText().toString();
                if (!text.equals("")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                javaMailSendSimpleEmail(text);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    final SweetAlertDialog dialog = new SweetAlertDialog(SendFeedbackActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("发送成功！")
                            .setContentText("感谢您的宝贵意见！")
                            .setConfirmText("好的")
                            .setCancelText("再写一条")
                            .showCancelButton(true);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            dialog.dismissWithAnimation();
                            finish();
                        }
                    });
                    dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            dialog.dismissWithAnimation();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    editText.setText("");
                                }
                            });
                        }
                    });
                    dialog.show();
                } else {
                    ToastUtil.showToast(SendFeedbackActivity.this, "请输入反馈内容！", Toast.LENGTH_SHORT);
                }
            }
        });

        Button addGroupButton = findViewById(R.id.send_feedback_add_group_button);
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!joinQQGroup(ImportantData.qqGroupKey)) {
                    ToastUtil.showToast(SendFeedbackActivity.this, "启动失败……请检查QQ是否安装或未启动", Toast.LENGTH_LONG);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isFirstExit) {
                    ToastUtil.showToast(SendFeedbackActivity.this, "再按一次退出", Toast.LENGTH_SHORT);
                    isFirstExit = false;
                } else {
                    isFirstExit = true;
                    finish();
                }
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (isFirstExit) {
            ToastUtil.showToast(SendFeedbackActivity.this, "再按一次退出", Toast.LENGTH_SHORT);
            isFirstExit = false;
        } else {
            isFirstExit = true;
            finish();
        }
    }

    private void javaMailSendSimpleEmail(String text) throws Exception {
        // 收件人电子邮箱
        String to = toAddress;

        // 发件人电子邮箱
        String from = fromAddress;

        // 获取系统属性
        Properties properties = new Properties();

        // 设置邮件服务器
        properties.setProperty("mail.transport.protocol", "SMTP");
        properties.setProperty("mail.smtp.host", "smtp.163.com");
        properties.setProperty("mail.smtp.port", "25");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.timeout", "1000");

        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        // 登陆邮件发送服务器的用户名和密码
                        return new PasswordAuthentication(fromAddress, authorizationCode);
                    }
                });

        // 创建默认的 MimeMessage 对象
        MimeMessage message = new MimeMessage(session);

        // Set From: 头部头字段
        message.setFrom(new InternetAddress(from));

        // Set To: 头部头字段
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        // Set Subject: 头部头字段
        message.setSubject("用户反馈意见");

        // 设置消息体
        message.setText(text);

        // 发送消息
        Transport.send(message);
    }

    /****************
     *
     * 发起手Q客户端申请加群 DeadlineSaver(878037112)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    private boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

}