package com.ucpeo.meal.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;

/**
 * Created on 2016/2/24.
 * 生成二维码的工具类
 */
public class MakeQRCodeUtil {
    /**
     * 转换位图
     *
     * @param matrix 原图
     * @param alpha  透明度 0 透明 1半透明 2不透明
     */
    private static Bitmap bitMatrix2Bitmap(BitMatrix matrix, int alpha) {
        int WIDTH = matrix.getWidth();
        int HEIGHT = matrix.getHeight();
        int[] pixels = new int[WIDTH * HEIGHT];
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < HEIGHT; x++) {
                int color;
                switch (alpha) {
                    case 0:
                        color = 0x00FFFFFF;
                        break;
                    case 1:
                        color = 0x80FFFFFF;
                        break;
                    default:
                        color = 0xFFFFFFFF;
                        break;
                }
                if (matrix.get(x, y)) {
                    // 有内容的部分，颜色设置为黑色，当然这里可以自己修改成喜欢的颜色
                    color = 0xFF000000;
                }
                pixels[x + (y * WIDTH)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
        return bitmap;
    }

    /**
     * 设置透明度
     *
     * @return Paint    画笔
     */
    private static Paint setAlpha() {
        // 建立Paint 物件
        Paint vPaint = new Paint();
        vPaint.setStyle(Paint.Style.STROKE);   //空心
        vPaint.setAlpha(75);   //0—255
        return vPaint;
    }

    /**
     * 设置透明度
     *
     * @return Paint    画笔
     */
    private static Paint selectAlpha() {
        return setAlpha();
    }

    /**
     * 添加logo
     *
     * @param src  原图
     * @param logo logo图
     * @return 效果图
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (logo == null) {
            return src;
        }
        // 获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        // logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

    /**
     * 生成带logo的二维码
     *
     * @param logo    logo图
     * @param content 内容
     * @param width   宽度
     * @param height  高度
     * @param alpha   透明度
     * @param isEdge  去白边    true 去除 false 保留
     * @return 二维码
     */
    public static Bitmap generateQRCode(Bitmap logo, String content, int width, int height, int alpha, boolean isEdge) {
        return addLogo(generateQRCode(content, width, height, alpha, isEdge), logo);
    }

    /**
     * 生成不带logo的二维码
     *
     * @param content 内容
     * @param width   宽度
     * @param height  高度
     * @param alpha   透明度
     * @param isEdge  去白边    true 去除 false 保留
     * @return 二维码
     */
    public static Bitmap generateQRCode(String content, int width, int height, int alpha, boolean isEdge) {
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            // 设置编码方式utf-8
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //设置二维码的纠错级别为h
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix matrix;
            if (isEdge) {
                matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            } else {
                matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            }
            return bitMatrix2Bitmap(matrix, alpha);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

}
