package com.ucpeo.meal.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.ucpeo.meal.R;
import com.ucpeo.meal.TAppllication;
import com.ucpeo.meal.utils.MakeQRCodeUtil;
import com.ucpeo.meal.utils.QRcode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Implementation of App Widget functionality.
 */
public class Widget extends AppWidgetProvider {
    private static final String MONTH_TOP_CLICK = "com.ucpeo.meal.action.APPWIDGET_UPDATE";
    private static final String TAG = "桌面小部件";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.v(TAG, "updateAppWidget");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.widget_update, "更新");
        Intent topIntent = new Intent(context, Widget.class).setAction(MONTH_TOP_CLICK);
        PendingIntent topPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, topIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget, topPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_update, topPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v(TAG, "收到广播:" + action);
        if (MONTH_TOP_CLICK.equals(action)) {
            Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
            long[] patter = {20, 10, 10};
            vibrator.vibrate(patter, -1);
            create(context);
            return;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.v(TAG, "onUpdate");

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Log.v(TAG, "onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.v(TAG, "onDisabled");
    }

    public static void create(final Context context) {
        TAppllication application = (TAppllication) context.getApplicationContext();
        String lst = application.get("lastTime");
        if (!"".equals(lst)) {
            if (System.currentTimeMillis() - Long.parseLong(lst) < 1000) {
                return;
            }
        }
        application.save("lastTime", String.valueOf(System.currentTimeMillis()));
        try {
            final QRcode qRcode = new QRcode(context, application.get_http_client());
            qRcode.getCodeOnHttp();
            final long now = System.currentTimeMillis();
            qRcode.setListener(new QRcode.QRlistener() {
                @Override
                public void needLoginError() {
                    Log.v(TAG, "需要登录");
                }

                @Override
                public void netWorkError(String des) {
                    Log.v(TAG, "网络错误");
                    error(context, des);
                }

                @Override
                public void success(String code) {
                    Log.v(TAG, "获取到支付码：" + code);
                    Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
                    Bitmap bitmap = MakeQRCodeUtil.generateQRCode(icon, code, 400, 400, 0, true);
                    if (bitmap != null) {
                        apply(context, bitmap);
                        Log.v(TAG, "本次耗时:" + (System.currentTimeMillis() - now) + " ms");
                        qRcode.writeCookies();
                        qRcode.getBalance();
                    }
                }

                @Override
                public void successBalance(String balance) {
                    if (balance.equals("null"))
                        return;
                    Log.v(TAG, "获取到余额：" + balance);
                    apply(context, balance);
                }
            });
        } catch (Exception e) {
            Log.v(TAG, "error");
        }
    }


    public static void apply(Context context, Bitmap bitmap) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setImageViewBitmap(R.id.widget_image, bitmap);
        remoteViews.setViewVisibility(R.id.widget_net_error, View.INVISIBLE);
        remoteViews.setViewVisibility(R.id.widget_licence, View.INVISIBLE);
        Intent layout = new Intent(context, Widget.class);
        layout.setAction(MONTH_TOP_CLICK);
        remoteViews.setOnClickPendingIntent(R.id.widget, PendingIntent.getBroadcast(context, 0, layout, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        long[] patter = {20, 30, 10};
        vibrator.vibrate(patter, -1);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, Widget.class), remoteViews);
    }

    public static void apply(Context context, String balance) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setTextViewText(R.id.widget_balance, balance);
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        remoteViews.setTextViewText(R.id.widget_time, format.format(date));
        remoteViews.setViewVisibility(R.id.widget_net_error, View.INVISIBLE);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, Widget.class), remoteViews);
    }

    public static void error(Context context, String des) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setViewVisibility(R.id.widget_net_error, View.VISIBLE);
        remoteViews.setTextViewText(R.id.widget_error_des, des);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, Widget.class), remoteViews);
    }
}
