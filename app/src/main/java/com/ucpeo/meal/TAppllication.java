package com.ucpeo.meal;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.ucpeo.meal.utils.ShareSaved;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

public class TAppllication extends Application {
    String TAG = "application 应用";
    ShareSaved shareSaved;
    //读写权限



    @Override
    public void onCreate() {
        super.onCreate();
        shareSaved = new ShareSaved(this);
        ZXingLibrary.initDisplayOpinion(this);
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
            return Integer.valueOf(ver_str);
        } catch (Exception e) {
            Log.v(TAG,"转换版本号错误 : " +ver_str);
            return 0;
        }

    }

    public boolean checkVersion() {
        int version = getLocalVersion(this);
        int history_version = getHistoryVersion();
        Log.v(TAG,"version :" + version+ " history version :" + history_version);
        if (version > history_version) {
            return true;
        } else
            return false;
    }

    public void saveVersion(){
         save("version",String.valueOf(getLocalVersion(this)));

    }

}


