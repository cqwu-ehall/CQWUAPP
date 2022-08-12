package com.ucpeo.meal.utils;

import android.content.Context;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.ucpeo.meal.okhttp.PostData;

import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetUtil {
    private final OkHttpClient okHttpClient;

    public NetUtil(Context context) {
        okHttpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();
                    requestAddHeader(builder);
                    return chain.proceed(builder.build());
                })
                .cookieJar(new CookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)))
                .build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static void requestAddHeader(Request.Builder requestbuilder) {
        requestbuilder.header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-US) AppleWebKit/530.9 (KHTML, like Gecko) Chrome/999.999.999 Safari/530.9");
    }

    /**
     * PostData post数据数组
     * 添加进request post 数据中
     */
    public static void httpPostData(Request.Builder builder, PostData datas) {
        FormBody.Builder formbodybuilder = new FormBody.Builder();
        for (PostData.Data data1 : datas.getDatas()) {
            formbodybuilder.add(data1.name, data1.value);
        }
        builder.post(formbodybuilder.build());
    }
}
