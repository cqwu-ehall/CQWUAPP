package com.ucpeo.meal;


import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

public class Welcome extends AhoyOnboarderActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AhoyOnboarderCard ahoyOnboarderCard1 = new AhoyOnboarderCard("付款码", "将付款码搬家到桌面,支付我快人一步", R.drawable.barcode);
        AhoyOnboarderCard ahoyOnboarderCard2 = new AhoyOnboarderCard("开门", "二维码推送到桌面,开门还需要打开某日校园?看点广告，慢慢载入页面？在这里不存在的", R.drawable.door);
        AhoyOnboarderCard ahoyOnboarderCard3 = new AhoyOnboarderCard("充值", "集成快速充值通道,剔除其他支付方式，采用最优秀安全的支付宝。", R.drawable.wallet);
        AhoyOnboarderCard ahoyOnboarderCard4 = new AhoyOnboarderCard("使用协议&免责声明", "本软件将使用你的账户信息，包括但不限于通过本软件产生的任何数据,本软件属于免费共享软件,仅用于个人方便生活和学习,即对于本软件的使用造成任何后果与软件作者无关,使用者自行承担任何责任。", R.drawable.tip);

        ahoyOnboarderCard1.setBackgroundColor(R.color.white);
        ahoyOnboarderCard2.setBackgroundColor(R.color.white);
        ahoyOnboarderCard3.setBackgroundColor(R.color.white);
        ahoyOnboarderCard4.setBackgroundColor(R.color.white);

        List<AhoyOnboarderCard> pages = new ArrayList<>();

        pages.add(ahoyOnboarderCard1);
        pages.add(ahoyOnboarderCard2);
        pages.add(ahoyOnboarderCard3);
        pages.add(ahoyOnboarderCard4);

        for (AhoyOnboarderCard page : pages) {
            page.setTitleColor(R.color.black);
            page.setDescriptionColor(R.color.grey_600);
        }

        setFinishButtonTitle("进入");
        showNavigationControls(false);

        List<Integer> colorList = new ArrayList<>();
        colorList.add(R.color.solid_one);
        colorList.add(R.color.solid_two);
        colorList.add(R.color.solid_three);

        setColorBackground(colorList);


        setOnboardPages(pages);

    }

    @Override
    public void onFinishButtonPressed() {
        Toast.makeText(this, "Finish Pressed", Toast.LENGTH_SHORT).show();
        TAppllication appllication = (TAppllication) getApplication();
        appllication.saveVersion();
        finish();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }
}
