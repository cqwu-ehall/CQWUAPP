package com.ucpeo.meal.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.lxj.xpopup.XPopup;
import com.ucpeo.meal.TAppllication;
import com.ucpeo.meal.WebViewActivity;


public final class ShowPrivacy {
    private ShowPrivacy() {
    }

    private static final String PRIVACY_URL = "file:///android_asset/privacy.html";

    public static void showPrivacyDialog(Context context, TAppllication ctx) {
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .asConfirm("接受服务风险及免责声明", getPrivacy(context),
                        "拒绝", "同意",
                        ctx::AcceptPrivacy, ctx::CancelPrivacy, false)
                .show();
    }

    private static SpannableStringBuilder getPrivacy(Context context) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder()
                .append("    欢迎使用 文理校园 APP!\n")
                .append("    我们深知个人信息对你的重要性，也感谢你对我们的信任。\n")
                .append("为了更好地保护你的权益，同时遵守相关监管的要求，我们将通过");
        stringBuilder.append(getPrivacyLink(context))
                .append("向你说明我们会如何收集、存储、保护、使用及对外提供你的信息，并说明你享有的权利。\n")
                .append("\n    更多详情，敬请查阅")
                .append(getPrivacyLink(context))
                .append("全文。");
        return stringBuilder;
    }

    private static SpannableString getPrivacyLink(Context context) {
        String privacyName = "隐私政策";
        SpannableString spannableString = new SpannableString(privacyName);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.setData(Uri.parse(PRIVACY_URL));
                context.startActivity(intent);
            }
        }, 0, privacyName.length(), Spanned.SPAN_MARK_MARK);
        return spannableString;
    }
}
