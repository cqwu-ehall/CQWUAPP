package com.ucpeo.meal.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.ucpeo.meal.okhttp.PostData;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CqwuUtil {
    public final static int CODE_FAIL = -1;
    public final static int CODE_SUCCESS = 1;
    public final static int CODE_GET_LOGIN_INPUT = 100;
    public final static int CODE_NEED_CODE = 101;
    public final static int CODE_LOGIN = 200;

    Handler handler;
    PostData initPostData = new PostData();
    String pwdDefaultEncryptSalt;

    private static final String TAG = "CqwuUtil  网络请求";
    OkHttpClient okHttpClient;

    public CqwuUtil(OkHttpClient okHttpClient, Handler handler) {
        this.handler = handler;
        this.okHttpClient = okHttpClient;
    }

    public void login(final PostData postData) {

        PostData post = new PostData();
        Map<String, String> map = new HashMap<>();

        for (PostData.Data data : initPostData.getDatas()) {
            map.put(data.name, data.value);
        }
        for (PostData.Data data : postData.getDatas()) {
            map.put(data.name, data.value);
        }
        for (String s : map.keySet()) {
            post.append(s, map.get(s));
        }

        String url = "http://authserver.cqwu.edu.cn/authserver/login";
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                faildTask(CODE_LOGIN);
                Log.v(TAG, "login eror 请求失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String html = response.body().string();
                response.close();
                if (html.contains("logout")) {
                    writeCookies();
                    successTask("同步及登录成功", CODE_LOGIN);
                } else {
                    faildTask(CODE_LOGIN);
                }
            }
        };

        Log.v(TAG, "登录表单" + post.toJson().toString());
        Request.Builder builder = new Request.Builder().url(url);
        NetUtil.httpPostData(builder, post);
        request(okHttpClient, builder.build(), callback, 1);
    }

    public void getLoginPage() {
        String url = "http://authserver.cqwu.edu.cn/authserver/login";
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String html = response.body().string();
                response.close();
                initPostData = buildLoginPost(html);
                setPwdDefaultEncryptSalt(html);
                CookieJar cookieJar = (CookieJar) okHttpClient.cookieJar();
                for (Cookie cookie : cookieJar.getCache()) {
                    System.out.println(cookie.toString());
                }
            }
        };
        Request.Builder builder = new Request.Builder().url(url);
        request(okHttpClient, builder.build(), callback, 1);
    }


    public PostData buildLoginPost(String html) {
        String reg = "<input.*? name=\"(.*?)\".*?value=\"(.*?)\".*?>";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(html);
        PostData postData = new PostData();
        while (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            postData.append(matchResult.group(1), matchResult.group(2));
        }
        return postData;
    }

    public void setPwdDefaultEncryptSalt(String html) {
        String reg = "<input type=\"hidden\" id=\"pwdDefaultEncryptSalt\" value=\"(.*?)\".*?>";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            pwdDefaultEncryptSalt = matchResult.group(1);
        }
    }

    public String getPwdDefaultEncryptSalt() {
        return pwdDefaultEncryptSalt;
    }

    public void needCode(String username) {
        String url = "http://authserver.cqwu.edu.cn/authserver/needCaptcha.html?username=" + username;
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                faildTask(CODE_NEED_CODE);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String html = response.body().string();
                Log.v(TAG, "是否需要验证码" + html);
                response.close();
                boolean needCode = html.contains("true");
                if (needCode) {
                    getCodeImage();
                } else {
                    successTask(null, CODE_NEED_CODE);
                }
            }
        };

        okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(callback);
    }

    private void getCodeImage() {
        String url = "http://authserver.cqwu.edu.cn/authserver/captcha.html?ts=" + System.currentTimeMillis() % 1000;
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                faildTask(CODE_NEED_CODE);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                InputStream inputStream = response.body().byteStream();
                Bitmap codeBitmap = BitmapFactory.decodeStream(inputStream);
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                response.close();
                successTask(codeBitmap, CODE_NEED_CODE);
            }
        };
        okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(callback);

    }

    public void faildTask(int taskId) {
        Message msg = Message.obtain(handler);
        msg.what = taskId;
        msg.arg1 = CODE_FAIL;
        handler.sendMessage(msg);
    }

    public void successTask(Object data, int taskId) {
        Message msg = Message.obtain(handler);
        msg.what = taskId;
        msg.arg1 = CODE_SUCCESS;
        msg.obj = data;
        handler.sendMessage(msg);
    }


    /**
     * 保存持久Cookie
     */
    public void writeCookies() {
        CookieJar cookieJar = (CookieJar) okHttpClient.cookieJar();
        List<Cookie> list = new ArrayList<>();
        for (Cookie cookie : cookieJar.getCache()) {
            list.add(cookie);
        }
        cookieJar.getPersistor().saveAll(list);
    }

    /**
     * 同步cookie到webview
     *
     * @param context 上下文对象 ==> SharedPrefsCookiePersistor 持久cookie
     */
    public static void syncCookie2WebClient(WebView webview, Context context) {
        final CookieManager cookieManager = CookieManager.getInstance();
        syncCookie(cookieManager, webview, context);
    }

    private static void syncCookie(CookieManager cookieManager, WebView webview, Context context) {
        cookieManager.setAcceptThirdPartyCookies(webview, true);
        CookieJar cookieJar = new CookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        List<Cookie> cookies = cookieJar.getPersistor().loadAll();
        for (Cookie cookie : cookies) {
            cookieManager.setCookie(cookie.domain(), cookie.toString());
        }
        cookieManager.flush();
    }

    public static void request(final OkHttpClient okHttpClient, final Request request, final Callback callback, Integer times) {
        if (times == null)
            times = 1;
        else if (times > 10) {
            Log.d(TAG, "request: 重定向次数过多");
            callback.onFailure(null, new IOException("重定向次数过多"));
        }
        final Integer finalTimes = times + 1;
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 300 && response.code() < 400) {
                    Log.d(TAG, "onResponse: " + request.url());
                    request(okHttpClient, new Request.Builder().url(request.url()).build(), callback, finalTimes);
                } else {
                    callback.onResponse(call, response);
                }
            }
        });
    }
}
