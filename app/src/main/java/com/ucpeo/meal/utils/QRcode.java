package com.ucpeo.meal.utils;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QRcode {
    OkHttpClient okHttpClient;
    Context context;


    public void writeCookies() {
        CookieJar cookieJar = (CookieJar) okHttpClient.cookieJar();
        List<Cookie> list = new ArrayList<>();
        for (Cookie cookie : cookieJar.getCache()) {
            list.add(cookie);
        }
        cookieJar.getPersistor().saveAll(list);

    }

    public void getCodeOnHttp() {
        Request.Builder builder = new Request.Builder();
        String url = "http://218.194.176.214:8382/epay/thirdconsume/qrcode";

        builder.url(url);

        okHttpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.v(this.getClass().getName(), "获取二维码错误");
                listener.netWorkError("网络错误");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.v("qr", response.request().url().host());
                String http_doc = response.body().string();

                String executionREG = "\"SWH5_(.*?)\"";
                Pattern executionpattern = Pattern.compile(executionREG);
                Matcher executionmatcher = executionpattern.matcher(http_doc);
                String execution;
                if (executionmatcher.find()) {
                    execution = Objects.requireNonNull(executionmatcher.group(0)).replace("\"", "");
                    listener.success(execution);
                } else {
                    if (response.request().url().toString().contains("http://authserver.cqwu.edu.cn/authserver/login")) {
                        listener.needLoginError();
                        return;
                    }
                    listener.netWorkError("请求出错");
                    Log.v("正则匹配", "未匹配" + response.request().url() + "\n" + http_doc);
                }
            }
        });
    }

    public QRcode(Context context, OkHttpClient client) {
        this.context = context;
        if (client != null) {
            okHttpClient = client;
        } else {
            okHttpClient = new NetUtil(context).getOkHttpClient();
        }
    }

    public interface QRlistener {
        default void needLoginError() {
        }

        default void netWorkError(String des) {
        }

        default void success(String code) {
        }

        default void successBalance(String balance) {
        }
    }

    QRlistener listener = new QRlistener() {

        @Override
        public void needLoginError() {
            Log.v(getClass().getName(), "获取二维码,需要登录");
        }

        @Override
        public void netWorkError(String des) {
            Log.v(getClass().getName(), "获取二维码,网络错误");
        }

        @Override
        public void success(String code) {
            Log.v(getClass().getName(), "获取二维码 : " + code);
        }

        @Override
        public void successBalance(String balance) {
            Log.v(getClass().getName(), "获取到余额数据:" + balance);
        }
    };


    public void setListener(QRlistener listener) {
        this.listener = listener;
    }

    public void getBalance() {
        String url = "http://218.194.176.214:8382/epay/thirdapp/balance";

        okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                listener.needLoginError();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String http_doc = response.body().string();
                String executionREG = "weui-cell__ft\">(.*?)￥";
                Pattern executionpattern = Pattern.compile(executionREG);
                Matcher executionmatcher = executionpattern.matcher(http_doc);
                String execution = "null";
                if (executionmatcher.find()) {
                    execution = executionmatcher.group(0);
                    assert execution != null;
                    execution = execution.replace("weui-cell__ft\">", "").replace("￥", "");
                }
                if (execution.equals("null")) {
                    listener.needLoginError();
                    return;
                }
                listener.successBalance(execution);
            }
        });
    }
}
