package com.ucpeo.meal.okhttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostData {
    public static class Data {
        public String name;
        public String value;

        public Data(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private final List<Data> datas = new ArrayList<>();

    public void append(String name, String value) {
        datas.add(new Data(name, value));
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (Data data : datas) {
            try {
                json.put(data.name, data.value);
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
