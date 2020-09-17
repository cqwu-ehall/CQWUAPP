package com.ucpeo.meal.utils;

import android.content.Context;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.CookieCache;
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;


public class CookieJar extends PersistentCookieJar {
    private CookieCache cache;
    private CookiePersistor persistor;

    public CookieJar(CookieCache cache, CookiePersistor persistor) {
        super(cache, persistor);
        this.cache = cache;
        this.persistor = persistor;
    }

    public CookieCache getCache() {
        return cache;

    }

    public CookiePersistor getPersistor() {
        return persistor;
    }


}
