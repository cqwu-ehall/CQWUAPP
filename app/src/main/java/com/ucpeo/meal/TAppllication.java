package com.ucpeo.meal;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.ucpeo.meal.utils.NetUtil;
import com.ucpeo.meal.utils.ShareSaved;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import java.util.Objects;

import okhttp3.OkHttpClient;

public class TAppllication extends Application {
    String TAG = "application 应用";
    ShareSaved shareSaved;
    OkHttpClient okHttpClient;
    //读写权限

    @Override
    public void onCreate() {
        super.onCreate();
        okHttpClient = new NetUtil(this).getOkHttpClient();
        shareSaved = new ShareSaved(this);
        ZXingLibrary.initDisplayOpinion(this);
    }

    public OkHttpClient get_http_client() {
        return okHttpClient;
    }

    public String get(String key) {
        return shareSaved.get(key);
    }

    public void save(String key, String value) {
        shareSaved.save(key, value);
    }

    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    public int getHistoryVersion() {
        String ver_str = get("version");
        try {
            return Integer.parseInt(ver_str);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean checkVersion() {
        int version = getLocalVersion(this);
        int history_version = getHistoryVersion();
        Log.v(TAG, "version :" + version + " history version :" + history_version);
        return version > history_version;
    }

    public void saveVersion() {
        save("version", String.valueOf(getLocalVersion(this)));
    }

    public boolean NeedShowPrivacyDialog() {
        try {
            String privacy = get("privacy_v1");
            return !Objects.equals(privacy, "true");
        } catch (Exception e) {
            return true;
        }
    }

    public void AcceptPrivacy() {
        save("privacy_v1", "true");
    }

    public void CancelPrivacy() {
        save("privacy_v1", "false");
    }

    public void fullScreen(Activity activity, Boolean light) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            if (!light) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }
    }
}
