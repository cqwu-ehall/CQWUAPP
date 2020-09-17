package com.ucpeo.meal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.ucpeo.meal.okhttp.PostData;
import com.ucpeo.meal.utils.CqwuUtil;
import com.ucpeo.meal.utils.NetUtil;


import java.util.List;




import okhttp3.Cookie;
import okhttp3.OkHttpClient;


public class LoginActivity extends Activity implements View.OnClickListener {
    private static String TAG = "LoginActivity 登录界面";
    public static int LOGIN_ERROR = 40996;
    public static int LOGIN_BROWSER = 30002;

    OkHttpClient okHttpClient;
    EditText usernameEdit;
    EditText passwordEdit;
    EditText codeEdit;
    CqwuUtil cqwuUtil;
    PostData login_Form = new PostData();
    LinearLayout code_group;
    ImageView codeImageView;
    Handler handler;
    String username;
    String password;

    public void needCode(Message msg) {
        Log.v(TAG, "主线程=" + isMainThread());
        Log.v(TAG, "获取是否需要验证码");
        if (msg.arg1 == CqwuUtil.CODE_FAIL) {
            Log.v(TAG, "获取验证码状态失败");
            return;
        }
        if (msg.obj != null) {
            Log.v(TAG, "需要验证码");
            code_group.setVisibility(View.VISIBLE);
            codeImageView.setImageBitmap((Bitmap) msg.obj);
            cqwuUtil.getLoginPage();


        } else {
            Log.v(TAG, "不需要验证码");
            code_group.setVisibility(View.INVISIBLE);
            codeEdit.getText().clear();

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        setEventListeners();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                login();
                break;
            case  R.id.usage_browser_login:
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.setData(Uri.parse("http://authserver.cqwu.edu.cn/authserver/login"));
                startActivity(intent);
                finish();
                break;

        }
    }

    private void login() {
        username = usernameEdit.getText().toString();
        password = passwordEdit.getText().toString();
        Log.v(TAG, username);
        if (username == null)
            return;

        TAppllication appllication = (TAppllication) getApplication();
        appllication.save("username", username);
        appllication.save("password", password);
        String code = codeEdit.getText().toString();
        if (code.length() != 0)
            login_Form.append("captchaResponse", code);
        Log.v(TAG, "username:" + username + "password:" + password);
        login_Form.append("username", username);
        login_Form.append("password", password);
        cqwuUtil.login(login_Form);
    }

    public void init() {
        usernameEdit = (EditText) findViewById(R.id.username_login);
        passwordEdit = (EditText) findViewById(R.id.password_login);
        codeEdit = (EditText) findViewById(R.id.code_login);
        code_group = (LinearLayout) findViewById(R.id.code_group);
        codeImageView = (ImageView) findViewById(R.id.code_view);
        okHttpClient = new NetUtil(this).getOkHttpClient();

        handler = new Handler(getApplicationContext().getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CqwuUtil.CODE_GET_LOGIN_INPUT:
                        if (msg.arg1 == CqwuUtil.CODE_FAIL) Log.v(TAG, "登录任务失败");
                        else login_Form = (PostData) msg.obj;
                        break;
                    case CqwuUtil.CODE_NEED_CODE:
                        needCode(msg);
                        break;
                    case CqwuUtil.CODE_LOGIN:
                        loginResult(msg);
                        break;
                    case 40996:
                        new AlertDialog.Builder(LoginActivity.this).setTitle("登录失败")//设置对话框标题
                                .setMessage("应用网关错误或登录信息错误")//设置显示的内容
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                    }
                                }).show();//显示此对话框
                        break;
                }
                Log.v(TAG, "线程状况 主线程：" + isMainThread());
                Log.v(TAG, "线程状况 主线程ID：" + Looper.getMainLooper().getThread().getId() + "  当前线程ID:" + Thread.currentThread().getId());
            }
        };

        cqwuUtil = new CqwuUtil(okHttpClient, handler);
        cqwuUtil.getLoginPage();

    }

    public void setEventListeners() {
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.usage_browser_login).setOnClickListener(this);
        usernameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.v(TAG, "用户名输入框焦点状态改变" + hasFocus);
                if (!hasFocus) {
                    String username = usernameEdit.getText().toString();
                    if (username.length() != 0) {
                        cqwuUtil.needCode(username);
                    }
                }
            }
        });

    }

    public void loginResult(Message msg) {
        if (msg.arg1 == CqwuUtil.CODE_SUCCESS) {
            Log.v(TAG, "登录成功");
            List<Cookie> cookies = new SharedPrefsCookiePersistor(this).loadAll();
            for (Cookie cookie : cookies) {
                Log.d(TAG,  cookie.domain()+": "+cookie.toString());
            }
            setResult(CqwuUtil.CODE_SUCCESS);
            finish();
        } else {
            Message msg1 =Message.obtain();
            msg1.what=40996;
            handler.sendMessage(msg1);
            Log.v(TAG, "登录失败");
            cqwuUtil.getLoginPage();
            String username = usernameEdit.getText().toString();
            if (username.length() != 0) {
                cqwuUtil.needCode(username);
            }
        }
    }

    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }


}
