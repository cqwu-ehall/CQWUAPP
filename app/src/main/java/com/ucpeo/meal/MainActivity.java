package com.ucpeo.meal;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import com.race604.flyrefresh.FlyRefreshLayout;
import com.ucpeo.meal.utils.AutoLogin;
import com.ucpeo.meal.utils.CqwuUtil;
import com.ucpeo.meal.utils.QRcode;
import com.ucpeo.meal.widget.Widget;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements QRcode.QRlistener {
    private static final String TAG = "MainActivity";
    final int GET_SERVER = 300;
    final private int REQUEST_CODE = 5;
    QRcode qRcode;
    int times = 0;
    long lastTime = 0;
    ListView listView;
    TextView balanceView;
    TextView baltimeView;
    Handler handler;
    FlyRefreshLayout flyRefreshLayout;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
    };
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        qRcode = new QRcode(this);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }

        TAppllication appllication = (TAppllication) getApplication();

        if (appllication.checkVersion()) {
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


        qRcode.setListener(this);
        listView = findViewById(R.id.list);
        balanceView = findViewById(R.id.balance);
        baltimeView = findViewById(R.id.time);

        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.textview, new String[]{"余额充值", "卡中心", "重新登录", "刷新小部件", "测试"}));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String type = (String) listView.getAdapter().getItem(position);
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.setData(Uri.parse("http://218.194.176.214:8382/epay/thirdapp/index"));
            Log.d(TAG, "onCreate: " + type);
            switch (type) {
                case "余额充值":
                    startActivity(intent);
                    break;
                case "卡中心":
                    startActivity(intent);
                    break;
                case "重新登录":
                    startActivity(new Intent(this, LoginActivity.class));
                    break;
                case "刷新小部件":
                    Widget.create(this);
                    break;

                default:
                    new AutoLogin(this, new AutoLogin.LoginBack() {
                        @Override
                        public void success() {
                            Log.d(TAG, "success: ");
                        }

                        @Override
                        public void fail() {
                            Log.d(TAG, "fail: ");
                        }
                    }).autoLogin();
                    break;
            }
        });

        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case GET_SERVER:
                        break;
                }
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
        if (requestCode == CqwuUtil.CODE_LOGIN) {
            if (resultCode == CqwuUtil.CODE_SUCCESS) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    if (result.indexOf("http") >= 0) {
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
        Log.d(TAG, "needLoginError: ");
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
        Log.d(TAG, "successBalance: " + balance);
        baltimeView.post(() -> {
            balanceView.setText(balance);
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("HH:mm:ss");
            baltimeView.setText(format.format(date));

        });
    }
}
