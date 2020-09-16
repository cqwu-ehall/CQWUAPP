package com.ucpeo.meal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.ucpeo.meal.utils.CookieJar;
import com.ucpeo.meal.utils.CqwuUtil;
import com.ucpeo.meal.utils.NetUtil;
import com.ucpeo.meal.utils.WebviewCookieSync;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebViewActivity extends Activity implements View.OnClickListener {
    ImageButton[] buttons =null;
    WebView webView ;
    boolean login = false;
    String script  = "   var re = false;\n" +
            "    let node = document.getElementsByClassName('index-nav-id');\n" +
            "    if(node.length==0){}\n" +
            "    else if(node[0].innerHTML!=\"\"){\n" +
            "        re= true;\n" +
            "    }else{\n" +
            "        re= false;\n" +
            "    }\n" +
            "    re";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        LinearLayout btn_group = (LinearLayout)findViewById(R.id.btn_group);
        buttons = new ImageButton[btn_group.getChildCount()-1];
        int j = 0;
        for (int i = 0; i <btn_group.getChildCount() ; i++) {
            View view = btn_group.getChildAt(i);
            if (view instanceof ImageButton){
                buttons[j++] = (ImageButton) view;
                view.setOnClickListener(this);
            }
        }
        webView=findViewById(R.id.web_view);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().getScheme().contains("http")){
                    return  super.shouldOverrideUrlLoading(view,request);
                }else
                {
                    Toast.makeText(WebViewActivity.this,request.getUrl().toString(),Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                    startActivity(intent);
                    webView.goBack();
                    return  false;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("TAG", "onPageFinished: "+url);
                super.onPageFinished(view, url);
                if (url.contains("authserver.cqwu.edu.cn/authserver/index.do")){
                    webView.evaluateJavascript(script,result->{
                       if (login&&"true".equals(result)){
                           loginSuccess();
                       }
                    });
                }

            }
        });

        webView.getSettings().setJavaScriptEnabled(true);

    }


    private void loginSuccess() {
        Log.d("FILE", "loginSuccess: "+ this.getFilesDir().toString());
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        }
        cookieManager.setAcceptCookie(true);



        CookiePersistor cookiePersistor= new SharedPrefsCookiePersistor(this);
        cookiePersistor.removeAll(cookiePersistor.loadAll());
        cookiePersistor.saveAll(new WebviewCookieSync(this).getCooikes());
        Intent intent = new Intent(this,MainActivity.class);

        CqwuUtil.request(new NetUtil(this).getOkHttpClient(),
                new Request.Builder().url("http://authserver.cqwu.edu.cn/authserver/index.do")
                .build(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("WEb", "失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("WEb", "onResponse: "+response.body().string());
            }
        },1);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        Intent intent = getIntent();
        if (intent.getData().toString().contains("http://authserver.cqwu.edu.cn/authserver/login")){
            CqwuUtil.clearCookie(cookieManager);
            login=true;
        }else {
            CqwuUtil.clearCookie(cookieManager);
            CqwuUtil.syncCookie2WebClient(this);
        }

        Uri uri = intent.getData();
        webView.loadUrl(uri.toString());
        super.onResume();
    }

    @Override
    public void onClick(View view) {
         int t =0;
        for (ImageButton button : buttons) {
            if (view==button)
                break;
            t++;
        }
        switch (t)
        {
            case 3:finish();
                break;
            case 2:
                webView.reload();
                break;
            case 1:
                break;

        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode==event.KEYCODE_BACK){
        webView.goBack();
        return  false;
    }
    return  super.onKeyDown(keyCode,event);
}
}


