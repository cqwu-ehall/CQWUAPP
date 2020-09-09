package com.ucpeo.meal;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ucpeo.meal.utils.CqwuUtil;
import com.ucpeo.meal.widget.Widget;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    final int GET_SERVER = 300;
    final private int REQUEST_CODE = 5;
    Handler handler;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
    };
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }

        TAppllication appllication = (TAppllication) getApplication();

        if (appllication.checkVersion()) {
            startActivity(new Intent(this, Welcome.class));
        }

        findViewById(R.id.tool_login).setOnClickListener(this);
        findViewById(R.id.tool_recharge).setOnClickListener(this);
        findViewById(R.id.juanzhu).setOnClickListener(this);
        findViewById(R.id.lainxi).setOnClickListener(this);
        findViewById(R.id.refresh).setOnClickListener(this);

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tool_login:
                startActivityForResult(new Intent(this, LoginActivity.class), CqwuUtil.CODE_LOGIN);
                break;
            case R.id.tool_recharge:
                startActivity(new Intent(this, RechargeActivity.class));
                break;
            case R.id.juanzhu:
                startActivity(new Intent(this, JuanzhuActivity.class));
                break;
            case R.id.lainxi:
                lianxi();
                break;
            case R.id.refresh:
                refresh();
                break;
        }
    }

    private void refresh() {
        Widget.create(this);

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


    public static boolean isQQClientAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mobileqq")) {
                    return true;
                }
            }
        }
        return false;
    }


    public void lianxi() {
        if (isQQClientAvailable(MainActivity.this)) {
            final String qqUrl = "mqqwpa://im/chat?chat_type=wpa&uin=2013142594&version=1";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(qqUrl)));
        } else {
            Toast.makeText(MainActivity.this, "请安装QQ客户端", Toast.LENGTH_SHORT).show();
        }

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

}
