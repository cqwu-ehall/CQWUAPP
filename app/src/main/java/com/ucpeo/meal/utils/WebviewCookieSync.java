package com.ucpeo.meal.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;

public class WebviewCookieSync {
    public static final String TAG = "SQLIte";
    SQLiteOpenHelper sqLiteOpenHelper;

    public WebviewCookieSync(Context context, String db) {
        sqLiteOpenHelper = new SQLiteOpenHelper(context, db, null, 2) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                Log.d(TAG, "Cookie 不存在: ");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }
        };

    }
    /***
     *
     * 自动寻找cookie 文件
     *
     */
    public WebviewCookieSync(Context context) {
           File file = new File("/data/user/0/com.ucpeo.meal/app_webview/Cookies");
           if (!file.exists()){
               file = new File("/data/user/0/com.ucpeo.meal/app_webview/Default/Cookies");
           }

        sqLiteOpenHelper = new SQLiteOpenHelper(context, file.toString(), null, 2) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                Log.d(TAG, "Cookie 不存在: ");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }
        };

    }

    public List<Cookie> getCooikes() {
        Cursor cursor = sqLiteOpenHelper.getReadableDatabase().query("cookies", new String[]{"host_key", "name", "value", "path"}, "", null, null, null, null);
        List<Cookie> cookies = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> kv = new HashMap<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                kv.put(cursor.getColumnName(i), cursor.getString(i));
            }
            Cookie cookie = new Cookie.Builder().domain(kv.get("host_key")).name(kv.get("name")).value(kv.get("value")).path(kv.get("path")).build();
            cookies.add(cookie);
        }
        return cookies;
    }
}
