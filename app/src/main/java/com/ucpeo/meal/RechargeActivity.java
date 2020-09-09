package com.ucpeo.meal;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;


import com.kaopiz.kprogresshud.KProgressHUD;
import com.ucpeo.meal.okhttp.PostData;
import com.ucpeo.meal.utils.CookieJar;
import com.ucpeo.meal.utils.NetUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Pair;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
/**
 *
 * 充值界面
 * */
public class RechargeActivity extends Activity {
    public final int ERROR = 0;
    public final int SUCCESS = 1;
    public final int CONTINUE = 2;
    public final int BREAK = 4;
    public final int CHARGE = 5;
    public final int ARG_ERROR = 6;

    OkHttpClient okHttpClient = null;
    final String TAG = " recharge Activity";
    KProgressHUD kProgressHUD;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KProgressHUD.create(RechargeActivity.this);
        setContentView(R.layout.activity_recharge);
        findViewById(R.id.recharge_create).setOnClickListener(onClickListener);
        findViewById(R.id.recharge_back).setOnClickListener(onClickListener);
        okHttpClient = new NetUtil(this).getOkHttpClient();
        handler = new Handler(getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ERROR:
                        kProgressHUD.dismiss();
                        break;
                    case ARG_ERROR:
                        kProgressHUD.dismiss();
                        new AlertDialog.Builder(RechargeActivity.this).setTitle("参数错误")//设置对话框标题
                                .setMessage("填写的数值错误或其他错误")//设置显示的内容
                                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {//添加确定按钮
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                        kProgressHUD.dismiss();
                                    }
                                }).show();//显示此对话框
                        break;
                    case CONTINUE:
                        kProgressHUD.dismiss();
                        kProgressHUD.setLabel((String) msg.obj).show();
                        break;
                    case BREAK:
                        kProgressHUD.dismiss();
                        break;
                    case SUCCESS:
                        try {
                            startActivity((Intent) msg.obj);
                        } catch (Exception e) {
                            new AlertDialog.Builder(RechargeActivity.this).setTitle("唤醒支付宝失败")//设置对话框标题
                                    .setMessage("无法唤醒支付宝，请确认是否安装支付宝")//设置显示的内容
                                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {//添加确定按钮
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                            kProgressHUD.dismiss();
                                        }
                                    }).show();//显示此对话框

                        }
                        break;
                    case CHARGE:
                        kProgressHUD.dismiss();
                        TextView charge = findViewById(R.id.recharge_charge);
                        charge.setText((String) msg.obj);
                        break;

                }
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kProgressHUD != null && kProgressHUD.isShowing()) {
            kProgressHUD.dismiss();
        }
        getCharge();

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.recharge_back:
                    finish();
                    break;
                case R.id.recharge_create:
                    kProgressHUD
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("验证登录")
                            .setCancellable(true)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show();
                    init();
                    break;
            }
        }
    };

    public void create() {

        String url = "http://pay.cqwu.edu.cn/scardRechargeCreateOrder";
        PostData postData = new PostData();



        postData.append("payAmt", ((EditText) findViewById(R.id.recharge_money)).getText().toString()); // 金额
        postData.append("payProjectId", "2");
        postData.append("factorycode", "Z007");
        postData.append("rechargeType", "1");  // 本人 1  他人2
        postData.append("recharge_idserial", ""); // 他人学号
        Request.Builder reqb = new Request.Builder().url(url);

        NetUtil.httpPostData(reqb, postData);

        okHttpClient.newCall(reqb.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: 订单创建失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Message msg = Message.obtain();
                String str = response.body().string();
                Log.v(TAG, "创建订单界面：" + str);
                try {
                    JSONObject json = new JSONObject(str);
                    Log.v(TAG, json.toString());
                    json = json.getJSONObject("payOrderTrade");
                    String payid = json.getString("orderno");
                    Log.v(TAG, payid);

                    RadioButton radioButton = findViewById(R.id.pay_alipy);
                    if (radioButton.isChecked())
                        aliPay(payid);
                    else
                        weixinPay(payid);
                    msg.what = CONTINUE;
                    msg.obj = "创建支付";
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.v(TAG, "json 转换失败");
                    Message m =Message.obtain();
                    m.what=ARG_ERROR;
                    handler.sendMessage(m);
                    return;
                }


            }
        });

    }

    void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.v(TAG, "init 建立请求");
                Request req = new Request.Builder().url("http://ehall.cqwu.edu.cn:80/newmobile/client/userStoreAppList?identity=512aa3d4").build();
                okHttpClient.newCall(req).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.v(TAG, "请求失败");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String url = "http://pay.cqwu.edu.cn:80/signAuthentication?url=openSchoolCardRecharge-payProjectId=2-id=3&timestamp=1568037373232";
                        final Request.Builder builder = new Request.Builder().url(url);
                        okHttpClient.newCall(builder.build()).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                Log.v(TAG, "init 2请求失败");
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                Log.v(TAG, "请求成功");
                                String str = response.body().string();
                                response.close();
                                if (str.contains("<title>校园缴费平台</title>")) {
                                    Message message = Message.obtain();
                                    message.what = CONTINUE;
                                    message.obj = "创建订单";
                                    handler.sendMessage(message);
                                    create();
                                    return;
                                }

                                String reg = "window.location.href='(.*?)'";
                                final Matcher matcher = Pattern.compile(reg).matcher(str);
                                if (matcher.find()) {
                                    okHttpClient.newCall(builder.url(matcher.group(1)).build()).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            Log.v(TAG, "init 3次请求失败");
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                            Log.v(TAG, "init");
                                            String str = response.body().string();
                                            //Log.v(TAG, str);
                                            if (str.contains("<title>校园缴费平台</title>")) {
                                                Message message = Message.obtain();
                                                message.what = CONTINUE;
                                                message.obj = "创建订单";
                                                handler.sendMessage(message);
                                                create();
                                                return;
                                            } else {
                                                Message message = Message.obtain();
                                                message.what = BREAK;
                                                message.obj = "创建订单";
                                                handler.sendMessage(message);
                                                return;
                                            }
                                        }
                                    });
                                }

                            }
                        });
                    }
                });
            }
        }).start();
    }



    void weixinPay(String payid) {
        return;
    }


    void aliPay(String payid) {//更改为微信支付
       CookieJar cookieJar = (CookieJar)okHttpClient.cookieJar();
        Iterator<Cookie> iterator = cookieJar.getCache().iterator();
        while (iterator.hasNext()){
            Cookie cookie =iterator.next();
       //     Log.d(cookie.domain(), cookie.toString());
        }


        String payUrl = "http://pay.cqwu.edu.cn/h5onlinepay";

        PostData postData = new PostData();

        postData.append("paytype", "03");
        postData.append("tradetype", "WAP");
        postData.append("payways", "0203");
        postData.append("userip", "218.194.176.162");
        postData.append("contextPath", "");
        postData.append("orderno", payid);
        postData.append("orderamt",  ((EditText) findViewById(R.id.recharge_money)).getText().toString());
        postData.append("mess","");

        System.out.println(postData.toJson());
        final Request.Builder reqb = new Request.Builder();
        reqb.url(payUrl);
        NetUtil.httpPostData(reqb, postData);
        try {
            Response response =okHttpClient.newCall(reqb.build()).execute();
            System.out.println(response.request().url()+"code:" + response.code());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "aliPay: 出错啦");
        }
    }

    void getCharge() {
        if (kProgressHUD == null)
            kProgressHUD = KProgressHUD.create(this);
        kProgressHUD.setLabel("刷新余额中").show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request req = null;
                Request.Builder reqbuilder = new Request.Builder();
                // Tool.requestAddHeader(reqbuilder);
                reqbuilder.url("http://218.194.176.214:8382/epay/thirdapp/balance");
                req = reqbuilder.build();

                okHttpClient.newCall(req).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Message msg = Message.obtain();
                        String http_doc = response.body().string();
                        String executionREG = "weui-cell__ft\">(.*?)￥";
                        Pattern executionpattern = Pattern.compile(executionREG);
                        Matcher executionmatcher = executionpattern.matcher(http_doc);
                        String execution = "";
                        if (executionmatcher.find()) {
                            execution = executionmatcher.group(0);
                            execution = execution.replace("weui-cell__ft\">", "").replace("￥", "");
                            msg.what = CHARGE;
                            msg.obj = execution;
                            handler.sendMessage(msg);
                        } else {
                            msg.what = ERROR;
                            handler.sendMessage(msg);
                            Log.v(TAG, "获取余额 失败\n" + http_doc);
                        }
                    }
                });

            }
        }).start();

    }


}
