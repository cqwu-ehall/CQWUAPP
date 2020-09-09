package com.ucpeo.meal.utils;

import android.util.Log;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCookie implements CookieJar {
	 final  public String TAG= "Mycookie";
     	public MyCookie(){
	
	   }

	   CookieManager cookieManager =new CookieManager();

	   
	    private Map<String,List<Cookie>> cookieStore=new HashMap<String,List<Cookie>>();

	    public Map<String, List<Cookie>> getCookies() {
	        return cookieStore;
	    }

	    public  MyCookie(Map<String,List<Cookie>> cookieStore){
	        super();
	        this.cookieStore=cookieStore;
	    }

	    @Override
	    public List<Cookie> loadForRequest(HttpUrl arg0) {
	       // cookieManager
	    	if (cookieStore.get(arg0.host())==null) {
				//System.out.println("空kookie");
	        	return new ArrayList<Cookie>();
			}

			Log.v(TAG,"\n\nURL:"+arg0.toString());
			Log.v(TAG,"主机："+arg0.host() + "   query : " + arg0.query()+ " cookie个数："+cookieStore.get(arg0.host()).size());


	        for(Cookie cookie:cookieStore.get(arg0.host())) {
				System.out.println(cookie.toString());


				Log.v(TAG, "\n\n所有cookie");

				for (String key : cookieStore.keySet()) {
					for (Cookie cookie1 : cookieStore.get(key)) {
						System.out.println("host:"+key+ "   cookie:"+ cookie1.toString());
					}

					Log.v(TAG, "\n\n\n\n");
				}
	        }

				return cookieStore.get(arg0.host());

	    }

	    @Override
	    public void saveFromResponse(HttpUrl arg0, List<Cookie> cookies) {
	        List<Cookie> cookieList = cookies;

	        Map<String, Cookie> mapCookie = new HashMap<String, Cookie>();
	        if (cookieStore.get(arg0.host())!=null)
	        {
	            cookieList  = cookieStore.get(arg0.host());
	            for (Cookie c : cookieList)
	            {
	                mapCookie.put(c.name(),c);
	            }
	          for(Cookie c :cookies){
	                mapCookie.put(c.name(),c);

	            }
	            cookieList= new ArrayList<Cookie>();
	            for (Cookie c : mapCookie.values()){
	                 cookieList.add(c);
	            }
	        }
	        cookieStore.put(arg0.host(),cookieList);
	    }
	}