package com.ucpeo.meal;

import static com.ucpeo.meal.utils.ShowPrivacy.showFailedDialog;
import static com.ucpeo.meal.utils.ShowPrivacy.showNeedCodeDialog;
import static com.ucpeo.meal.utils.ShowPrivacy.showPrivacyDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.ucpeo.meal.okhttp.PostData;
import com.ucpeo.meal.utils.CqwuUtil;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;


import okhttp3.Cookie;
import okhttp3.OkHttpClient;


public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity 登录界面";
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
    WebView webView;
    static final int login_icon_id = R.id.login_button;
    static final int privacy_view_id = R.id.privacy;

    public void needCode(Message msg) {
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
        TAppllication tAppllication = (TAppllication) this.getApplicationContext();
        tAppllication.fullScreen(this, true);
        if (tAppllication.NeedShowPrivacyDialog()) {
            showPrivacyDialog(this, tAppllication);
        }
        okHttpClient = tAppllication.okHttpClient;
        username = tAppllication.get("username");
        password = tAppllication.get("password");

        setContentView(R.layout.activity_login);

        init();
        setEventListeners();
        initWebview();

        if (!Objects.equals(username, "")) {
            usernameEdit.setText(username);
            cqwuUtil.needCode(username);
        }
        if (!Objects.equals(password, "")) {
            passwordEdit.setText(password);
        }
    }

    @Override
    public void onClick(View v) {
        if (((TAppllication) this.getApplicationContext()).NeedShowPrivacyDialog()) {
            showPrivacyDialog(this, (TAppllication) this.getApplicationContext());
            return;
        }
        switch (v.getId()) {
            case login_icon_id:
                login();
                break;
            case privacy_view_id:
                showPrivacyDialog(this, (TAppllication) this.getApplicationContext());
                break;
        }
    }

    private void login() {
        username = usernameEdit.getText().toString();
        password = passwordEdit.getText().toString();
        Log.d(TAG, "username:" + username + "password:" + password);
        if (username == null) return;
        if (password == null) return;

        String script = "encryptAES(\"" + password + "\", \"" + cqwuUtil.getPwdDefaultEncryptSalt() + "\");";
        webView.evaluateJavascript(script, s -> {
            login_Form.append("password", s.replace("\"", ""));
            try {
                if (login_Form.toJson().get("username") != null) {
                    cqwuUtil.login(login_Form);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        String code = codeEdit.getText().toString();
        if (code_group.getVisibility() == View.VISIBLE) {
            if (code.length() == 4) {
                login_Form.append("captchaResponse", code);
            } else {
                showNeedCodeDialog(this);
                return;
            }
        } else {
            login_Form.append("username", username);
        }
        login_Form.append("username", username);
    }

    public void init() {
        usernameEdit = findViewById(R.id.username_login);
        passwordEdit = findViewById(R.id.password_login);
        codeEdit = findViewById(R.id.code_login);
        code_group = findViewById(R.id.code_group);
        codeImageView = findViewById(R.id.code_view);

        handler = new Handler(getApplicationContext().getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CqwuUtil.CODE_GET_LOGIN_INPUT:
                        if (msg.arg1 == CqwuUtil.CODE_FAIL) {
                            loginResult(msg);
                            return;
                        } else login_Form = (PostData) msg.obj;
                        break;
                    case CqwuUtil.CODE_NEED_CODE:
                        needCode(msg);
                        break;
                    case CqwuUtil.CODE_LOGIN:
                        loginResult(msg);
                        break;
                }
            }
        };

        cqwuUtil = new CqwuUtil(okHttpClient, handler);
        cqwuUtil.getLoginPage();
    }

    public void setEventListeners() {
        findViewById(login_icon_id).setOnClickListener(this);
        findViewById(privacy_view_id).setOnClickListener(this);
        usernameEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String username = usernameEdit.getText().toString();
                if (username.length() != 0) {
                    cqwuUtil.needCode(username);
                }
            }
        });
    }

    public void loginResult(Message msg) {
        if (msg.arg1 == CqwuUtil.CODE_SUCCESS) {
            Log.v(TAG, "登录成功");
            List<Cookie> cookies = new SharedPrefsCookiePersistor(this).loadAll();
            for (Cookie cookie : cookies) {
                Log.d(TAG, cookie.domain() + ": " + cookie);
            }
            username = usernameEdit.getText().toString();
            password = passwordEdit.getText().toString();
            Log.v(TAG, username);
            if (username == null) return;

            TAppllication appllication = (TAppllication) getApplication();
            appllication.save("username", username);
            appllication.save("password", password);
            setResult(CqwuUtil.CODE_SUCCESS);
            finish();
        } else {
            showFailedDialog(LoginActivity.this);
            Log.v(TAG, "登录失败");
            cqwuUtil.getLoginPage();
            login_Form.clear();
            String username = usernameEdit.getText().toString();
            if (username.length() != 0) {
                cqwuUtil.needCode(username);
            }
            if (!passwordEdit.getText().toString().equals("")) {
                passwordEdit.setText("");
            }
        }
    }

    public void initWebview() {
        webView = findViewById(R.id.web_view);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/encrypt.html");
    }
}
