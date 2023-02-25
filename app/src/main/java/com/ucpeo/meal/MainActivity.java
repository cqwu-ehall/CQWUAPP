package com.ucpeo.meal;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ucpeo.meal.utils.CqwuUtil;
import com.ucpeo.meal.utils.QRcode;
import com.ucpeo.meal.widget.Widget;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends Activity implements QRcode.QRlistener {
    private static final String TAG = "MainActivity";
    final int GET_SERVER = 300;
    QRcode qRcode;
    TextView balanceView;
    TextView baltimeView;
    Handler handler;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.INTERNET
    };
    //请求状态码
    private static final int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAppllication application = (TAppllication) getApplication();
        application.fullScreen(this, false);
        qRcode = new QRcode(this, application.okHttpClient);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this).setTitle("需要网络")//设置对话框标题
                    .setMessage("请授予我联网权限，否则我无法正常运行！！！")//设置显示的内容
                    .setPositiveButton("确定", (dialog, which) -> {//确定按钮的响应事件
                    }).show();//显示此对话框
            return;
        }

        if (application.checkVersion()) {
            startActivity(new Intent(this, Welcome.class));
        }

        qRcode.getBalance();
        qRcode.setListener(this);

        initClickButton();

        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        Message msg = Message.obtain();
        msg.what = GET_SERVER;
        msg.obj = true;
        handler.sendMessage(msg);
    }

    protected void initClickButton() {
        List<TextView> listView = new ArrayList<>();

        balanceView = findViewById(R.id.balance);
        baltimeView = findViewById(R.id.time);

        listView.add(findViewById(R.id.quick_pay_button));
        listView.add(findViewById(R.id.quick_charge_button));
        listView.add(findViewById(R.id.quick_login_button));
        listView.add(findViewById(R.id.quick_bill_button));
        listView.add(findViewById(R.id.quick_center_button));
        listView.add(findViewById(R.id.quick_refresh));
        listView.add(findViewById(R.id.qq_share));

        @SuppressLint("NonConstantResourceId") View.OnClickListener listener = v -> {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            Intent intent_ = new Intent();
            switch (v.getId()) {
                case R.id.title_github_icon:
                    intent_.setAction(Intent.ACTION_VIEW);
                    intent_.setData(Uri.parse("https://github.com/cqwu-ehall/CQWUAPP"));
                    startActivity(intent_);
                    break;
                case R.id.quick_pay_button:
                    intent.setData(Uri.parse("http://218.194.176.214:8382/epay/thirdconsume/qrcode"));
                    startActivity(intent);
                    break;
                case R.id.quick_charge_button:
                    intent.setData(Uri.parse("https://pay.cqwu.edu.cn/casLogin"));
                    startActivity(intent);
                    break;
                case R.id.quick_login_button:
                    startActivity(new Intent(this, LoginActivity.class));
                    break;
                case R.id.quick_bill_button:
                    intent.setData(Uri.parse("http://218.194.176.214:8382/epay/thirdapp/bill"));
                    startActivity(intent);
                    break;
                case R.id.quick_center_button:
                    intent.setData(Uri.parse("http://218.194.176.214:8382/epay/thirdapp/index"));
                    startActivity(intent);
                    break;
                case R.id.quick_refresh:
                    Widget.create(this);
                    break;
                case R.id.refresh_click:
                    qRcode.getBalance();
                    break;
                case R.id.qq_share:
                    intent_.setAction(Intent.ACTION_SEND);
                    intent_.putExtra(Intent.EXTRA_TEXT, "「文理校园」，亮出消费码快人一步\uD83D\uDC49：https://app.cqwu.wiki （复制地址到浏览器打开）");
                    intent_.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(intent_, null);
                    startActivity(shareIntent);
                    break;
            }
        };

        for (int i = 0; i < listView.size(); i++) {
            listView.get(i).setClickable(true);
            listView.get(i).setOnClickListener(listener);
        }
        LinearLayout main_big_click = findViewById(R.id.refresh_click);
        main_big_click.setClickable(true);
        main_big_click.setOnClickListener(listener);

        ImageFilterView github_click = findViewById(R.id.title_github_icon);
        github_click.setClickable(true);
        github_click.setOnClickListener(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int REQUEST_CODE = 5;
        if (requestCode == CqwuUtil.CODE_LOGIN) {
            if (resultCode == CqwuUtil.CODE_SUCCESS) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    if (result.contains("http")) {
                        Log.v(TAG, "二维码结果" + result);
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getApplicationContext(), "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        qRcode.getBalance();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.v("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

    protected void changeLoginStatus(Boolean status) {
        runOnUiThread(() -> {
            if (status) {
                String username = ((TAppllication) this.getApplicationContext()).get("username");
                Calendar c = Calendar.getInstance();
                int s = c.get(Calendar.HOUR_OF_DAY);
                if (s <= 4 || s >= 19) {
                    username += "，晚上好";
                } else if (s >= 12) {
                    username += "，下午好";
                } else {
                    username += "，早上好";
                }
                ((TextView) findViewById(R.id.main_text_status)).setText(username);
                findViewById(R.id.main_lin_status).setBackgroundResource(R.drawable.bg_green_round);
                ImageFilterView img_status = findViewById(R.id.main_img_status);
                img_status.setImageResource(R.mipmap.ic_success);
            } else {
                findViewById(R.id.main_lin_status).setBackgroundResource(R.drawable.bg_dark_round);
                ImageFilterView img_status = findViewById(R.id.main_img_status);
                img_status.setImageResource(R.mipmap.ic_warn);
            }
        });
    }

    @Override
    public void needLoginError() {
        changeLoginStatus(false);
        Looper.prepare();
        Toast.makeText(getApplicationContext(), "登录失效，请尝试重新登录", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void netWorkError(String des) {
        Log.d(TAG, "netWorkError: ");
    }

    @Override
    public void success(String code) {
        Log.d(TAG, "success: ");
    }

    @Override
    public void successBalance(String balance) {
        if (Objects.equals(balance, "null")) {
            changeLoginStatus(false);
            Toast.makeText(getApplicationContext(), "登录失效，请尝试重新登录", Toast.LENGTH_LONG).show();
            balance = "0.00";
        }
        Log.d(TAG, "success get Balance: " + balance);
        String finalBalance = balance;
        baltimeView.post(() -> {
            balanceView.setText(finalBalance);
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            baltimeView.setText(format.format(date));
            changeLoginStatus(true);
        });
    }
}
