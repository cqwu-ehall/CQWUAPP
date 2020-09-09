package com.ucpeo.meal.okhttp;



import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostData {
    public class Data {
        public String name;
        public String value;

        public Data() {
        }
        public Data(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private List<Data> datas = new ArrayList<Data>();

    public Boolean append(String name, String value) {
        datas.add(new Data(name, value));
        return true;
    }

    public Boolean append(Data data) {
        datas.add(data);
        return true;
    }

    public void remove(String name) {
        if (datas == null)
            return;
        for (Data data : datas) {
            if (data.name.equals(name))
                datas.remove(data);
        }
    }
    public void replace(String name,String value){
        if (datas == null)
            return;
        for (Data data : datas) {
            if (data.name.equals(name))
               data.value=value;
        }
    }


    public String toGet(){
        StringBuffer sb = new StringBuffer("?");
        for(int i=0;i<datas.size();i++)
        {
            sb.append(datas.get(i).name+"="+datas.get(i).value);
            if(i<datas.size()-1)
                sb.append("&");
        }

        return sb.toString();
    }

    public JSONObject toJson()  {
        JSONObject json = new JSONObject();
        for(Data data : datas){
            try {
                json.put(data.name,data.value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public List<Data> getDatas() {
        return datas;
    }



}
