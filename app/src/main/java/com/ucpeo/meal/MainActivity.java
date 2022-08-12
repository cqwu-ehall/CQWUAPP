package com.ucpeo.meal;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.race604.flyrefresh.FlyRefreshLayout;
import com.ucpeo.meal.utils.CqwuUtil;
import com.ucpeo.meal.utils.QRcode;
import com.ucpeo.meal.widget.Widget;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.OkHttpClient;


public class MainActivity extends Activity implements QRcode.QRlistener {
    private static final String TAG = "MainActivity";
    final int GET_SERVER = 300;
    QRcode qRcode;
    ListView listView;
    TextView balanceView;
    TextView baltimeView;
    Handler handler;
    FlyRefreshLayout flyRefreshLayout;
    OkHttpClient okHttpClient;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
    };
    //请求状态码
    private static final int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAppllication application = (TAppllication) getApplication();
        application.fullScreen(this);
        okHttpClient = application.okHttpClient;
        qRcode = new QRcode(this, okHttpClient);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }

        if (application.checkVersion()) {
            startActivity(new Intent(this, Welcome.class));
        }

        flyRefreshLayout = findViewById(R.id.fly_layout);
        flyRefreshLayout.setActionDrawable(getResources().getDrawable(R.drawable.icon));
        flyRefreshLayout.setOnPullRefreshListener(new FlyRefreshLayout.OnPullRefreshListener() {
            @Override
            public void onRefresh(FlyRefreshLayout view) {
                qRcode.getBalance();
            }

            @Override
            public void onRefreshAnimationEnd(FlyRefreshLayout view) {
                Toast.makeText(getParent(), "刷新完成", Toast.LENGTH_LONG).show();
            }
        });
        qRcode.getBalance();

        qRcode.setListener(this);
        listView = findViewById(R.id.list);
        balanceView = findViewById(R.id.balance);
        baltimeView = findViewById(R.id.time);

        listView.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.textview, new String[]{"余额充值", "支付码", "账单", "卡中心", "重新登录", "刷新小部件"}));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String type = (String) listView.getAdapter().getItem(position);
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            Log.d(TAG, "onCreate: " + type);
            switch (type) {
                case "余额充值":
                    intent.setData(Uri.parse("http://pay.cqwu.edu.cn/signAuthentication?url=openSchoolCardRecharge-payProjectId=2-id=3"));
                    startActivity(intent);
                    break;
                case "支付码":
                    intent.setData(Uri.parse("http://218.194.176.214:8382/epay/thirdconsume/qrcode"));
                    startActivity(intent);
                    break;
                case "账单":
                    intent.setData(Uri.parse("http://218.194.176.214:8382/epay/thirdapp/bill"));
                    startActivity(intent);
                    break;
                case "卡中心":
                    intent.setData(Uri.parse("http://218.194.176.214:8382/epay/thirdapp/index"));
                    startActivity(intent);
                    break;
                case "重新登录":
                    startActivity(new Intent(this, LoginActivity.class));
                    break;
                case "刷新小部件":
                    Widget.create(this);
                    break;
            }
        });

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

    @Override
    public void needLoginError() {
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
            Toast.makeText(getApplicationContext(), "登录失效，请尝试重新登录", Toast.LENGTH_LONG).show();
            balance = "0.00";
        }
        Log.d(TAG, "success get Balance: " + balance);
        String finalBalance = balance;
        baltimeView.post(() -> {
            balanceView.setText(finalBalance);
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
            baltimeView.setText(format.format(date));
        });
    }
}
