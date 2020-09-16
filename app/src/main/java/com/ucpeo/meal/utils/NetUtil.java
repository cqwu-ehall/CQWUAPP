package com.ucpeo.meal.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.ucpeo.meal.okhttp.PostData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetUtil {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient okHttpClient;

    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     *
     * @return
     */
    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
        }
        return ssfFactory;
    }

    /**
     * 用于信任所有证书
     */
    class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public NetUtil(Context context) {

        okHttpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory())
                .hostnameVerifier(new HostnameVerifier() {
                    @SuppressLint("BadHostnameVerifier")
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(new Interceptor() {
                    @NotNull
                    @Override
                    public Response intercept(@NotNull Chain chain) throws IOException {
                        Request.Builder builder = chain.request().newBuilder();
                        requestAddHeader(builder);
                        return chain.proceed(builder.build());

                    }
                })
                .cookieJar(new CookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)))
                .build();


    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static Request.Builder requestAddHeader(Request.Builder requestbuilder) {
        requestbuilder.header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_6; en-US) AppleWebKit/530.9 (KHTML, like Gecko) Chrome/999.999.999 Safari/530.9");
        return requestbuilder;
    }

    public static Request.Builder requestAddHeader(Request.Builder requestbuilder, String s) {
        //"Referer:https://cqwu.wanxue.cn/student_center.do?appid=1&username=13527408549&randomCode=98f7cfe7-aacb-4e1f-ac2c-a6745e67b1e8&displayname=13527408549&type=0&clickStats=zjxx&reffer=https://cqwu.wanxue.cn"

        requestbuilder.header(s.split(":")[0], s.split(":")[1]);
        return requestbuilder;
    }


    public static Request.Builder httpPostToGet(Request.Builder builder, String url, PostData datas) {
        builder.url(url + datas.toGet());
        //builder.post(null);
        return builder;
    }
    /**
     * PostData post数据数组
     * 添加进request post 数据中
     *
     * */
    public static Request.Builder httpPostData(Request.Builder builder, PostData datas) {
        FormBody.Builder formbodybuilder = new FormBody.Builder();
        for (PostData.Data data1 : datas.getDatas()) {
            formbodybuilder.add(data1.name, data1.value);
        }
        builder.post(formbodybuilder.build());
        return builder;

    }

    public static Request.Builder httpPostData(Request.Builder builder, PostData datas, String type) {
        /*
         * PostData post数据数组
         * 添加进request post 数据中
         *
         * */
        FormBody.Builder formbodybuilder = new FormBody.Builder();
        for (PostData.Data data1 : datas.getDatas()) {
            formbodybuilder.add(data1.name, data1.value);
        }
        builder.post(formbodybuilder.build());
        return builder;

    }

    public static Request.Builder httpSetUrl(Request.Builder builder, String url) {
        builder.url(url);
        return builder;

    }

    public static Request.Builder httpJson(Request.Builder builder, JSONObject json) {
        RequestBody body = RequestBody.create(JSON, json.toString());
        builder.post(body);
        return builder;
    }

    public static Request.Builder httpJson(Request.Builder builder, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        builder.post(body);
        return builder;

    }

    /**
     * 将InputStream写入本地文件
     *
     * @param destination 写入本地目录
     * @param input       输入流
     * @throws IOException IOException
     */
    public static void writeToLocal(String destination, InputStream input)
            throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        input.close();
        downloadFile.close();

    }


}
