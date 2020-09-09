package com.ucpeo.meal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ucpeo.meal.utils.ShareSaved;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class JuanzhuActivity extends AppCompatActivity implements View.OnClickListener {
    ShareSaved shareSaved;
    TextView username_text;
    EditText money_edit;
    EditText fullname_eidt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
        shareSaved = new ShareSaved(this);

        username_text = findViewById(R.id.username);
        fullname_eidt = findViewById(R.id.full_name);
        money_edit = findViewById(R.id.money);
        findViewById(R.id.juanzhu_btn).setOnClickListener(this);
        username_text.setText(shareSaved.get("username"));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.juanzhu_btn:
                String des = "u:" + username_text.getText() + ",name:" + fullname_eidt.getText().toString();
                int money=10;
                try {
                    money = new Integer(money_edit.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String url = "alipays://platformapi/startapp?appId=000000&url=";
                String parm = "alipays://platformapi/startapp?appId=00000&actionType=scan&biz_data=";

                StringBuffer biz = new StringBuffer();
                biz.append("{").append("\"s\"").append(":")
                        .append("\"money\"").append(",")
                        .append("\"u\"").append(":")
                        .append("\"\"").append(",")
                        .append("\"a\"").append(":").append("\"").append(money).append("\",")
                        .append("\"m\"").append(":")
                        .append("\"").append(des).append("\"").append("}");
                parm = url + URLEncoder.encode(parm + biz.toString());
                Log.v("url：", parm);
                try {
                    Intent intent = Intent.parseUri(parm, Intent.URI_INTENT_SCHEME);
                    startActivity(intent);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }


        }
    }
}
