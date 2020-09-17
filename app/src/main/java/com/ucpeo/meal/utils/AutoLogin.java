package com.ucpeo.meal.utils;


import android.content.Context;
import android.util.Log;

import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.ucpeo.meal.TAppllication;
import com.ucpeo.meal.okhttp.PostData;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

public class AutoLogin {
    Context context ;
    OkHttpClient okHttpClient ;
    LoginBack loginBack ;
    public AutoLogin(Context context,LoginBack loginBack) {
        this.loginBack =loginBack;
        this.context = context ;

    }


    public void autoLogin(){

        SharedPrefsCookiePersistor sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(context);
        sharedPrefsCookiePersistor.clear();
        this.okHttpClient =new NetUtil(context).getOkHttpClient();
        getLoginPage();
    }

    private void login(PostData initPostData,final PostData postData) {

        PostData  post =new PostData();
        Map<String,String> map = new HashMap<>();

        for (PostData.Data data : initPostData.getDatas()) {
            map.put(data.name,data.value);
        }
        for (PostData.Data data : postData.getDatas()) {
            map.put(data.name,data.value);
        }
        for (String s : map.keySet()) {
            post.append(s,map.get(s));
        }

        String url = "http://authserver.cqwu.edu.cn/authserver/login";
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
               loginBack.fail();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String url = response.request().url().toString();
                String html = response.body().string();
                response.close();
                if (html.contains("logout")){
                        CookieJar cookieJar = (CookieJar) okHttpClient.cookieJar();
                        List<Cookie> list = new ArrayList<>();
                        Iterator<Cookie> cookies = cookieJar.getCache().iterator();
                        while (cookies.hasNext()) {
                            list.add(cookies.next());
                        }
                        cookieJar.getPersistor().saveAll(list);
                   loginBack.success();
                }else {
                 loginBack.fail();
                }

            }
        };

        Request.Builder builder = new Request.Builder().url(url);
        NetUtil.httpPostData(builder, post);
        CqwuUtil.request(okHttpClient,builder.build(),callback,1);

    }



    private void getLoginPage() {
        String url ="http://authserver.cqwu.edu.cn/authserver/login";
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                loginBack.fail();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String html = response.body().string();
                response.close();
                PostData initPostData = buildLoginPost(html);
                TAppllication tAppllication = (TAppllication) context.getApplicationContext();
                String username = tAppllication.get("username");
                String password = tAppllication.get("password");
                PostData postData =new PostData();
                postData.append("username",username);
                postData.append("password",password);
                login(initPostData,postData);
            }
        };

        Request.Builder builder = new Request.Builder().url(url);
        CqwuUtil.request(okHttpClient,builder.build(),callback,1);
    }


    private PostData buildLoginPost(String html) {
        String reg = "<input.*? name=\"(.*?)\".*?value=\"(.*?)\".*?>";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher =pattern.matcher(html);
        PostData postData = new PostData();
        while (matcher.find()){
            MatchResult matchResult = matcher.toMatchResult();
            postData.append(matchResult.group(1),matchResult.group(2));
        }
        return  postData;
    }



    public interface LoginBack{
        void success();
        void fail();
    }


}
