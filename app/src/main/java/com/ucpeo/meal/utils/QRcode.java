package com.ucpeo.meal.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ucpeo.meal.TAppllication;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QRcode {
    OkHttpClient okHttpClient;
    Context context ;


    public void writeCookies() {
        CookieJar cookieJar = (CookieJar) okHttpClient.cookieJar();
        List<Cookie> list = new ArrayList<>();
        Iterator<Cookie> cookies = cookieJar.getCache().iterator();
        while (cookies.hasNext()) {
            list.add(cookies.next());
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
                String execution = "";
                if (executionmatcher.find()) {
                    execution = executionmatcher.group(0).replace("\"", "");
                    listener.success(execution);
                    return;
                } else {
                    if (response.request().url().toString().contains("http://authserver.cqwu.edu.cn/authserver/login")) {
                        listener.needLoginError();
                        Log.v("需要登录", "需要登录");
                        return;
                    }

                    listener.netWorkError("请求出错");
                    Log.v("正则匹配", "未匹配" + response.request().url().toString() + "\n" + http_doc);
                }
            }

        });
    }


    // 创建二维码
    public static Bitmap createQRcodeBitmap(String url) {
        int w = 400;
        int h = 400;
        try {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, w, h, hints);
            int[] pixels = new int[w * h];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * w + x] = 0xff000000;
                    } else {
                        pixels[y * w + x] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }

    public QRcode(Context context) {
        this.context = context;
        okHttpClient = new NetUtil(context).getOkHttpClient();

    }


    public interface QRlistener {
       default void needLoginError(){};

        default  void netWorkError(String des){};

        default  void success(String code){};

        default  void successBalance(String balance){};
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
                listener.successBalance("null");
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
                    execution = execution.replace("weui-cell__ft\">", "").replace("￥", "");
                }
                listener.successBalance(execution);
            }

        });

    }





}
