package com.ucpeo.meal.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class ShareSaved{
    Context context ;
    SharedPreferences preferences;
    public ShareSaved(Context context) {
        this.context = context;
        preferences=context.getSharedPreferences("share",Context.MODE_PRIVATE);
    }

    public String get(String key){
        return  preferences.getString(key,"");
    }

    public void save(String key,String value){
        preferences.edit().putString(key,value).commit();
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }
}
