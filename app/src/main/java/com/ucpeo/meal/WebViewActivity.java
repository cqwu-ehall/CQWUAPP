package com.ucpeo.meal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.ucpeo.meal.utils.CqwuUtil;
import com.ucpeo.meal.utils.NetUtil;
import com.ucpeo.meal.utils.WebviewCookieSync;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class WebViewActivity extends Activity implements View.OnClickListener {
    ImageButton[] buttons = null;
    WebView webView;
    String script = "   var re = false;\n" +
            "    let node = document.getElementsByClassName('index-nav-id');\n" +
            "    if(node.length==0){}\n" +
            "    else if(node[0].innerHTML!=\"\"){\n" +
            "        re= true;\n" +
            "    }else{\n" +
            "        re= false;\n" +
            "    }\n" +
            "    re";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAppllication application = (TAppllication) getApplication();
        application.fullScreen(this);
        setContentView(R.layout.activity_web_view);

        LinearLayout btn_group = findViewById(R.id.btn_group);
        buttons = new ImageButton[btn_group.getChildCount() - 1];
        int j = 0;
        for (int i = 0; i < btn_group.getChildCount(); i++) {
            View view = btn_group.getChildAt(i);
            if (view instanceof ImageButton) {
                buttons[j++] = (ImageButton) view;
                view.setOnClickListener(this);
            }
        }

        webView = findViewById(R.id.web_view);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().getScheme().contains("http")) {
                    return super.shouldOverrideUrlLoading(view, request);
                } else {
                    Toast.makeText(WebViewActivity.this, request.getUrl().toString(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                    startActivity(intent);
                    webView.goBack();
                    return false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("TAG", "onPageFinished: " + url);
                super.onPageFinished(view, url);
                if (url.contains("authserver.cqwu.edu.cn/authserver/index.do")) {
                    webView.evaluateJavascript(script, result -> {
                        if ("true".equals(result)) {
                            loginSuccess();
                        }
                    });
                }
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        CqwuUtil.syncCookie2WebClient(webView, this);
    }

    private void loginSuccess() {
        Log.d("FILE", "loginSuccess: " + this.getFilesDir().toString());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.flush();
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        CookiePersistor cookiePersistor = new SharedPrefsCookiePersistor(this);
        cookiePersistor.removeAll(cookiePersistor.loadAll());
        cookiePersistor.saveAll(new WebviewCookieSync(this).getCooikes());
        Intent intent = new Intent(this, MainActivity.class);

        CqwuUtil.request(new NetUtil(this).getOkHttpClient(),
                new Request.Builder().url("http://authserver.cqwu.edu.cn/authserver/index.do")
                        .build(), new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d("WEb", "失败");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Log.d("WEb", "onResponse: " + response.body().string());
                    }
                }, 1);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        CqwuUtil.syncCookie2WebClient(webView, this);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        webView.loadUrl(uri.toString());
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        int t = 0;
        for (ImageButton button : buttons) {
            if (view == button)
                break;
            t++;
        }
        switch (t) {
            case 3:
                finish();
                break;
            case 2:
                webView.reload();
                break;
            case 1:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                break;
            case 0:
                if (webView.canGoBack()) {
                    webView.goBack();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
